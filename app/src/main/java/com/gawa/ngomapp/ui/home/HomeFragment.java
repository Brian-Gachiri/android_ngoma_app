package com.gawa.ngomapp.ui.home;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gawa.ngomapp.R;
import com.gawa.ngomapp.adapters.SongsAdapter;
import com.gawa.ngomapp.databinding.FragmentHomeBinding;
import com.gawa.ngomapp.models.Song;
import com.gawa.ngomapp.utils.MessageManager;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    List<Song> songs = new ArrayList<>();
    Context context = getContext();
    SongsAdapter songsAdapter;
    RecyclerView songsRecyclerView;
    TextInputEditText inputSearch;
    MessageManager messageManager;
    NotificationManagerCompat notificationManager;

    MediaPlayer mediaPlayer;
    View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(requireActivity()).get(HomeViewModel.class);


        root = inflater.inflate(R.layout.fragment_home, container, false);
        inputSearch = root.findViewById(R.id.input_search);
        songsRecyclerView = root.findViewById(R.id.songs_recyclerview);
        songsRecyclerView.setNestedScrollingEnabled(true);
        songsRecyclerView.setLayoutManager(
                new LinearLayoutManager(context));


        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeViewModel.getSongs().observe(requireActivity(), songs1 -> {

            songs.clear();
            songs.addAll(songs1);

        });

        songsAdapter = new SongsAdapter(context, songs, this);
        songsRecyclerView.setAdapter(songsAdapter);

        messageManager = new MessageManager(getActivity(), R.drawable.ic_music);
        messageManager.createNotificationChannel();


        notificationManager = NotificationManagerCompat.from(getActivity());



    }

    @Override
    public void onStart() {
        super.onStart();

//        mediaPlayer = MediaPlayer.create(getActivity().getApplicationContext(), R.raw.song1);

//        mediaPlayer.start();

        verifyPermissions();

    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            mediaPlayer.stop();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        mediaPlayer.release();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    public void playSong(String data, String name){

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getActivity().getApplicationContext(), Uri.parse(data));
            mediaPlayer.prepare();
            mediaPlayer.start();

            notificationManager.notify(234, messageManager.setupNotification("Playing", name).build());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void stopSong(){

        try {
            mediaPlayer.pause();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public void goToDetails(Song song, Integer position){
//        bundle.putString("NAME", song.getName());
//        bundle.putString("ALBUM", song.getAlbum());
//        bundle.putString("DATA", song.getData());
//        bundle.putString("ARTIST", song.getArtist());


        Log.d("POSITION", "goToDetails: " + position);

        homeViewModel.select(song, position);
        Navigation.findNavController(root).navigate(R.id.action_fragment_details);

    }

    public void verifyPermissions(){

        String[] permissions= {
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        if (ContextCompat
                .checkSelfPermission(getActivity().getApplicationContext(), permissions[0])
                == PackageManager.PERMISSION_GRANTED){

        }
        else{
            ActivityCompat
                    .requestPermissions(getActivity(), permissions, 134);
        }
    }

}