package com.lf.phoneav.bean;

import java.io.Serializable;

import android.database.Cursor;

public class AudioItem implements Serializable {

    // Media._ID, Media.TITLE, Media.ARTIST, Media.DATA
    private String title;
    private String artist;
    private String data;

    public static AudioItem fromCursor(Cursor cursor) {
        AudioItem item = new AudioItem();
        item.setTitle(cursor.getString(1));
        item.setArtist(cursor.getString(2));
        item.setData(cursor.getString(3));
        return item;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }


}
