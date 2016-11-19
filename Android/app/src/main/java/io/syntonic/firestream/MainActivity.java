package io.syntonic.firestream;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
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

import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.UUID;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class MainActivity extends AppCompatActivity {

    public static final String FIREBASE_DATABASE_TABLE_PARTIES = "parties";

    private static final String TAG = "MainActivity";
    private static final int SPOTIFY_REQUEST_CODE = 1337;
    private static final String SPOTIFY_REDIRECT_URI = "firestream://callback/";

    private ArrayList<Party> parties = new ArrayList<>();
    private PartyAdapter partyAdapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private MaterialProgressBar progressBar;

    private String mSearchQuery;
    private SearchView searchView;
    private SearchView.OnQueryTextListener queryTextListener;
    private TextView emptyListTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Find a Party");

        progressBar = (MaterialProgressBar) findViewById(R.id.progressBar);
        emptyListTextView = (TextView) findViewById(R.id.tv_empty);

        recyclerView = (RecyclerView) findViewById(R.id.party_list);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        subscribeToParties();

        FloatingActionButton createPartyButton = (FloatingActionButton) findViewById(R.id.fab_create_party);
        createPartyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthenticationRequest.Builder builder =
                        new AuthenticationRequest.Builder(getString(R.string.spotify_client_id), AuthenticationResponse.Type.TOKEN, SPOTIFY_REDIRECT_URI);

                builder.setScopes(new String[]{"streaming"});
                AuthenticationRequest request = builder.build();
                AuthenticationClient.openLoginActivity(MainActivity.this, SPOTIFY_REQUEST_CODE, request);
            }
        });

//        addTestParty();
    }

    private void subscribeToParties() {

        parties = new ArrayList<>();

        final DatabaseReference db = Utils.getDatabase().getReference(FIREBASE_DATABASE_TABLE_PARTIES);

        Query q = db.orderByChild("attendees").limitToLast(50);
        q.addChildEventListener((new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Party p = dataSnapshot.getValue(Party.class);
                parties.add(p);
                displayParties();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Party p = dataSnapshot.getValue(Party.class);
                Iterator<Party> i = parties.iterator();
                while (i.hasNext()) {
                    Party thisParty = i.next();
                    if (thisParty.id.equals(p.id)) {
                        i.remove();
                    }
                }
                parties.add(p);
                displayParties();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Party p = dataSnapshot.getValue(Party.class);
                Iterator<Party> i = parties.iterator();
                while (i.hasNext()) {
                    Party thisParty = i.next();
                    if (thisParty.id.equals(p.id)) {
                        i.remove();
                    }
                    displayParties();
                }
            }

            @Override
            public void onChildMoved (DataSnapshot dataSnapshot, String s){

            }

            @Override
            public void onCancelled (DatabaseError databaseError){
                Log.d(TAG, "Database error: " + databaseError.getMessage());
                progressBar.setVisibility(View.GONE);
            }

        }));
    }

    private void displayParties() {
        if (parties.size() > 0) {
            setAdapter();
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
            emptyListTextView.setVisibility(View.VISIBLE);
            if (mSearchQuery == null)
                emptyListTextView.setText("Could not find any Parties. Check your network connection and try again!");
            else
                emptyListTextView.setText("Could not find any Parties that match this search");
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void addTestParty() {
        final Party newParty = new Party("test");
        Utils.getDatabase().getReference(FIREBASE_DATABASE_TABLE_PARTIES).child(newParty.id).setValue(
                newParty, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        Toast.makeText(MainActivity.this, databaseError == null ?
                                "Party Created!" : "Error. Try again later.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setAdapter() {
        if (parties == null || recyclerView == null)
            return;

        if (partyAdapter == null) {
            partyAdapter = new PartyAdapter(this, parties);
            recyclerView.setAdapter(partyAdapter);
        } else {
            int index = linearLayoutManager.findFirstVisibleItemPosition();
            View v = recyclerView.getChildAt(0);
            int top = (v == null) ? 0 : (v.getTop() - recyclerView.getPaddingTop());
            partyAdapter = new PartyAdapter(this, parties);
            recyclerView.setAdapter(partyAdapter);
            linearLayoutManager.scrollToPositionWithOffset(index, top);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_party_list, menu);

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

                    if (mSearchQuery == null || mSearchQuery.isEmpty()) {
                        subscribeToParties();
                    } else {

                        parties = new ArrayList<>();

                        final DatabaseReference db = Utils.getDatabase().getReference(FIREBASE_DATABASE_TABLE_PARTIES);
                        Query q = db.orderByChild("nameLower").startAt(mSearchQuery).endAt(mSearchQuery + '\uf8ff').limitToFirst(50);
                        q.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getChildrenCount() > 0) {
                                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                                    Iterator<DataSnapshot> iterator = children.iterator();
                                    while (iterator.hasNext()) {
                                        Party p = iterator.next().getValue(Party.class);
                                        parties.add(p);
                                    }
                                }
                                displayParties();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d(TAG, "Database error: " + databaseError.getMessage());
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }

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

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == SPOTIFY_REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                case TOKEN:
                    Intent createPartyIntent = new Intent(this, CreatePartyActivity.class);
                    startActivity(createPartyIntent);
                    break;
                case ERROR:
                    Toast.makeText(this, "Hosting a party requires Spotfiy Premium", Toast.LENGTH_LONG).show();
                    break;
                default:
            }
        }
    }

    public class PartyAdapter extends RecyclerView.Adapter<PartyAdapter.PartyViewHolder> {

        private ArrayList<Party> mParties;
        private Context context;

        public PartyAdapter(Context context, ArrayList<Party> parties) {
            this.context = context;
            this.mParties = parties;
        }

        @Override
        public int getItemCount() {
            return mParties.size();
        }

        @Override
        public PartyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_party, viewGroup, false);
            PartyViewHolder vh = new PartyViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(PartyViewHolder holder, int position) {
            final Party party = mParties.get(position);

            Song song = null;
            if (party.queue.size() > 0)
                song = party.queue.get(0);

            if (party.name != null)
                holder.partyName.setText(party.name);

            if (party.hostName != null && song != null) {
                holder.partyDetails.setText("Host: " + party.hostName + " | " + "Now Playing: " + party.queue.get(0).name);
            }

            holder.partyCount.setText(String.valueOf(party.attendees));

            if (song != null)
                Picasso.with(context).load(song.album_url).into(holder.partyCurrentImage);
        }

        public class PartyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView partyName;
            TextView partyDetails;
            TextView partyCount;
            ImageView partyCurrentImage;

            PartyViewHolder(final View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);
                partyName = (TextView) itemView.findViewById(R.id.tv_party_name);
                partyDetails = (TextView) itemView.findViewById(R.id.tv_party_details);
                partyCount = (TextView) itemView.findViewById(R.id.tv_party_count);
                partyCurrentImage = (ImageView) itemView.findViewById(R.id.iv_party_current_album);
            }

            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
            }
        }
    }
}
