package com.gawa.ngomapp.ui.home;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gawa.ngomapp.models.Song;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<Integer> selectedPosition = new MutableLiveData<Integer>();
    private MutableLiveData<List<Song>> songs;
    private MutableLiveData<Song> selected = new MutableLiveData<Song>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<List<Song>> getSongs() {

        if (songs == null){
            songs = new MutableLiveData<List<Song>>();
            loadSongs();
        }

        return songs;
    }

    private void loadSongs(){
        //This is where we get the songs from a content provider

        ContentResolver contentResolver = getApplication().getApplicationContext().getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);


        if (cursor != null && cursor.getCount() > 0) {
//            songs = new ArrayList<>();
            List<Song> ourSongs = new ArrayList<>();

            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                // Save to audioList
                ourSongs.add(new Song(title, data, album, artist));

            }
            songs.setValue(ourSongs);

        }
        cursor.close();

    }

    public void select(Song song,  Integer position){
        selected.setValue(song);
        selectedPosition.setValue(position);
    }


    public LiveData<Song> getSelected(){
        return selected;
    }

    public LiveData<Integer> getIndex(){ return selectedPosition;}



    public LiveData<Song> getNext(){
        int position;

        if (selectedPosition.getValue() > getSongs().getValue().size()){
            position = 0;
        }
        else{
            position = selectedPosition.getValue() + 1;

        }
        Song song = getSongs().getValue().get(position);

        select(song, position);

        return getSelected();
    }


    public LiveData<Song> getPrevious(){
        int position;

        if (selectedPosition.getValue() == 0){
            position = 0;
        }
        else{
            position = selectedPosition.getValue() - 1;

        }
        Song song = getSongs().getValue().get(position);

        select(song, position);

        return getSelected();
    }
}