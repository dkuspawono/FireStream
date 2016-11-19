package io.syntonic.firestream;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Andrew on 11/5/2016.
 */

public class Party {

    public String id;

    public String name = null;
    public String nameLower = null;
    public String password = null;
    public String hostName = null;
    public String hostToken = null;

    public int attendees;
    public boolean hasPassword;

    public ArrayList<Song> queue = new ArrayList<>();
    public ArrayList<Song> requests = new ArrayList<>();

    public Party() {

    }

    public Party(String name) {
        this.id = UUID.randomUUID().toString();
        this.hasPassword = false;
        this.name = name;
        this.nameLower = name.toLowerCase();
        this.attendees = 1;

        queue.add(new Song());
    }

    public Party(String name, String password) {
        this.id = UUID.randomUUID().toString();
        this.hasPassword = true;
        this.password = Utils.MD5(password);
        this.name = name;
        this.nameLower = name.toLowerCase();
        this.attendees = 1;
    }
}
