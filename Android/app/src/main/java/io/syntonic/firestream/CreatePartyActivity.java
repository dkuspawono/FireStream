package io.syntonic.firestream;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.Recommendations;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CreatePartyActivity extends AppCompatActivity {

    private final static String TAG = "CreatePartyActivity";

    private final static String[] SEED_TYPES = { "Genre", "Artist", "Track" };
    public static final String EXTRA_KEY_PARTY = "EXTRA_KEY_PARTY";
    private ArrayList<View> seedViews = new ArrayList<>();

    private String spotify_token = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_party);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Create a Party");

        addSeedView(true);

        Button getSpotifyPlaylists = (Button) findViewById(R.id.button_spotify_playlists);
        getSpotifyPlaylists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SpotifyService spotify = ((MyApplication)getApplicationContext()).spotifyApi.getService();
                spotify.getMyPlaylists(new Callback<Pager<PlaylistSimple>>() {
                    @Override
                    public void success(final Pager<PlaylistSimple> playlistSimplePager, Response response) {
                        if (playlistSimplePager != null) {
                            if (playlistSimplePager.items == null || playlistSimplePager.items.size() == 0) {
                                Toast.makeText(CreatePartyActivity.this, "You have no Spotify playlists", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String[] playlistNames = new String[playlistSimplePager.items.size()];
                            for (int i = 0; i < playlistSimplePager.items.size(); i++) {
                                playlistNames[i] = (playlistSimplePager.items.get(i).name);
                            }

                            AlertDialog.Builder builder = new AlertDialog.Builder(CreatePartyActivity.this);
                            builder.setTitle("Select a Playlist");
                            builder.setItems(playlistNames, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    dialog.dismiss();
                                    final String playlistId = playlistSimplePager.items.get(item).id;
                                    spotify.getPlaylistTracks(((MyApplication) getApplicationContext()).spotifyUserId, playlistId, new Callback<Pager<PlaylistTrack>>() {
                                        @Override
                                        public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {
                                            final TextView partyHost = (TextView) findViewById(R.id.et_party_host);
                                            final TextView partyName = (TextView) findViewById(R.id.et_party_name);
                                            final TextView partyPassword = (TextView) findViewById(R.id.et_party_password);

                                            if (partyHost.getText() == null || partyHost.getText().length() == 0) {
                                                Toast.makeText(CreatePartyActivity.this, "Party Host cannot be empty", Toast.LENGTH_LONG).show();
                                                return;
                                            }
                                            if (partyName.getText() == null || partyName.getText().length() == 0) {
                                                Toast.makeText(CreatePartyActivity.this, "Party Name cannot be empty", Toast.LENGTH_LONG).show();
                                                return;
                                            }
                                            Party party = new Party(partyName.getText().toString(), partyPassword.getText().toString());
                                            party.hostName = partyHost.getText().toString();
                                            party.hostSpotifyId = ((MyApplication) getApplicationContext()).spotifyUserId;

                                            for (int i = 0; i < playlistTrackPager.items.size(); i++) {
                                                Track track = playlistTrackPager.items.get(i).track;
                                                String name = track.name;
                                                String id = track.id;
                                                String artist = "";
                                                for (int a = 0; a < track.artists.size(); a++)
                                                    artist += track.artists.get(a).name + (a == track.artists.size() - 1 ? "" : " | ");

                                                String image = null;
                                                if (track.album.images != null)
                                                    image = track.album.images.get(i).url;

                                                party.queue.add(new Song(id, name, image, artist));
                                            }

                                            Intent data = new Intent();
                                            data.putExtra(EXTRA_KEY_PARTY, (Parcelable) party);
                                            setResult(RESULT_OK, data);
                                            finish();
                                        }

                                        @Override
                                        public void failure(RetrofitError error) {

                                        }
                                    });
                                }
                            });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });
            }
        });

        Button cancelButton = (Button) findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button createButton = (Button) findViewById(R.id.button_create);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final TextView partyHost = (TextView) findViewById(R.id.et_party_host);
                final TextView partyName = (TextView) findViewById(R.id.et_party_name);
                final TextView partyPassword = (TextView) findViewById(R.id.et_party_password);

                if (partyHost.getText() == null || partyHost.getText().length() == 0) {
                    Toast.makeText(CreatePartyActivity.this, "Party Host cannot be empty", Toast.LENGTH_LONG).show();
                    return;
                }
                if (partyName.getText() == null || partyName.getText().length() == 0) {
                    Toast.makeText(CreatePartyActivity.this, "Party Name cannot be empty", Toast.LENGTH_LONG).show();
                    return;
                }

                String seed_genres = "", seed_artists = "", seed_tracks = "";

                for (int i = 0; i < seedViews.size(); i++) {
                    View seed = seedViews.get(i);
                    Spinner seedType = (Spinner) seed.findViewById(R.id.spinner_seed_type);
                    EditText seedInfo = (EditText) seed.findViewById(R.id.et_seed_info);

                    if (seedInfo.getText() == null || seedInfo.getText().length() == 0) {
                        Toast.makeText(CreatePartyActivity.this, "Must enter a value for playlist builder option", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String type = seedType.getSelectedItem().toString();
                    switch (type.toLowerCase()) {
                        case "genre":
                            seed_genres += seedInfo.getText().toString() + ",";
                            break;
                        case "artist":
                            seed_artists += seedInfo.getText().toString() + ",";
                            break;
                        case "track":
                            seed_tracks += seedInfo.getText().toString() + ",";
                            break;
                    }
                }

                // Get recommendations from spotify API

                class SpotifyTask extends AsyncTask<Object, Object, Object> {

                    public AsyncResponse delegate = null;

                    public SpotifyTask(AsyncResponse asyncResponse) {
                        delegate = asyncResponse;
                    }

                    @Override
                    protected Object doInBackground(Object... params) {
                        String seed_artists = (String) params[0];
                        String seed_tracks = (String) params[1];

                        SpotifyService spotify = ((MyApplication)getApplicationContext()).spotifyApi.getService();
                        if (seed_artists.length() > 0) {
                            seed_artists = seed_artists.substring(0, seed_artists.length() - 1);

                            String[] artists = seed_artists.split(",");
                            seed_artists = "";
                            for (int i = 0; i < artists.length; i++) {
                                try {
                                    seed_artists += spotify.searchArtists(artists[i]).artists.items.get(0).id + ",";
                                } catch (RetrofitError error) {
                                    Log.d(TAG, error.getMessage());
                                }
                            }
                            seed_artists = seed_artists.substring(0, seed_artists.length() - 1);
                        }
                        if (seed_tracks.length() > 0) {
                            seed_tracks = seed_tracks.substring(0, seed_tracks.length() - 1);

                            String[] tracks = seed_tracks.split(",");
                            seed_tracks = "";
                            for (int i = 0; i < tracks.length; i++) {
                                seed_tracks += spotify.searchTracks(tracks[i]).tracks.items.get(0).id + ",";
                            }
                            seed_tracks = seed_tracks.substring(0, seed_tracks.length() - 1);
                        }

                        return new Object[] { seed_artists, seed_tracks };
                    }

                    @Override
                    protected void onPostExecute(Object result) {
                        delegate.processFinish(result);
                    }

                }

                final SpotifyService spotify = ((MyApplication)getApplicationContext()).spotifyApi.getService();
                final Map<String, Object> params = new HashMap<String, Object>();
                if (seed_genres.length() > 0) {
                    seed_genres = seed_genres.substring(0, seed_genres.length() - 1);
                    params.put("seed_genres", seed_genres);
                }
                SpotifyTask task = new SpotifyTask(new AsyncResponse() {
                    @Override
                    public void processFinish(Object output) {
                        Object[] results = (Object[]) output;
                        params.put("seed_artists", results[0]);
                        params.put("seed_tracks", results[1]);

                        spotify.getRecommendations(params, new Callback<Recommendations>() {
                            @Override
                            public void success(Recommendations recommendations, Response response) {
                                Party party = new Party(partyName.getText().toString(), partyPassword.getText().toString());
                                party.hostName = partyHost.getText().toString();
                                party.hostSpotifyId = ((MyApplication) getApplicationContext()).spotifyUserId;

                                for (int i = 0; i < recommendations.tracks.size(); i++) {
                                    Track track = recommendations.tracks.get(i);
                                    String name = track.name;
                                    String id = track.id;
                                    String artist = "";
                                    for (int a = 0; a < track.artists.size(); a++)
                                        artist += track.artists.get(a).name + (a == track.artists.size() - 1 ? "" : " | ");

                                    String image = null;
                                    if (track.album.images != null)
                                        image = track.album.images.get(0).url;

                                    party.queue.add(new Song(id, name, image, artist));
                                }

                                Intent data = new Intent();
                                data.putExtra(EXTRA_KEY_PARTY, (Parcelable) party);
                                setResult(RESULT_OK, data);
                                finish();
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Log.d(TAG, error.getMessage().toString());
                                Toast.makeText(CreatePartyActivity.this, "An error occured, try again later.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                task.execute(new Object[] { seed_artists, seed_tracks });

            }
        });
    }

    private void addSeedView(final boolean buttonAdd) {
        final LinearLayout layout = (LinearLayout)findViewById(R.id.ll_seed_container);

        final View seed = getLayoutInflater().inflate(R.layout.view_playlist_builder_seed, null);
        layout.addView(seed);

        seedViews.add(seed);

        ImageButton modifySeeds = (ImageButton) seed.findViewById(R.id.ib_add_seed);
        modifySeeds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonAdd) {
                    if (seedViews.size() >= 5) {
                        Toast.makeText(CreatePartyActivity.this, "You can only have 5 seeds for a playlist", Toast.LENGTH_LONG).show();
                        return;
                    }
                    addSeedView(false);
                } else {
                    layout.removeView(seed);
                    seedViews.remove(seed);
                }
            }
        });

        if (!buttonAdd) {
            ImageButton button = (ImageButton) seed.findViewById(R.id.ib_add_seed);
            button.setImageDrawable(getResources().getDrawable(R.drawable.ic_remove_white_24dp));
        }

        Spinner seedTypeSpinner = (Spinner) seed.findViewById(R.id.spinner_seed_type);

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, SEED_TYPES);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seedTypeSpinner.setAdapter(spinnerArrayAdapter);
    }

    public interface AsyncResponse {
        void processFinish(Object output);
    }
}
