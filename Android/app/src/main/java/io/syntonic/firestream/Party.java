package io.syntonic.firestream;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Andrew on 11/5/2016.
 */

public class Party implements Parcelable {

    public String id;

    public String name = null;
    public String nameLower = null;
    public String password = null;
    public String hostName = null;
    public String hostSpotifyId = null;
    public String hostToken = null;

    public boolean hasPassword;
    public boolean isPlaying;
    public int progress;
    public long timestamp;
    public int attendees;

    public ArrayList<String> members = new ArrayList<>();
    public ArrayList<Song> queue = new ArrayList<>();
    public ArrayList<Song> requests = new ArrayList<>();

    public Party() {

    }

    public Party(String name, String password) {
        this.id = UUID.randomUUID().toString();
        if (password != null && !password.isEmpty()) {
            this.hasPassword = true;
            this.password = Utils.MD5(password);
        }
        this.name = name;
        this.nameLower = name.toLowerCase();
        this.isPlaying = true;
        this.progress = 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.nameLower);
        dest.writeString(this.password);
        dest.writeString(this.hostName);
        dest.writeString(this.hostSpotifyId);
        dest.writeString(this.hostToken);
        dest.writeByte(this.hasPassword ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isPlaying ? (byte) 1 : (byte) 0);
        dest.writeInt(this.progress);
        dest.writeLong(this.timestamp);
        dest.writeStringList(this.members);
        dest.writeTypedList(this.queue);
        dest.writeTypedList(this.requests);
        dest.writeInt(this.attendees);
    }

    protected Party(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.nameLower = in.readString();
        this.password = in.readString();
        this.hostName = in.readString();
        this.hostSpotifyId = in.readString();
        this.hostToken = in.readString();
        this.hasPassword = in.readByte() != 0;
        this.isPlaying = in.readByte() != 0;
        this.progress = in.readInt();
        this.timestamp = in.readLong();
        this.members = in.createStringArrayList();
        this.queue = in.createTypedArrayList(Song.CREATOR);
        this.requests = in.createTypedArrayList(Song.CREATOR);
        this.attendees = in.readInt();
    }

    public static final Creator<Party> CREATOR = new Creator<Party>() {
        @Override
        public Party createFromParcel(Parcel source) {
            return new Party(source);
        }

        @Override
        public Party[] newArray(int size) {
            return new Party[size];
        }
    };
}
