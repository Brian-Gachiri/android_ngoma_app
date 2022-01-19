package com.gawa.ngomapp;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gawa.ngomapp.models.Song;
import com.gawa.ngomapp.services.MyService;
import com.gawa.ngomapp.ui.home.HomeViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;


public class DetailsFragment extends Fragment {

    private HomeViewModel homeViewModel;
    ImageView imgPlay, imgNext, imgPrevious;
    MediaPlayer mediaPlayer;
    boolean isPlaying = false;
    TextView title, description;
    Song song;

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
                            changeSong();

                        });
            }
        });

        imgPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homeViewModel.getPrevious()
                        .observe(requireActivity(), song1 -> {
                            song = song1;
                            changeSong();

                        });
            }
        });

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        homeViewModel.getSongs().observe(requireActivity(), songs -> {

            Toast.makeText(getActivity(), "There are "+ songs.size()+" songs", Toast.LENGTH_LONG).show();
        });
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

        try {
            mediaPlayer.setDataSource(getActivity().getApplicationContext(), Uri.parse(song.getData()));
            mediaPlayer.prepare();

        } catch (Exception e) {
            e.printStackTrace();
        }
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
        mediaPlayer.stop();
        mediaPlayer.release();
    }


    public void playSong(){

        if (isPlaying){
//           stopSong();
            Intent intent = new Intent(getActivity(), MyService.class);
            getActivity().stopService(intent);
        }
        else{
//            mediaPlayer.start();
            isPlaying = true;
            imgPlay.setImageResource(R.drawable.ic_pause);


            Intent intent = new Intent(getActivity(), MyService.class);
            intent.putExtra("DATA", song.getData());
            getActivity().startService(intent);

        }
    }

    public void stopSong(){
        mediaPlayer.pause();
        isPlaying = false;
        imgPlay.setImageResource(R.drawable.ic_play);
    }

    public void changeSong(){
        if (mediaPlayer.isPlaying()){
            stopSong();
            mediaPlayer.stop();
        }
        try {
            mediaPlayer.reset();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        setupSong();
        playSong();
    }
}

