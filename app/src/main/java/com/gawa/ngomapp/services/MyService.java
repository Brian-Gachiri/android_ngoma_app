package com.gawa.ngomapp.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.gawa.ngomapp.MainActivity;
import com.gawa.ngomapp.R;
import com.gawa.ngomapp.models.Song;
import com.gawa.ngomapp.ui.home.HomeFragment;
import com.gawa.ngomapp.utils.MessageManager;
import com.gawa.ngomapp.utils.StorageUtils;

import java.io.IOException;
import java.util.ArrayList;

public class MyService extends Service implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener {

    //ACTION CONSTANTS
    private static final String ACTION_PLAY = "com.gawa.ngomapp.ACTION_PLAY";
    private static final String ACTION_PAUSE = "com.gawa.ngomapp.ACTION_PAUSE";
    private static final String ACTION_PREVIOUS = "com.gawa.ngomapp.ACTION_PREVIOUS";
    private static final String ACTION_NEXT = "com.gawa.ngomapp.ACTION_NEXT";
    private static final String ACTION_STOP = "com.gawa.ngomapp.ACTION_STOP";

    //Notification ID
    private static final int NOTIFICATION_ID = 101;

    //Media Session Variables
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;






    MediaPlayer mediaPlayer;
    String mediaFile;
    private int resumePosition;
    private AudioManager audioManager;

    //List of available audio files
    private ArrayList<Song> audioList;
    private int audioIndex = -1;
    private Song activeAudio; //The currently playing audio file



    public class LocalBinder extends Binder{

        //Here we define a method that returns an instance of the
        //MediaPlayer service we have created

        public MyService getService(){
            return MyService.this;
        }
    }

    private final IBinder iBinder = new LocalBinder();

    public MyService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();

        //Listen for the new Audio to play -- BroadcastReciever
        register_playNewAudio();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
//            mediaFile = intent.getStringExtra("DATA");

            StorageUtils storage = new StorageUtils(getApplicationContext());
            audioList = storage.loadAudio();
            audioIndex = storage.loadAudioIndex();

            if (audioIndex != -1 && audioIndex < audioList.size()){
                activeAudio = audioList.get(audioIndex);
                mediaFile = activeAudio.getData();
            }
            else{
                stopSelf();
            }
        }

        catch (NullPointerException e){
            stopSelf();
        }

        if (!requestFocus()){
            stopSelf();
        }

        if (mediaSessionManager == null){
            try{
                initMediaSession();
                initMediaPlayer();
            } catch (RemoteException e){
                e.printStackTrace();
                stopSelf();
            }

            buildNotification(true);

        }

        //Handle intent action from MediaSession.TransportControls
        handleIncomingActions(intent);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

        switch (focusChange){

            case AudioManager.AUDIOFOCUS_GAIN:
                //resume playback
                if (mediaPlayer == null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                //Lost focus for an unknown amount of time: stop playback
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                //Lost but we are able to tell for how long
                //We stop playback but keep the media player
                //because playback is likely to resume

                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.setVolume(0.1f, 0.1f);
                }
                break;
        }

    }

    private boolean requestFocus(){
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
        );

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            return true;
        }
        return false;

    }

    private boolean removeAudioFocus(){
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

        stopMedia();

        stopSelf();

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int error, int extra) {

        switch(error){
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MEDIA::", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK "+ extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                 Log.d("MEDIA::", "MEDIA ERROR SERVER DIED"+extra);
                 break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MEDIA::", "MEDIA_ERROR_UNKNOWN "+ extra);
                break;
        }

        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        playMedia();

    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

    }

    private void initMediaPlayer(){

        mediaPlayer = new MediaPlayer();

        //setup the media player event listeners

        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);

        //here we reset the mediaPlayer so that it is not
        //pointing to another data source(song/audio)
        mediaPlayer.reset();


        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try{
            mediaPlayer.setDataSource(activeAudio.getData());
        }
        catch (IOException e){
            e.printStackTrace();
            stopSelf();
        }
        mediaPlayer.prepareAsync();

    }

    public void playMedia(){
        if (!mediaPlayer.isPlaying()){
            mediaPlayer.start();
        }
    }

    private void stopMedia(){
        //If the media player had not been initialised
        //We return which exits the function immediately.
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
    }

    public void pauseMedia(){
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            //we save he current paused position
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia(){
        if (!mediaPlayer.isPlaying()){
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }


    }

    private void skipToPrevious(){

        if (audioIndex == 0){
            //if first in playlist
            //set the index of the last in audioList
            audioIndex = audioList.size() -1;
            activeAudio = audioList.get(audioIndex);
        }
        else{
            activeAudio = audioList.get(--audioIndex);
        }

        new StorageUtils(getApplicationContext())
                .storeAudioIndex(audioIndex);

        stopMedia();

        mediaPlayer.reset();
        initMediaPlayer();
    }

    private void skipToNext(){
        if (audioIndex == audioList.size()-1){
            //if last in playlist
            audioIndex = 0;
            activeAudio = audioList.get(audioIndex);
        }
        else {
            //get next in playlist
            activeAudio = audioList.get(++audioIndex);
        }

        new StorageUtils(getApplicationContext())
                .storeAudioIndex(audioIndex);
        stopMedia();

        mediaPlayer.reset();
        initMediaPlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null){
            stopMedia();
            mediaPlayer.release();
        }

        removeAudioFocus();
        removeNotification();

        unregisterReceiver(playNewAudio);

        new StorageUtils(getApplicationContext()).clearCachedAudioPlaylist();
    }


    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            audioIndex = new StorageUtils(getApplicationContext())
                    .loadAudioIndex();

            if (audioIndex != -1 && audioIndex < audioList.size()){
                //Index is in a valid range
                activeAudio = audioList.get(audioIndex);
                mediaFile = activeAudio.getData();
            }
            else{
                stopSelf();
            }

            stopMedia();
            mediaPlayer.reset();
            initMediaPlayer();

            //TODO: Show Notification

        }
    };

    private void register_playNewAudio(){
        //Here we register the receiver using n intent filter
        //The same we register our Launcher activity using Intent filters

        IntentFilter filter = new IntentFilter(HomeFragment.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
    }


    /**
     * Media Session and Notification Actions
     */

    private void removeNotification(){

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    //Function to get Pending Intents for the
    // different playback actions

    private PendingIntent playbackAction(int actionNumber){
        Intent playbackAction =
                new Intent(this, MyService.class);

        switch (actionNumber){
            case 0:
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(
                        this, actionNumber,
                        playbackAction, 0
                );

            case 1:
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(
                        this, actionNumber,
                        playbackAction, 0
                );

            case 2:
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(
                        this, actionNumber,
                        playbackAction, 0
                );

            case 3:
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(
                        this, actionNumber,
                        playbackAction, 0
                );

            default:
                break;
        }
        return null; //If no action is specified, do nothing.
    }

    private void buildNotification(Boolean playbackStatus) {
        /**
         * Notification Actions => playBackAction()
         * 0 -> Play
         * 1 -> Pause
         * 2 -> Next Track
         * 3 -> Previous Track
         */

        int notificationButtonIcon = android.R.drawable.ic_media_pause;
        PendingIntent play_pauseAction = null;

        if (playbackStatus) {
            notificationButtonIcon = android.R.drawable.ic_media_pause;
            //create the pause action pending intent
            play_pauseAction = playbackAction(1);
        } else {
            notificationButtonIcon = android.R.drawable.ic_media_play;
            play_pauseAction = playbackAction(0);

        }

        Bitmap largeIcon = BitmapFactory.decodeResource(
                getResources(), R.drawable.brown);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MessageManager.CHANNEL_ID)
                //hide Timestamp
                .setShowWhen(false)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .setColor(getResources().getColor(R.color.brown))
                .setSmallIcon(android.R.drawable.ic_media_pause)
                .setLargeIcon(largeIcon)
                .setContentText(activeAudio.getName())
                .setContentTitle(activeAudio.getArtist())
                .setContentInfo(activeAudio.getAlbum())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                //Add the playback actions
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationButtonIcon, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(NOTIFICATION_ID, builder.build());
    }

    private void initMediaSession() throws RemoteException{

        if (mediaSessionManager != null) return; //MediaSessionManager Exists

        mediaSessionManager = (MediaSessionManager)
                getSystemService(Context.MEDIA_SESSION_SERVICE);

        //Create new media session
        mediaSession = new MediaSessionCompat(
                getApplicationContext(), "AudioPlayer");

        //Get controls from the session to help control the media
        transportControls = mediaSession.getController().getTransportControls();

        //Set mediaSession ready to receive media commands
        mediaSession.setActive(true);

        //Indicate that the mediasession handles the transport control commands
        //through its MediaSessionCompat.Callback
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        //set mediaSessions's Metadata
        updateMetaData();

        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();

                resumeMedia();
                buildNotification(true);
            }

            @Override
            public void onPause() {
                super.onPause();

                pauseMedia();
                buildNotification(false);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();

                skipToNext();
                updateMetaData();
                buildNotification(true);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
                updateMetaData();
                buildNotification(true);
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                stopSelf();
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
            }
        });

    }

    private void updateMetaData(){
        Bitmap albumArt = BitmapFactory.decodeResource(
                getResources(), R.drawable.brown);

        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeAudio.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeAudio.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio.getName())
                .build());
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mediaSession.release();
        removeNotification();
        return super.onUnbind(intent);

    }

    private void handleIncomingActions(Intent playbackAction){

        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();

        if (actionString.equalsIgnoreCase(ACTION_PLAY)){
            transportControls.play();
        }
        else if (actionString.equalsIgnoreCase(ACTION_PAUSE)){
            transportControls.pause();
        }
        else if (actionString.equalsIgnoreCase(ACTION_NEXT)){
            transportControls.skipToNext();
        }
        else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)){
            transportControls.skipToPrevious();
        }
        else if (actionString.equalsIgnoreCase(ACTION_STOP)){
            transportControls.stop();
        }
    }
}