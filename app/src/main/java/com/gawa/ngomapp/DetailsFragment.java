package com.gawa.ngomapp;

import static com.gawa.ngomapp.ui.home.HomeFragment.Broadcast_PLAY_NEW_AUDIO;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gawa.ngomapp.models.Song;
import com.gawa.ngomapp.services.MyService;
import com.gawa.ngomapp.ui.home.HomeViewModel;
import com.gawa.ngomapp.utils.StorageUtils;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;


public class DetailsFragment extends Fragment {

    private HomeViewModel homeViewModel;
    ImageView imgPlay, imgNext, imgPrevious;
    MediaPlayer mediaPlayer;
    boolean isPlaying = false;
    boolean serviceBound = false;
    TextView title, description;
    Song song;
    MyService player;
    int index = 0;

    ArrayList<Song> songs = new ArrayList<>();

    public DetailsFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getArguments() !=null){
            serviceBound = getArguments().getBoolean("SERVICE");
        }
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        imgPlay = view.findViewById(R.id.img_play);
        title = view.findViewById(R.id.txt_song_title);
        description = view.findViewById(R.id.txt_song_details);
        imgNext = view.findViewById(R.id.img_next);
        imgPrevious = view.findViewById(R.id.img_previous);
        imgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                playSong();
            }
        });


        imgNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                homeViewModel.getNext()
                        .observe(requireActivity(), song1 -> {
                            song = song1;
                            setupSong();

                        });
            }
        });

        imgPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homeViewModel.getPrevious()
                        .observe(requireActivity(), song1 -> {
                            song = song1;
                            setupSong();

                        });
            }
        });

//        mediaPlayer = new MediaPlayer();
//        mediaPlayer.setAudioAttributes(
//                new AudioAttributes.Builder()
//                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                        .setUsage(AudioAttributes.USAGE_MEDIA)
//                        .build()
//        );

        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        homeViewModel.getSelected().observe(requireActivity(), song1 -> {

            song = song1;
            setupSong();

        });

    }


    private void setupSong(){

        homeViewModel.getIndex().observe(requireActivity(), position->{
            index=position;
        });

        if (serviceBound){
            StorageUtils storage = new StorageUtils(getActivity().getApplicationContext());
            storage.storeAudioIndex(index);

            //Service is active so we send a broadcast to it
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            requireActivity().sendBroadcast(broadcastIntent);
        }
        else{

            homeViewModel.getSongs().observe(requireActivity(), songs1 -> {
                songs.addAll(songs1);

                StorageUtils storage = new StorageUtils(getActivity().getApplicationContext());
                storage.storeAudio(songs);
                storage.storeAudioIndex(index);


                Intent playerIntent = new Intent(getActivity(), MyService.class);
//            playerIntent.putExtra("DATA", data);

                requireActivity().startService(playerIntent);
                requireActivity().bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            });


        }
        isPlaying = true;
        imgPlay.setImageResource(R.drawable.ic_pause);


//        try {
//            mediaPlayer.setDataSource(getActivity().getApplicationContext(), Uri.parse(song.getData()));
//            mediaPlayer.prepare();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        title.setText(song.getName());
        description.setText(song.getArtist()+", "+song.getAlbum());

    }

    private void loadSongInfo() {

        if (getArguments().containsKey("DATA")){

            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(getActivity().getApplicationContext(), Uri.parse(getArguments().getString("DATA")));
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (getArguments().containsKey("NAME")){
            title.setText(getArguments().getString("NAME"));
        }
        if (getArguments().containsKey("ARTIST")){
            description.setText(getArguments().getString("ARTIST")+" "+getArguments().getString("ALBUM"));
        }

    }

    @Override
    public void onStop() {
        super.onStop();
//        mediaPlayer.stop();
//        mediaPlayer.release();
    }


    public void playSong(){

        if (isPlaying){

            player.pauseMedia();
            isPlaying = false;
            imgPlay.setImageResource(R.drawable.ic_play);

        }
        else{
            player.playMedia();
            isPlaying = true;
            imgPlay.setImageResource(R.drawable.ic_pause);

        }
    }

    public void stopSong(){
        mediaPlayer.pause();
        isPlaying = false;
        imgPlay.setImageResource(R.drawable.ic_play);
    }

    public void changeSong(){
//        if (mediaPlayer.isPlaying()){
//            stopSong();
//            mediaPlayer.stop();
//        }
//        try {
//            mediaPlayer.reset();
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
        setupSong();
//        playSong();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            MyService.LocalBinder binder = (MyService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

            Toast.makeText(getActivity(), "Service Bound", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceBound = false;
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (serviceBound){
            requireActivity().unbindService(serviceConnection);
            player.stopSelf();
        }
    }
}

