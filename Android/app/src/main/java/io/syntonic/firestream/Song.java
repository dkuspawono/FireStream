package io.syntonic.firestream;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Andrew on 11/5/2016.
 */

public class Song implements Parcelable {

    public String name;
    public String albumUrl;
    public String artist;
    public String id;
    public long duration;

    public Song() {

    }

    public Song(String id, String name, String albumUrl, String artist, long duration) {
        this.id = id;
        this.name = name;
        this.albumUrl = albumUrl;
        this.artist = artist;
        this.duration = duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.albumUrl);
        dest.writeString(this.artist);
        dest.writeString(this.id);
        dest.writeLong(this.duration);
    }

    protected Song(Parcel in) {
        this.name = in.readString();
        this.albumUrl = in.readString();
        this.artist = in.readString();
        this.id = in.readString();
        this.duration = in.readLong();
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
