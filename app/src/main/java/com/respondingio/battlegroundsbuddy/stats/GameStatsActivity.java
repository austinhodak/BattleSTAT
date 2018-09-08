package com.respondingio.battlegroundsbuddy.stats;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.ListCallbackSingleChoice;
import com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter.Callback;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.respondingio.battlegroundsbuddy.R;
import com.respondingio.battlegroundsbuddy.models.Seasons;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.mateware.snacky.Snacky;

public class GameStatsActivity extends AppCompatActivity {

    @BindView(R.id.bottom_navigation) BottomNavigationView mBottomNavigationView;

    @BindView(R.id.weapon_detail_toolbar)
    Toolbar mToolbar;

    final MaterialSimpleListAdapter playerAdapter = new MaterialSimpleListAdapter(new Callback() {
        @Override
        public void onMaterialListItemSelected(final MaterialDialog dialog, final int index, final MaterialSimpleListItem item) {
            String playerName = (String) item.getContent();
            String playerID = players.get(playerName);

            ((TextView) selectUserLL.getChildAt(0)).setText(playerName);

            mSharedPreferences.edit().putString("stats_selected_player", playerName).apply();
            mSharedPreferences.edit().putString("stats_selected_player_id", playerID).apply();

            mBundle.putString("player_id", playerID);
            mBundle.putString("player_name", playerName);

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
        }
    });

    @BindView(R.id.select_user_ll) LinearLayout selectUserLL;

    @BindView(R.id.toolbar_title)
    TextView title;

    @BindView(R.id.region_text) TextView regionTV;

    @BindView(R.id.select_mode) LinearLayout modeSelect;

    @BindView(R.id.mode_text) TextView modeTV;

    @BindView(R.id.select_seasonTV) TextView seasonTV;

    @BindView(R.id.select_season) LinearLayout seasonSelect;

    @BindView(R.id.outdated_shadow) View outdatedShadow;

    @BindView(R.id.outdated_view) LinearLayout outdatedView;

    @BindView(R.id.select_region) LinearLayout regionSelect;

    private DatabaseReference mDatabase;

    private Fragment mFragment;

    private SharedPreferences mSharedPreferences;

    private Bundle mBundle = new Bundle();

    Map<String, String> players = new HashMap<>();


    int selectedRegion = -1;
    int selectedMode = -1;
    int selectedSeason = -1;

    int selectedRegionInt = -1;

    private String selectedPlayerID;

    public static String[] regions = {"Xbox Asia", "Xbox Europe", "Xbox North America", "Xbox Oceania", "PC Korea", "PC Japan", "PC North America", "PC Europe", "PC Russia", "PC Oceania", "PC Kakao", "PC South East Asia",
            "PC South and Central America", "PC Asia"};

    public static String[] modes = {"Solo TPP", "Solo FPP", "Duo TPP", "Duo FPP", "Squad TPP", "Squad FPP"};

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSharedPreferences = this.getSharedPreferences("com.austinhodak.pubgcenter", Context.MODE_PRIVATE);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Snacky.builder().setActivity(this).error().setDuration(Snacky.LENGTH_INDEFINITE).setText("You must be logged in.")
                    .setAction("LOGIN", new OnClickListener() {
                        @Override
                        public void onClick(final View v) {

                        }
                    }).show();
            return;
        }

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

        loadPlayers();

        selectUserLL.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                new MaterialDialog.Builder(GameStatsActivity.this)
                        .title("Select Player")
                        .adapter(playerAdapter, null)
                        .neutralText("LINK NEW")
                        .positiveText("One Time Search")
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

        seasonSelect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                new MaterialDialog.Builder(GameStatsActivity.this)
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

        regionSelect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                new MaterialDialog.Builder(GameStatsActivity.this)
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
                new MaterialDialog.Builder(GameStatsActivity.this)
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

        if (mSharedPreferences.contains("selected_gamemode")) {
            selectedMode = mSharedPreferences.getInt("selected_gamemode", -1);
            if (selectedMode != -1) {
                modeTV.setText(modes[selectedMode]);
                mBundle.putInt("gamemode", selectedMode);
            }
        }

        mBottomNavigationView.setOnNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.your_stats_menu:
                        NewStatsFragment yourStatsFragment = new NewStatsFragment();
                        yourStatsFragment.setArguments(mBundle);
                        getSupportFragmentManager().beginTransaction().replace(R.id.home_frame, yourStatsFragment).commit();
                        break;
                    case R.id.matches_menu:
                        MatchesListFragment matchesListFragment = new MatchesListFragment();
                        matchesListFragment.setArguments(mBundle);
                        getSupportFragmentManager().beginTransaction().replace(R.id.home_frame, matchesListFragment).commit();
                        break;
//                    case R.id.stats_more:
//                        MoreFragment moreFragment = new MoreFragment();
//                        moreFragment.setArguments(mBundle);
//                        getSupportFragmentManager().beginTransaction().replace(R.id.home_frame, moreFragment).commit();
                }
                return true;
            }
        });

        reloadFragments();
    }

    private void loadPlayers() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference ref = mDatabase.child("users").child(currentUser.getUid()).child("pubg_players");

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onCancelled(@NonNull final DatabaseError databaseError) {

            }

            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable final String s) {
                String shardID = dataSnapshot.child("shardID").getValue().toString().toUpperCase();
                String playerName = dataSnapshot.child("playerName").getValue().toString();

                selectedRegion = Arrays.asList(AddPlayerBottomSheet.regionList).indexOf(shardID);
                mSharedPreferences.edit().putInt("player_region-" + dataSnapshot.getKey(), selectedRegion).apply();

                if (players.containsKey(playerName)) return;

                int iconDrawable;
                if (shardID.contains("PC")) {
                    iconDrawable = R.drawable.windows_color;
                } else {
                    iconDrawable = R.drawable.xbox_logo;
                }

                playerAdapter.add(new MaterialSimpleListItem.Builder(GameStatsActivity.this)
                        .content(playerName)
                        .icon(iconDrawable)
                        .iconPaddingDp(8)
                        .backgroundColor(Color.WHITE)
                        .build());

                players.put(playerName, dataSnapshot.getKey());

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

                    if (shardID.contains("PC")) {
                        selectedRegionInt = 1;
                    } else {
                        selectedRegionInt = 2;
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull final DataSnapshot dataSnapshot, @Nullable final String s) {

            }

            @Override
            public void onChildMoved(@NonNull final DataSnapshot dataSnapshot, @Nullable final String s) {

            }

            @Override
            public void onChildRemoved(@NonNull final DataSnapshot dataSnapshot) {

            }
        });
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onCancelled(@NonNull final DatabaseError databaseError) {

            }

            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Snacky.builder().setView(mBottomNavigationView).warning().setText("You haven't linked your PUBG account yet.").setAction("LINK",
                            new OnClickListener() {
                                @Override
                                public void onClick(final View v) {
                                    AddPlayerBottomSheet addPlayerBottomSheet = new AddPlayerBottomSheet();
                                    addPlayerBottomSheet.show(getSupportFragmentManager(), addPlayerBottomSheet.getTag());
                                }
                            }).setDuration(Snacky.LENGTH_INDEFINITE).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void reloadFragments() {
        NewStatsFragment yourStatsFragment = new NewStatsFragment();
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
//            case R.id.stats_more:
//                MoreFragment moreFragment = new MoreFragment();
//                moreFragment.setArguments(mBundle);
//                getSupportFragmentManager().beginTransaction().replace(R.id.home_frame, moreFragment).commit();
            default:
                yourStatsFragment.setArguments(mBundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.home_frame, yourStatsFragment).commit();
        }
    }
}
