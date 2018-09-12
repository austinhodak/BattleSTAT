package com.respondingio.battlegroundsbuddy.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.ListCallbackSingleChoice;
import com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.respondingio.battlegroundsbuddy.R;
import com.respondingio.battlegroundsbuddy.stats.AddPlayerBottomSheet;
import com.respondingio.battlegroundsbuddy.stats.OLDMainStatsFragment;
import com.respondingio.battlegroundsbuddy.stats.MatchesListFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.mateware.snacky.Snacky;

public class Player implements Parcelable {

    private int kills;

    public Player() {
    }

    public int getKills() {
        return kills;
    }

    public void setKills(final int kills) {
        this.kills = kills;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected Player(Parcel in) {
        in.writeInt(getKills());
    }

    public static final Creator<Player> CREATOR = new Creator<Player>() {
        @Override
        public Player createFromParcel(Parcel in) {
            return new Player(in);
        }

        @Override
        public Player[] newArray(int size) {
            return new Player[size];
        }
    };

    public static class StatsMainActivity extends AppCompatActivity {

        @BindView(R.id.weapon_detail_toolbar)
        Toolbar mToolbar;

        @BindView(R.id.toolbar_title)
        TextView title;

        @BindView(R.id.bottom_navigation) BottomNavigationView mBottomNavigationView;

        @BindView(R.id.select_user_ll) LinearLayout selectUserLL;

        @BindView(R.id.select_region) LinearLayout regionSelect;

        @BindView(R.id.region_text) TextView regionTV;

        @BindView(R.id.select_mode) LinearLayout modeSelect;

        @BindView(R.id.mode_text) TextView modeTV;

        @BindView(R.id.select_seasonTV) TextView seasonTV;

        @BindView(R.id.select_season) LinearLayout seasonSelect;

        @BindView(R.id.outdated_shadow) View outdatedShadow;

        @BindView(R.id.outdated_view) LinearLayout outdatedView;

        Bundle mBundle = new Bundle();

        Map<String, String> players = new HashMap<>();

        List<String> playerList = new ArrayList<>();

        List<String> seasonList = new ArrayList<>();

        private DatabaseReference mDatabase;

        private SharedPreferences mSharedPreferences;

        int selectedRegion = -1;
        int selectedMode = -1;
        int selectedSeason = -1;

        int selectedRegionInt = -1;

        private String selectedPlayerID;

        private final String apiKey = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI5NTYzY2UzMC02MjQ3LTAxMzYtNzUzZi0zNTRhYTk3YWEzOTAiLCJpc3MiOiJnYW1lbG9ja2VyIiwiaWF0IjoxNTMwNzcwODMzLCJwdWIiOiJibHVlaG9sZSIsInRpdGxlIjoicHViZyIsImFwcCI6ImJhdHRsZS1idWRkeS1mb3ItcHViZyJ9.1trk1FWn3jDSFOHV2yIQsMMeOZXKT8zzSmQbGB02AZc";

        public static String[] regions = {"Xbox Asia", "Xbox Europe", "Xbox North America", "Xbox Oceania", "PC Korea", "PC Japan", "PC North America", "PC Europe", "PC Russia", "PC Oceania", "PC Kakao", "PC South East Asia",
                "PC South and Central America", "PC Asia"};

        public static String[] modes = {"Solo TPP", "Solo FPP", "Duo TPP", "Duo FPP", "Squad TPP", "Squad FPP"};

        private Fragment mFragment;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_stats_main);
            ButterKnife.bind(this);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mSharedPreferences = this.getSharedPreferences("com.austinhodak.pubgcenter", Context.MODE_PRIVATE);

            mDatabase = FirebaseDatabase.getInstance().getReference();

            mBottomNavigationView.setOnNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.your_stats_menu:
                            OLDMainStatsFragment yourStatsFragment = new OLDMainStatsFragment();
                            yourStatsFragment.setArguments(mBundle);
                            getSupportFragmentManager().beginTransaction().replace(R.id.home_frame, yourStatsFragment).commit();
                            break;
                        case R.id.matches_menu:
                            MatchesListFragment matchesListFragment = new MatchesListFragment();
                            matchesListFragment.setArguments(mBundle);
                            getSupportFragmentManager().beginTransaction().replace(R.id.home_frame, matchesListFragment).commit();
                            break;
                    }
                    return true;
                }
            });

            if (mSharedPreferences.contains("stats_selected_player") && mSharedPreferences.contains("stats_selected_player_id")) {
                mBundle.putString("player_id", mSharedPreferences.getString("stats_selected_player_id", null));
                mBundle.putString("player_name", mSharedPreferences.getString("stats_selected_player", null));

                selectedPlayerID = mSharedPreferences.getString("stats_selected_player_id", null);

                players.put(mSharedPreferences.getString("stats_selected_player_id", null), mSharedPreferences.getString("stats_selected_player", null));

                ((TextView) selectUserLL.getChildAt(0)).setText(mSharedPreferences.getString("stats_selected_player", "Select Player"));

                selectedRegion = mSharedPreferences.getInt("player_region-" + selectedPlayerID, -1);

                if (selectedRegion != -1) {
                    regionTV.setText(regions[selectedRegion]);
                    mBundle.putInt("region", selectedRegion);

                    if (regions[selectedRegion].contains("PC")) {
                        selectedRegionInt = 1;
                        selectedSeason = Seasons.getInstance().getCurrentSeasonInt(selectedRegionInt);
                    } else {
                        selectedRegionInt = 2;
                        selectedSeason = Seasons.getInstance().getCurrentSeasonInt(selectedRegionInt);
                    }

                    String seasonID = Seasons.getInstance().getSeasonListArray(selectedRegionInt).get(selectedSeason);
                    if (seasonID.contains("Current")) {
                        seasonID = seasonID.replaceAll(" .*", "");
                    }

                    Log.d("REGION", seasonID + " - " + selectedRegion);

                    mBundle.putString("season_id", seasonID);

                    seasonTV.setText(Seasons.getInstance().getSeasonListArray(selectedRegionInt).get(selectedSeason));
                }
            }

            if (mSharedPreferences.contains("selected_gamemode")) {
                selectedMode = mSharedPreferences.getInt("selected_gamemode", -1);
                if (selectedMode != -1) {
                    modeTV.setText(modes[selectedMode]);
                    mBundle.putInt("gamemode", selectedMode);
                }
            }

            loadPlayers();

            mFragment = new OLDMainStatsFragment();
            mFragment.setArguments(mBundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.home_frame, mFragment).commit();

            selectUserLL.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    playerList.clear();

                    int selectedPlayer = -1;

                    for (Map.Entry entry: players.entrySet()) {
                        playerList.add((String) entry.getValue());
                    }

                    if (playerList.contains(((TextView) selectUserLL.getChildAt(0)).getText().toString())) {
                        selectedPlayer = playerList.indexOf(((TextView) selectUserLL.getChildAt(0)).getText().toString());
                    }

                    new MaterialDialog.Builder(StatsMainActivity.this)
                            .title("Your Players")
                            .items(playerList)
                            .neutralText("Link new")
                            .positiveText("One Time Search")
                            .alwaysCallSingleChoiceCallback()
                            .itemsCallbackSingleChoice(selectedPlayer, new ListCallbackSingleChoice() {
                                @Override
                                public boolean onSelection(final MaterialDialog dialog, final View itemView, final int which, final CharSequence text) {
                                    ((TextView) selectUserLL.getChildAt(0)).setText(text);

                                    String playerID = "";

                                    for (Map.Entry entry: players.entrySet()) {
                                        if (text.equals(entry.getValue())) {
                                            playerID = (String) entry.getKey();
                                            mSharedPreferences.edit().putString("stats_selected_player", String.valueOf(text)).apply();
                                            mSharedPreferences.edit().putString("stats_selected_player_id", String.valueOf(entry.getKey())).apply();
                                            break;
                                        }
                                    }

                                    mBundle.putString("player_id", playerID);
                                    mBundle.putString("player_name", String.valueOf(text));

                                    selectedRegion = mSharedPreferences.getInt("player_region-" + playerID, -1);

                                    if (selectedRegion != -1) {
                                        regionTV.setText(regions[selectedRegion]);
                                        mBundle.putInt("region", selectedRegion);

                                        if (regions[selectedRegion].contains("PC")) {
                                            selectedRegionInt = 1;
                                            selectedSeason = Seasons.getInstance().getCurrentSeasonInt(selectedRegionInt);
                                        } else {
                                            selectedRegionInt = 2;
                                            selectedSeason = Seasons.getInstance().getCurrentSeasonInt(selectedRegionInt);
                                        }

                                        String seasonID = Seasons.getInstance().getSeasonListArray(selectedRegionInt).get(selectedSeason);
                                        if (seasonID.contains("Current")) {
                                            seasonID = seasonID.replaceAll(" .*", "");
                                        }

                                        mBundle.putString("season_id", seasonID);

                                        seasonTV.setText(Seasons.getInstance().getSeasonListArray(selectedRegionInt).get(selectedSeason));
                                    }

                                    dialog.dismiss();

                                    reloadFragments();
                                    return true;
                                }
                            })
                            .onNeutral(new SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {
                                    AddPlayerBottomSheet addPlayerBottomSheet = new AddPlayerBottomSheet();
                                    addPlayerBottomSheet.show(getSupportFragmentManager(), addPlayerBottomSheet.getTag());
                                }
                            })
                            .show();
                }
            });

            regionSelect.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    new MaterialDialog.Builder(StatsMainActivity.this)
                            .title("Select Region")
                            .items("Xbox Asia", "Xbox Europe", "Xbox North America", "Xbox Oceania", "PC Korea", "PC Japan", "PC North America", "PC Europe", "PC Russia", "PC Oceania", "PC Kakao", "PC South East Asia",
                                    "PC South and Central America", "PC Asia")
                            .itemsCallbackSingleChoice(selectedRegion, new ListCallbackSingleChoice() {
                                @Override
                                public boolean onSelection(final MaterialDialog dialog, final View itemView, final int which, final CharSequence text) {
                                    selectedRegion = which;
                                    regionTV.setText(text);
                                    mBundle.putInt("region", which);

                                    mSharedPreferences.edit().putInt("selected_region", selectedRegion).apply();

                                    if (regions[selectedRegion].contains("PC")) {
                                        selectedRegionInt = 1;
                                        selectedSeason = Seasons.getInstance().getCurrentSeasonInt(selectedRegionInt);
                                    } else {
                                        selectedRegionInt = 2;
                                        selectedSeason = Seasons.getInstance().getCurrentSeasonInt(selectedRegionInt);
                                    }

                                    String seasonID = Seasons.getInstance().getSeasonListArray(selectedRegionInt).get(selectedSeason);
                                    if (seasonID.contains("Current")) {
                                        seasonID = seasonID.replaceAll(" .*", "");
                                    }

                                    mBundle.putString("season_id", seasonID);

                                    seasonTV.setText(Seasons.getInstance().getSeasonListArray(selectedRegionInt).get(selectedSeason));

                                    reloadFragments();
                                    return false;
                                }
                            })
                            .show();
                }
            });

            modeSelect.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    new MaterialDialog.Builder(StatsMainActivity.this)
                            .title("Select Gamemode")
                            .items("Solo TPP", "Solo FPP", "Duo TPP", "Duo FPP", "Squad TPP", "Squad FPP")
                            .itemsCallbackSingleChoice(selectedMode, new ListCallbackSingleChoice() {
                                @Override
                                public boolean onSelection(final MaterialDialog dialog, final View itemView, final int which, final CharSequence text) {
                                    selectedMode = which;
                                    modeTV.setText(text);
                                    mBundle.putInt("gamemode", which);

                                    mSharedPreferences.edit().putInt("selected_gamemode", selectedMode).apply();

                                    reloadFragments();
                                    return false;
                                }
                            })
                            .show();
                }
            });

            seasonSelect.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    new MaterialDialog.Builder(StatsMainActivity.this)
                            .title("Select Season (" + regionTV.getText() + ")")
                            .items(Seasons.getInstance().getSeasonListArray(selectedRegionInt))
                            .itemsCallbackSingleChoice(selectedSeason, new ListCallbackSingleChoice() {
                                @Override
                                public boolean onSelection(final MaterialDialog dialog, final View itemView, final int which, final CharSequence text) {
                                    selectedSeason = which;
                                    String seasonID = text.toString();
                                    if (seasonID.contains("Current")) {
                                        seasonID = seasonID.replaceAll(" .*", "");
                                    }
                                    mBundle.putString("season_id", seasonID);

                                    seasonTV.setText(Seasons.getInstance().getSeasonListArray(selectedRegionInt).get(selectedSeason));

                                    reloadFragments();
                                    return false;
                                }
                            })
                            .show();
                }
            });

        }

        private void loadSeasons() {
            seasonList.clear();
            String season;
            if (AddPlayerBottomSheet.regionList[selectedRegion].contains("XBOX")) {
                season = "xbox-na";
            } else {
                season = "pc-na";
            }

            Log.d("SEASON", season + " - "+ AddPlayerBottomSheet.regionList[selectedRegion]);

            mDatabase.child("seasons").child(season).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if ((boolean) child.getValue()) {
                            seasonList.add(child.getKey() + " (Current)");
                            selectedSeason = seasonList.indexOf(child.getKey() + " (Current)");
                        } else {
                            seasonList.add(child.getKey());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull final DatabaseError databaseError) {

                }
            });
        }

        private void reloadFragments() {
            OLDMainStatsFragment yourStatsFragment = new OLDMainStatsFragment();
            switch (mBottomNavigationView.getSelectedItemId()) {
                case R.id.your_stats_menu:
                    yourStatsFragment.setArguments(mBundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.home_frame, yourStatsFragment).commit();
                    break;
                case R.id.matches_menu:
                    MatchesListFragment matchesListFragment = new MatchesListFragment();
                    matchesListFragment.setArguments(mBundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.home_frame, matchesListFragment).commit();
                    break;
                    default:
                        yourStatsFragment.setArguments(mBundle);
                        getSupportFragmentManager().beginTransaction().replace(R.id.home_frame, yourStatsFragment).commit();
            }
        }


        private void loadPlayers() {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                Snacky.builder().setActivity(this).error().setText("You must be logged in to use this feature.").setDuration(Snacky.LENGTH_INDEFINITE).show();
                return;
            }
            mDatabase.child("users").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild("pubg_player")) {

                        mSharedPreferences.edit().remove("stats_selected_player").remove("stats_selected_player_id").apply();
                        //User hasn't linked his player.
                        Snacky.builder().setView(mBottomNavigationView).warning().setText("You haven't linked your PUBG account yet.").setAction("LINK",
                                new OnClickListener() {
                                    @Override
                                    public void onClick(final View v) {
                                        AddPlayerBottomSheet addPlayerBottomSheet = new AddPlayerBottomSheet();
                                        addPlayerBottomSheet.show(getSupportFragmentManager(), addPlayerBottomSheet.getTag());
                                    }
                                }).setDuration(Snacky.LENGTH_INDEFINITE).show();

                        Log.d("STATS", "User hasn't linked account yet");
                    }
                }

                @Override
                public void onCancelled(@NonNull final DatabaseError databaseError) {

                }
            });
            mDatabase.child("users").child(firebaseUser.getUid()).child("pubg_player").orderByChild("playerName").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable final String s) {

                    String shard_id = dataSnapshot.child("shardId").getValue().toString().toUpperCase();
                    selectedRegion = Arrays.asList(AddPlayerBottomSheet.regionList).indexOf(shard_id);
                    mSharedPreferences.edit().putInt("player_region-" + dataSnapshot.getKey(), selectedRegion).apply();

                    if (players.containsKey(dataSnapshot.getKey())) return;

                    players.put(dataSnapshot.getKey(), dataSnapshot.child("playerName").getValue().toString());

                    if (!mSharedPreferences.contains("stats_selected_player") || !mSharedPreferences.contains("stats_selected_player_id")) {

                        mBundle.putString("player_id", dataSnapshot.getKey());
                        mBundle.putString("player_name", dataSnapshot.child("playerName").getValue().toString());

                        mSharedPreferences.edit().putString("stats_selected_player", dataSnapshot.child("playerName").getValue().toString()).apply();
                        mSharedPreferences.edit().putString("stats_selected_player_id", dataSnapshot.getKey()).apply();

                        ((TextView) selectUserLL.getChildAt(0)).setText(dataSnapshot.child("playerName").getValue().toString());

                        mSharedPreferences.edit().putInt("selected_gamemode", selectedMode).apply();

                        mSharedPreferences.edit().putInt("selected_region", selectedRegion).apply();
                        regionTV.setText(regions[selectedRegion]);
                        mBundle.putInt("region", selectedRegion);

                        if (shard_id.contains("pc")) {
                            selectedRegionInt = 1;
                        } else {
                            selectedRegionInt = 2;
                        }

                        reloadFragments();
                    } else {

                    }
                }

                @Override
                public void onChildChanged(@NonNull final DataSnapshot dataSnapshot, @Nullable final String s) {

                }

                @Override
                public void onChildRemoved(@NonNull final DataSnapshot dataSnapshot) {
                    players.remove(dataSnapshot.getKey());
                }

                @Override
                public void onChildMoved(@NonNull final DataSnapshot dataSnapshot, @Nullable final String s) {

                }

                @Override
                public void onCancelled(@NonNull final DatabaseError databaseError) {

                }
            });
        }

        public void showOutdated() {
            outdatedView.setVisibility(View.VISIBLE);
            outdatedShadow.setVisibility(View.VISIBLE);
        }

        public void hideOutdated() {
            outdatedView.setVisibility(View.GONE);
            outdatedShadow.setVisibility(View.GONE);
        }

        @Override
        public boolean onCreateOptionsMenu(final Menu menu) {
            return super.onCreateOptionsMenu(menu);
        }

        @Override
        public boolean onSupportNavigateUp() {
            onBackPressed();
            return true;
        }
    }
}
