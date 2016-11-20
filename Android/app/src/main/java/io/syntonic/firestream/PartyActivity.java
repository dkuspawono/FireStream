package io.syntonic.firestream;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

import static io.syntonic.firestream.MyFirebaseMessagingService.EXTRA_KEY_SONG_REQUEST;
import static io.syntonic.firestream.MyFirebaseMessagingService.EXTRA_KEY_SONG_REQUEST_ACTION;
import static io.syntonic.firestream.MyFirebaseMessagingService.EXTRA_NOTIFICATION_INT;
import static io.syntonic.firestream.MyFirebaseMessagingService.REQUEST_CODE_SONG_REQUEST_ADD_TO_FRONT;
import static io.syntonic.firestream.MyFirebaseMessagingService.REQUEST_CODE_SONG_REQUEST_REJECT;
import static io.syntonic.firestream.MyFirebaseMessagingService.REQUEST_CODE_SONG_REQUEST_SHUFFLE_IN;
import static io.syntonic.firestream.Utils.millisToString;

public class PartyActivity extends AppCompatActivity implements ConnectionStateCallback, Player.NotificationCallback {

    private static final String TAG = "PartyActivity";
    private static final int REQUEST_CODE_SONG_SEARCH = 1;
    Party party;
    private Player mPlayer;
    private Socket mSocket;

    private QueueAdapter mAdapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party);

        party = getIntent().getParcelableExtra(CreatePartyActivity.EXTRA_KEY_PARTY);
        handleNotificationIntent(getIntent());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (party != null)
            setTitle(party.name);

        updateSongs();
        subscribeToParty();

        recyclerView = (RecyclerView) findViewById(R.id.queue);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        // If we are host, initialize player
        if (party != null && ((MyApplication) getApplicationContext()).spotifyUserId != null && ((MyApplication) getApplicationContext()).spotifyUserId.equals(party.hostSpotifyId) &&
                ((MyApplication) getApplicationContext()).spotifyAccessToken != null) {
            Config playerConfig = new Config(this, ((MyApplication) getApplicationContext()).spotifyAccessToken, getString(R.string.spotify_client_id));
            Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                @Override
                public void onInitialized(SpotifyPlayer spotifyPlayer) {
                    mPlayer = spotifyPlayer;
                    mPlayer.addConnectionStateCallback(PartyActivity.this);
                    mPlayer.addNotificationCallback(PartyActivity.this);
                }

                @Override
                public void onError(Throwable throwable) {
                    Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                }
            });
        }

        // Connect to node socket
        try {
            mSocket = IO.socket(Utils.SOCKET_URL);
            mSocket.connect();
            mSocket.emit("joinParty", party.id);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

        FloatingActionButton requestSong = (FloatingActionButton) findViewById(R.id.fab_request_song);
        requestSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PartyActivity.this, SongSearchActivity.class);
                intent.putExtra(CreatePartyActivity.EXTRA_KEY_PARTY, party);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");
        super.onNewIntent(intent);
        setIntent(intent);

        handleNotificationIntent(intent);
    }

    private void handleNotificationIntent(Intent intent) {
        Bundle bundle = getIntent().getExtras();

        if (!bundle.containsKey(EXTRA_KEY_SONG_REQUEST))
            return;

        String data = bundle.getString(EXTRA_KEY_SONG_REQUEST);

        // Dismiss the notification
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(bundle.getInt(EXTRA_NOTIFICATION_INT, -1));


        // Handle the notification's action
        try {
            JSONObject song = new JSONObject(data);
            int action = bundle.getInt(EXTRA_KEY_SONG_REQUEST_ACTION);

            String partyId = song.getString("partyId");

            String id = song.getString("id");
            String name = song.getString("name");
            String image = song.getString("albumUrl");
            String artist = song.getString("artist");
            long duration = song.getLong("duration");

            Song newSong = new Song(id, name, image, artist, duration);

            switch (action) {
                case REQUEST_CODE_SONG_REQUEST_ADD_TO_FRONT:
                    getPartyAndAddSong(partyId, newSong, true);
                    break;
                case REQUEST_CODE_SONG_REQUEST_SHUFFLE_IN:
                    getPartyAndAddSong(partyId, newSong, false);
                    break;
                case REQUEST_CODE_SONG_REQUEST_REJECT:

                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getPartyAndAddSong(final String partyId, final Song newSong, final boolean b) {
        Log.d(TAG, "getPartyAndAddSong");

        Utils.getDatabase().getReference(MainActivity.FIREBASE_DATABASE_TABLE_PARTIES).child(partyId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Party p = dataSnapshot.getValue(Party.class);
                p.queue.add(b ? 1 : new Random().nextInt(p.queue.size() - 1) + 1, newSong);
                p.timestamp = System.currentTimeMillis();

                // If app was previously closed
                if (party == null) {
                    party = p;
                    updateSongs();
                    subscribeToParty();
                }


                Utils.getDatabase().getReference(MainActivity.FIREBASE_DATABASE_TABLE_PARTIES).child(partyId).setValue(
                        p, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null)
                                    Toast.makeText(PartyActivity.this, "Song added", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateSongs() {
        if (party == null || party.queue == null || party.queue.size() == 0)
            return;

        Song song = party.queue.get(0);

        ImageView albumImage = (ImageView) findViewById(R.id.iv_player_album_art);
        TextView songName = (TextView) findViewById(R.id.tv_song_name);
        TextView songArtist = (TextView) findViewById(R.id.tv_song_artist);
        TextView songDuration = (TextView) findViewById(R.id.tv_song_duration);
        ImageButton controlsPrev = (ImageButton) findViewById(R.id.player_button_back);
        final ImageButton controlsPlayPause = (ImageButton) findViewById(R.id.player_button_play);
        ImageButton controlsNext = (ImageButton) findViewById(R.id.player_button_next);
        SeekBar songProgress = (SeekBar) findViewById(R.id.sb_player_progress);

        TextView partyCount = (TextView) findViewById(R.id.tv_party_count);
        partyCount.setText(String.valueOf(party.attendees));

        if (song.albumUrl != null)
            Picasso.with(this).load(song.albumUrl).into(albumImage);
        if (song.name != null)
            songName.setText(song.name);
        if (song.artist != null)
            songArtist.setText(song.artist);

        songDuration.setText(millisToString(song.duration));
        songProgress.setMax((int) song.duration);
        songProgress.setProgress(party.progress);

        controlsPlayPause.setImageDrawable(party.isPlaying ? getResources().getDrawable(R.drawable.ic_pause_white_24dp) : getResources().getDrawable(R.drawable.ic_play_arrow_white_24dp));

        if (((MyApplication) getApplicationContext()).spotifyUserId == null || !((MyApplication) getApplicationContext()).spotifyUserId.equals(party.hostSpotifyId)) {
            // If not host, hide player controls
            controlsPrev.setVisibility(View.GONE);
            controlsPlayPause.setVisibility(View.GONE);
            controlsNext.setVisibility(View.GONE);
            songProgress.setEnabled(false);
        } else {
            // Else, show controls and set up on click listeners
            controlsPrev.setVisibility(View.VISIBLE);
            controlsPlayPause.setVisibility(View.VISIBLE);
            controlsNext.setVisibility(View.VISIBLE);
            songProgress.setEnabled(true);

            controlsPlayPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    party.isPlaying = !party.isPlaying;
                    controlsPlayPause.setImageDrawable(party.isPlaying ? getResources().getDrawable(R.drawable.ic_pause_white_24dp) : getResources().getDrawable(R.drawable.ic_play_arrow_white_24dp));
                    if (party.isPlaying)
                        mPlayer.resume(null);
                    else if (!party.isPlaying)
                        mPlayer.pause(null);
                    if (mPlayer != null && party.isPlaying)
                        mPlayer.playUri(null, "spotify:track:" + party.queue.get(0).id, 0, party.progress);
                    updateParty();
                }
            });
            controlsPrev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Song last = party.queue.remove(party.queue.size() - 1);
                    party.queue.add(0, last);
                    party.isPlaying = true;
                    party.progress = 0;
                    updateParty();
                    updateSongs();
                    if (mPlayer != null && party.queue.size() > 0)
                        mPlayer.playUri(null, "spotify:track:" + party.queue.get(0).id, 0, party.progress);
                }
            });
            controlsNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Song first = party.queue.remove(0);
                    party.queue.add(first);
                    party.isPlaying = true;
                    party.progress = 0;
                    updateParty();
                    updateSongs();
                    if (mPlayer != null && party.queue.size() > 0)
                        mPlayer.playUri(null, "spotify:track:" + party.queue.get(0).id, 0, party.progress);
                }
            });
            songProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    party.progress = seekBar.getProgress();
                    if (mPlayer != null)
                        mPlayer.seekToPosition(new Player.OperationCallback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Error error) {

                            }
                        }, party.progress);
                    updateParty();
                }
            });
        }

        setAdapter();
    }

    private void setAdapter() {
        if (party.queue == null || recyclerView == null)
            return;

        if (mAdapter == null) {
            mAdapter = new QueueAdapter(this, party.queue);
            recyclerView.setAdapter(mAdapter);
        } else {
            int index = linearLayoutManager.findFirstVisibleItemPosition();
            View v = recyclerView.getChildAt(0);
            int top = (v == null) ? 0 : (v.getTop() - recyclerView.getPaddingTop());
            mAdapter = new QueueAdapter(this, party.queue);
            recyclerView.setAdapter(mAdapter);
            linearLayoutManager.scrollToPositionWithOffset(index, top);
        }
    }

    private void subscribeToParty() {
        if (party == null)
            return;

        final DatabaseReference db = Utils.getDatabase().getReference(MainActivity.FIREBASE_DATABASE_TABLE_PARTIES).child(party.id);
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (party.queue.size() == 0)
                    return;

                Party p = dataSnapshot.getValue(Party.class);
                String prevSong = party.queue.get(0).id;
                party = p;
                updateSongs();
                if (!prevSong.equals(party.queue.get(0).id) && mPlayer != null)
                    mPlayer.playUri(null, "spotify:track:" + party.queue.get(0).id, 0, party.progress);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateParty() {
        party.timestamp = System.currentTimeMillis();
        Utils.getDatabase().getReference(MainActivity.FIREBASE_DATABASE_TABLE_PARTIES).child(party.id).setValue(
                party, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.QueueViewHolder> {

        private ArrayList<Song> mSongs;
        private Context context;

        public QueueAdapter(Context context, ArrayList<Song> mSongs) {
            this.context = context;
            this.mSongs = new ArrayList<>();
            for (int i = 1; i < mSongs.size(); i++)
                this.mSongs.add(mSongs.get(i));
        }

        @Override
        public int getItemCount() {
            return mSongs.size();
        }

        @Override
        public QueueAdapter.QueueViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_song, viewGroup, false);
            QueueAdapter.QueueViewHolder vh = new QueueAdapter.QueueViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(QueueAdapter.QueueViewHolder holder, int position) {
            final Song song = mSongs.get(position);

            if (song.name != null)
                holder.songName.setText(song.name);

            String details = "";
            if (song.artist != null)
                details += song.artist + " | ";
            details += millisToString(song.duration);

            holder.songDetails.setText(details);
        }

        public class QueueViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView songName;
            TextView songDetails;

            QueueViewHolder(final View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);
                songName = (TextView) itemView.findViewById(R.id.tv_queue_song_name);
                songDetails = (TextView) itemView.findViewById(R.id.tv_queue_song_details);
            }

            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();

            }
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d(TAG, "User logged in");

        if (mPlayer != null && party.isPlaying && party.queue.size() > 0)
            mPlayer.playUri(null, "spotify:track:" + party.queue.get(0).id, 0, party.progress);
    }

    @Override
    public void onLoggedOut() {
        Log.d(TAG, "User logged out");
    }

    @Override
    public void onLoginFailed(int i) {
        Log.d(TAG, "onLoginFailed");
    }

    @Override
    public void onTemporaryError() {
        Log.d(TAG, "onTemporaryError");
    }

    @Override
    public void onConnectionMessage(String s) {
        Log.d(TAG, "onConnectionMessage");
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d(TAG, "onPlaybackEvent");
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d(TAG, "onPlaybackError");
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }
}
