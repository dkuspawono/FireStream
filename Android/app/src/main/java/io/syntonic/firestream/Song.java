package io.syntonic.firestream;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Andrew on 11/5/2016.
 */

public class Song implements Parcelable {

    public String name;
    public String album_url;
    public String artist;
    public String id;

    public Song() {

    }

    public Song(String id, String name, String album_url, String artist) {
        this.id = id;
        this.name = name;
        this.album_url = album_url;
        this.artist = artist;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.album_url);
        dest.writeString(this.artist);
        dest.writeString(this.id);
    }

    protected Song(Parcel in) {
        this.name = in.readString();
        this.album_url = in.readString();
        this.artist = in.readString();
        this.id = in.readString();
    }

    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}
