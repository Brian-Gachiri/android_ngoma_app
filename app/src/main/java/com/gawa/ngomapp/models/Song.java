package com.gawa.ngomapp.models;

public class Song {

    String name, data, album, artist;
    boolean isPlaying = false;

    public Song(String name) {
        this.name = name;
    }

    public Song(String name, String data, String album, String artist) {
        this.name = name;
        this.data = data;
        this.album = album;
        this.artist = artist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
//
//implements Serializable {
//
//private String data;
//private String title;
//private String album;
//private String artist;
//
//public Audio(String data, String title, String album, String artist) {
//        this.data = data;
//        this.title = title;
//        this.album = album;
//        this.artist = artist;
//        }
//
//public String getData() {
//        return data;
//        }
//
//public void setData(String data) {
//        this.data = data;
//        }
//
//public String getTitle() {
//        return title;
//        }
//
//public void setTitle(String title) {
//        this.title = title;
//        }
//
//public String getAlbum() {
//        return album;
//        }
//
//public void setAlbum(String album) {
//        this.album = album;
//        }
//
//public String getArtist() {
//        return artist;
//        }
//
//public void setArtist(String artist) {
//        this.artist = artist;
//        }
