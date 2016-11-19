package io.syntonic.firestream;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class CreatePartyActivity extends AppCompatActivity {

    private final static String[] SEED_TYPES = { "Genre", "Artist", "Track" };
    private ArrayList<View> seedViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_party);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Create a Party");

        addSeedView(true);

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
                TextView partyName = (TextView) findViewById(R.id.et_party_name);
                TextView partyPassword = (TextView) findViewById(R.id.et_party_password);

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
                seed_genres = seed_genres.substring(0, seed_genres.length() - 1);
                seed_artists = seed_artists.substring(0, seed_artists.length() - 1);
                seed_tracks = seed_tracks.substring(0, seed_tracks.length() - 1);
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
}
