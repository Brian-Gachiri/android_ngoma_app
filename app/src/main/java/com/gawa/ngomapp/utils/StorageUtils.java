package com.gawa.ngomapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.gawa.ngomapp.models.Song;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class StorageUtils {

    private final String STORAGE = "com.gawa.ngomapp.STORAGE";
    private SharedPreferences preferences;
    private Context context;

    public StorageUtils(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(
                STORAGE, Context.MODE_PRIVATE);
    }

    public void storeAudioIndex(int index){

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("AUDIO_INDEX", index);
        editor.apply();
    }

    public int loadAudioIndex(){
        return preferences.getInt("AUDIO_INDEX", -1);
    }

    public void clearCachedAudioPlaylist(){
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    public void storeAudio(ArrayList<Song> arrayList){
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("AUDIO_LIST", json);
        editor.apply();

    }

    public ArrayList<Song> loadAudio(){
        Gson gson = new Gson();
        String json = preferences.getString("AUDIO_LIST", null);
        Type type = new TypeToken<ArrayList<Song>>(){}.getType();

        return gson.fromJson(json, type);
    }


}
