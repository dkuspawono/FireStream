package io.syntonic.firestream;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static io.syntonic.firestream.Utils.millisToString;

public class SongSearchActivity extends AppCompatActivity {

    private String mSearchQuery;
    private SearchView searchView;
    private SearchView.OnQueryTextListener queryTextListener;

    private ArrayList<Song> songs = new ArrayList<>();
    private ArrayList<String> requestedSongIds = new ArrayList<>();
    private Party party;

    private SearchAdapter mAdapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private MaterialProgressBar progressBar;
    private TextView emptyListTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_search);

        party = getIntent().getParcelableExtra(CreatePartyActivity.EXTRA_KEY_PARTY);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Search");

        progressBar = (MaterialProgressBar) findViewById(R.id.progressBar);
        emptyListTextView = (TextView) findViewById(R.id.tv_empty);

        recyclerView = (RecyclerView) findViewById(R.id.song_list);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        doSearch();
    }

    private void doSearch() {
        songs = new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);

        SpotifyService spotify = ((MyApplication)getApplicationContext()).spotifyApi.getService();

        String query = "";
        if (mSearchQuery == null || mSearchQuery.isEmpty()) {
            String[] opts = getResources().getStringArray(R.array.search_defaults);
            query = opts[new Random().nextInt(5)];
        } else {
            query = mSearchQuery;
        }

        spotify.searchTracks(query, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                progressBar.setVisibility(View.GONE);

                for (int i = 0; i < tracksPager.tracks.items.size(); i++) {
                    Track track = tracksPager.tracks.items.get(i);
                    addTrack(track);
                }

                setAdapter();
            }

            @Override
            public void failure(RetrofitError error) {
                progressBar.setVisibility(View.GONE);
                setAdapter();
            }
        });
    }

    private void addTrack(Track track) {
        String name = track.name;
        if (name == null || name.isEmpty())
            return;

        String id = track.id;
        String artist = "";
        long duration = track.duration_ms;
        for (int a = 0; a < track.artists.size(); a++)
            artist += track.artists.get(a).name + (a == track.artists.size() - 1 ? "" : ", ");

        String image = null;
        if (track.album.images != null && track.album.images.size() > 0)
            image = track.album.images.get(0).url;

        songs.add(new Song(id, name, image, artist, duration));
    }

    private void setAdapter() {
        if (songs == null || recyclerView == null)
            return;

        if (songs.size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyListTextView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyListTextView.setVisibility(View.VISIBLE);
        }

        if (mAdapter == null) {
            mAdapter = new SearchAdapter(this, songs);
            recyclerView.setAdapter(mAdapter);
        } else {
            int index = linearLayoutManager.findFirstVisibleItemPosition();
            View v = recyclerView.getChildAt(0);
            int top = (v == null) ? 0 : (v.getTop() - recyclerView.getPaddingTop());
            mAdapter = new SearchAdapter(this, songs);
            recyclerView.setAdapter(mAdapter);
            linearLayoutManager.scrollToPositionWithOffset(index, top);
        }
    }

    private void updateParty(final Song newSong, final boolean host) {
        Utils.getDatabase().getReference(MainActivity.FIREBASE_DATABASE_TABLE_PARTIES).child(party.id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Party p = dataSnapshot.getValue(Party.class);
                p.timestamp = System.currentTimeMillis();
                if (host)
                    p.queue.add(newSong);
                else
                    p.requests.add(newSong);

                Utils.getDatabase().getReference(MainActivity.FIREBASE_DATABASE_TABLE_PARTIES).child(party.id).setValue(
                        p, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null)
                                    Toast.makeText(SongSearchActivity.this, host ? "Song added" : "Requested song", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_song_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

            AutoCompleteTextView searchTextView = (AutoCompleteTextView) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            try {
                Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
                mCursorDrawableRes.setAccessible(true);
                mCursorDrawableRes.set(searchTextView, R.drawable.cursor);
            } catch (Exception e) {
            }
            queryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    mSearchQuery = (newText != null && newText.length() > 0) ? newText.toLowerCase() : null;
                    doSearch();
                    return true;
                }
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return true;
                }
            };
            searchView.setOnQueryTextListener(queryTextListener);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

        private ArrayList<Song> mSongs;
        private Context context;

        public SearchAdapter(Context context, ArrayList<Song> mSongs) {
            this.context = context;
            this.mSongs = mSongs;
        }

        @Override
        public int getItemCount() {
            return mSongs.size();
        }

        @Override
        public SearchAdapter.SearchViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_song_search, viewGroup, false);
            SearchAdapter.SearchViewHolder vh = new SearchAdapter.SearchViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(SearchAdapter.SearchViewHolder holder, int position) {
            final Song song = mSongs.get(position);

            if (song.name != null)
                holder.songName.setText(song.name);

            String details = "";
            if (song.artist != null)
                details += song.artist + " | ";
            details += millisToString(song.duration);

            holder.songDetails.setText(details);

            if (song.albumUrl != null)
                Picasso.with(context).load(song.albumUrl).into(holder.songArt);

            holder.songRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (((MyApplication)getApplicationContext()).spotifyUserId != null && party.hostSpotifyId != null &&
                            ((MyApplication)getApplicationContext()).spotifyUserId.equals(party.hostSpotifyId)) {
                        updateParty(song, true);
                    } else {
                        updateParty(song, false);
                    }
                }
            });
        }

        public class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView songName;
            TextView songDetails;
            ImageView songArt;
            ImageButton songRequest;

            SearchViewHolder(final View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);
                songName = (TextView) itemView.findViewById(R.id.tv_search_song_name);
                songDetails = (TextView) itemView.findViewById(R.id.tv_search_song_details);
                songArt = (ImageView) itemView.findViewById(R.id.iv_search_song);
                songRequest = (ImageButton) itemView.findViewById(R.id.ib_request_song);
            }

            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
            }
        }
    }
}
