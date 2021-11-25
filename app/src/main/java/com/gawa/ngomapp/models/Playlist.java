package com.gawa.ngomapp.models;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Playlist {

    @Id
    public long id;
    String name, created_at;

    public Playlist() {
    }

    public Playlist(String name, String created_at) {
        this.name = name;
        this.created_at = created_at;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
