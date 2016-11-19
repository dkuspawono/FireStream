package io.syntonic.firestream;

import android.app.Application;
import android.content.res.Configuration;

import kaaes.spotify.webapi.android.SpotifyApi;

/**
 * Created by Andrew on 11/19/2016.
 */

public class MyApplication extends Application {

    SpotifyApi spotifyApi;
    public String spotifyUserId = null;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        spotifyApi = new SpotifyApi();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

}
