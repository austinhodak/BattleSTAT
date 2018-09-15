package com.respondingio.battlegroundsbuddy.stats;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.CardView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.respondingio.battlegroundsbuddy.R;
import com.respondingio.battlegroundsbuddy.models.Player;
import com.respondingio.battlegroundsbuddy.models.Player.StatsMainActivity;
import de.mateware.snacky.Snacky;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public class OLDMainStatsFragment extends Fragment {


    private SharedPreferences mSharedPreferences;

    private CardView modeSelector;

    private TextView modeSelectorText;

    @BindView(R.id.stats_deaths) TextView deathsTV;
    @BindView(R.id.stats_kd) TextView kdTV;
    @BindView(R.id.stats_headshots) TextView headshotKillsTV;
    @BindView(R.id.stats_assists) TextView assistsTV;
    @BindView(R.id.stats_roadKills) TextView roadKillsTV;
    @BindView(R.id.stats_dbnos) TextView DBNOsTV;
    @BindView(R.id.stats_longestKill) TextView longestKillTV;
    @BindView(R.id.stats_teamKills) TextView teamKillsTV;
    @BindView(R.id.stats_killStreak) TextView killStreakTV;
    @BindView(R.id.stats_heals) TextView healsTV;
    @BindView(R.id.stats_damageDealt) TextView damageDealtTV;

    @BindView(R.id.stats_wins) TextView winsTV;
    @BindView(R.id.stats_losses) TextView lossesTV;
    @BindView(R.id.stats_winloss) TextView winlossTV;
    @BindView(R.id.stats_roundsplayed) TextView roundsPlayedTV;
    @BindView(R.id.stats_top10s) TextView top10sTV;
    @BindView(R.id.stats_mostkills) TextView mostKills;
    @BindView(R.id.stats_revives) TextView revivesTV;
    @BindView(R.id.stats_time) TextView timeSurvived;
    @BindView(R.id.stats_suicides) TextView suicidesTV;

    @BindView(R.id.stats_rideDist) TextView rideDistanceTV;
    @BindView(R.id.stats_walkDist) TextView walkDistanceTV;

    @BindView(R.id.stats_boosts) TextView boostsTV;
    @BindView(R.id.stats_killPoints) TextView killPointsTV;
    @BindView(R.id.stats_longestSurv) TextView longestSurvTV;
    @BindView(R.id.stats_weaponsAqd) TextView weaponsAquiredTV;
    @BindView(R.id.stats_weeklyPoints) TextView weeklyPointsTV;
    @BindView(R.id.stats_winPoints) TextView winPointsTV;
    @BindView(R.id.stats_vehicleDestroy) TextView vehicleDestroysTV;

    ProgressBar mProgressBar;

    DatabaseReference mDatabase;

    private RequestQueue queue;

    private SwipeRefreshLayout swipeRefreshLayout;

    private ValueEventListener value;

    private View view;

    public OLDMainStatsFragment() {
        // Required empty public constructor
    }

    String playerName;
    String playerID;
    String region;
    String gamemode;
    String seasonShort = "2018-07";
    String seasonLong = "division.bro.official.2018-06";

    Player mPlayer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_your_stats, container, false);

        ButterKnife.bind(this, view);

        if (getArguments() == null) {
            return null;
        }

        mSharedPreferences = getActivity().getSharedPreferences("com.austinhodak.pubgcenter", Context.MODE_PRIVATE);

        queue = Volley.newRequestQueue(getActivity());

        mDatabase = FirebaseDatabase.getInstance().getReference();

        playerName = getArguments().getString("player_name");
        playerID = getArguments().getString("player_id");
        //region = AddPlayerBottomSheet.Companion.getRegionList()[getArguments().getInt("region")].toLowerCase();
        //gamemode = AddPlayerBottomSheet.Companion.getModesList()[getArguments().getInt("gamemode")];
        seasonLong = "division.bro.official." + getArguments().getString("season_id");
        seasonShort = getArguments().getString("season_id");

        Log.d("STATS SEASON", region);

        if (playerID != null)
        loadPlayerStats(playerID);

        swipeRefreshLayout.setColorSchemeResources(R.color.md_orange_500, R.color.md_pink_500);
        swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                mDatabase.child("users_stats").child(playerID).child("season_data").child(region).child(seasonShort).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            Toast.makeText(getActivity(), region + " - " + seasonShort + " Doesnt exist", Toast.LENGTH_SHORT).show();
                            //refreshData(playerID);
                            return;
                        }

                        if (getTimeSinceLastUpdated(dataSnapshot.child("last_updated").getValue().toString()) > 15 * 60 * 1000) {
                            Log.d("Stats", "Pull refresh: time since last updated greater than 15 min. Refreshing.");
                            refreshData(playerID);
                        } else {
                            updateStats(dataSnapshot, null);
                            swipeRefreshLayout.setRefreshing(false);
                            if (getActivity() != null)
                            Snacky.builder().setActivity(getActivity()).setBackgroundColor(Color.parseColor("#3F51B5")).setText("You can refresh once every 15 minutes.").setActionClickListener(
                                    new OnClickListener() {
                                        @Override
                                        public void onClick(final View v) {
                                            refreshData(playerID);
                                        }
                                    }).setActionText("UPGRADE").setDuration(5000).setActionTextColor(Color.WHITE).build().show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull final DatabaseError databaseError) {

                    }
                });
            }
        });

        return view;
    }



    private void loadPlayerStats(final String playerID) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase.child("users_stats").child(playerID).child("season_data").child(region).child(seasonShort).addValueEventListener(value = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    refreshData(playerID);
                    return;
                }

                StatsMainActivity activity = (StatsMainActivity) getActivity();

                if (getTimeSinceLastUpdated(dataSnapshot.child("last_updated").getValue().toString()) > 15 * 60 * 1000) {
                    if (activity != null) {
                        activity.showOutdated();
                    }
                    updateStats(dataSnapshot, null);
                } else {
                    if (activity != null) {
                        activity.hideOutdated();
                    }
                    updateStats(dataSnapshot, null);
                }

                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull final DatabaseError databaseError) {

            }
        });


//        mDatabase.child("users_stats").child(playerID).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
//                if (!dataSnapshot.exists()) {
//                    refreshData(playerID);
//                    return;
//                }
//
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                try {
//                    Date date1 = simpleDateFormat.parse(dataSnapshot.child("last_updated").getValue().toString());
//                    Date date2 = new Date();
//
//                    long different = date2.getTime() - date1.getTime();
//
//                    if (different < 900000 && dataSnapshot.hasChild("season_data/"+seasonShort)) {
//                        updateStats(dataSnapshot, null);
//                    } else {
//                        refreshData(playerID);
//                    }
//
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull final DatabaseError databaseError) {
//
//            }
//        });
    }

    private void refreshData(final String playerID) {
        swipeRefreshLayout.setRefreshing(true);
        //mProgressBar.setVisibility(View.VISIBLE);
        String url = "https://api.playbattlegrounds.com/shards/" + region + "/players/account." + playerID + "/seasons/" + seasonLong;
        Log.d("URL", url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        saveToDB(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("RESPONSE", "Error: " + error
                                + "\nStatus Code " + error.networkResponse.statusCode
                                + "\nResponse Data " + error.networkResponse.data
                                + "\nCause " + error.getCause()
                                + "\nmessage" + error.getMessage());

                        if (error.networkResponse.statusCode == 429) {
                            if (getActivity() != null)
                            Snacky.builder().setActivity(getActivity()).setText("Too many requests, try again in a minute.").setDuration(Snacky.LENGTH_LONG).warning().show();
                        }

                        swipeRefreshLayout.setRefreshing(false);

                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Accept", "application/vnd.api+json");
                params.put("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI5NTYzY2UzMC02MjQ3LTAxMzYtNzUzZi0zNTRhYTk3YWEzOTAiLCJpc3MiOiJnYW1lbG9ja2VyIiwiaWF0IjoxNTMwNzcwODMzLCJwdWIiOiJibHVlaG9sZSIsInRpdGxlIjoicHViZyIsImFwcCI6ImJhdHRsZS1idWRkeS1mb3ItcHViZyJ9.1trk1FWn3jDSFOHV2yIQsMMeOZXKT8zzSmQbGB02AZc");
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        queue.add(jsonObjectRequest);
    }

    private void saveToDB(final JSONObject response) {
        Date d = new Date();
        CharSequence s = DateFormat.format("yyyy-MM-dd HH:mm:ss", d.getTime());

        Map<String, Object> childUpdates = new HashMap<>();
        //childUpdates.put("/users_stats/" + playerID + "/season_data/2018-05", response);
        childUpdates.put("/users_stats/" + playerID + "/season_data/" + region + "/" + seasonShort + "/last_updated", s);

        mDatabase.updateChildren(childUpdates);

        Map<String, Object> jsonMap = new Gson().fromJson(response.toString(), new TypeToken<HashMap<String, Object>>() {}.getType());
        mDatabase.child("/users_stats/" + playerID + "/season_data/" + region + "/" + seasonShort).updateChildren(jsonMap);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                //TODO your background code
                try {
                    if (response.getJSONObject("data").getJSONObject("relationships").has("matchesDuoFPP")) {
                        JSONArray matchesArray = response.getJSONObject("data").getJSONObject("relationships").getJSONObject("matchesDuoFPP").getJSONArray("data");
                        for (int i = 0; i < matchesArray.length(); i++) {
                            JSONObject matchObj = matchesArray.getJSONObject(i);
                            saveMatchData(matchObj, "matchesDuoFPP");
                        }
                    }
                    if (response.getJSONObject("data").getJSONObject("relationships").has("matchesDuo")) {
                        JSONArray matchesArray = response.getJSONObject("data").getJSONObject("relationships").getJSONObject("matchesDuo").getJSONArray("data");
                        for (int i = 0; i < matchesArray.length(); i++) {
                            JSONObject matchObj = matchesArray.getJSONObject(i);
                            saveMatchData(matchObj, "matchesDuo");
                        }
                    }

                    if (response.getJSONObject("data").getJSONObject("relationships").has("matchesSoloFPP")) {
                        JSONArray matchesArray = response.getJSONObject("data").getJSONObject("relationships").getJSONObject("matchesSoloFPP").getJSONArray("data");
                        for (int i = 0; i < matchesArray.length(); i++) {
                            JSONObject matchObj = matchesArray.getJSONObject(i);
                            saveMatchData(matchObj, "matchesSoloFPP");
                        }
                    }
                    if (response.getJSONObject("data").getJSONObject("relationships").has("matchesSoloFPP")) {
                        JSONArray matchesArray = response.getJSONObject("data").getJSONObject("relationships").getJSONObject("matchesSoloFPP").getJSONArray("data");
                        for (int i = 0; i < matchesArray.length(); i++) {
                            JSONObject matchObj = matchesArray.getJSONObject(i);
                            saveMatchData(matchObj, "matchesSoloFPP");
                        }
                    }

                    if (response.getJSONObject("data").getJSONObject("relationships").has("matchesSquad")) {
                        JSONArray matchesArray = response.getJSONObject("data").getJSONObject("relationships").getJSONObject("matchesSquad").getJSONArray("data");
                        for (int i = 0; i < matchesArray.length(); i++) {
                            JSONObject matchObj = matchesArray.getJSONObject(i);
                            saveMatchData(matchObj, "matchesSquad");
                        }
                    }
                    if (response.getJSONObject("data").getJSONObject("relationships").has("matchesSquadFPP")) {
                        JSONArray matchesArray = response.getJSONObject("data").getJSONObject("relationships").getJSONObject("matchesSquadFPP").getJSONArray("data");
                        for (int i = 0; i < matchesArray.length(); i++) {
                            JSONObject matchObj = matchesArray.getJSONObject(i);
                            saveMatchData(matchObj, "matchesSquadFPP");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void saveMatchData(final JSONObject matchObj, final String gammode) throws JSONException {
        mDatabase.child("match_data").child(matchObj.getString("id")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> childUpdates = new HashMap<>();
                    try {
                        childUpdates.put("/users_stats/" + playerID + "/matches/" + region + "/" + seasonShort + "/" + gammode + "/" + matchObj.getString("id"), dataSnapshot.child("data/attributes/createdAt").getValue().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    mDatabase.updateChildren(childUpdates);
                    return;
                }

                String url = null;
                try {
                    url = "https://api.playbattlegrounds.com/shards/" + region + "/matches/" + matchObj.getString("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(final JSONObject response) {
                                try {

                                    Map<String, Object> jsonMap = new Gson().fromJson(response.toString(), new TypeToken<HashMap<String, Object>>() {}.getType());
                                    mDatabase.child("/match_data/" + matchObj.getString("id")).updateChildren(jsonMap).addOnSuccessListener(
                                            new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(final Void aVoid) {
                                                    Map<String, Object> childUpdates = new HashMap<>();
                                                    try {
                                                        childUpdates.put("/users_stats/" + playerID + "/matches/" + region + "/" + seasonShort + "/" + gammode + "/" + matchObj.getString("id"), response.getJSONObject("data").getJSONObject("attributes").getString("createdAt"));
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }

                                                    mDatabase.updateChildren(childUpdates);
                                                }
                                            });
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("RESPONSE", "Error: " + error
                                        + "\nStatus Code " + error.networkResponse.statusCode
                                        + "\nResponse Data " + error.networkResponse.data
                                        + "\nCause " + error.getCause()
                                        + "\nmessage" + error.getMessage());

                            }
                        }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String>  params = new HashMap<String, String>();
                        params.put("Accept", "application/vnd.api+json");
                        return params;
                    }

                    @Override
                    public String getBodyContentType() {
                        return "application/json";
                    }
                };
                queue.add(jsonObjectRequest);

            }

            @Override
            public void onCancelled(@NonNull final DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey("last_data")) {
            mPlayer = savedInstanceState.getParcelable("last_data");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPlayer != null)
        outState.putParcelable("last_data", mPlayer);
    }

    private void updateStats(DataSnapshot dataSnapshot, JSONObject jsonObject) {

        if (mPlayer == null) {
            ///mPlayer = new Player();
        }

        if (!dataSnapshot.hasChild("/data/attributes/gameModeStats")) {
            mDatabase.child("users_stats").child(playerID).child("season_data").child(region).child(seasonShort).removeEventListener(value);
            loadPlayerStats(playerID);
            return;
        }

        TextView killsTV = view.findViewById(R.id.stats_kills);

        String kills = "--";
        String deaths = "--";
        String headKills = "--";
        String assists = "--";
        String roadKills = "--";
        String DBNOs = "--";
        String longestKill = "--";
        String teamKills = "--";
        String killStreak = "--";
        String heals = "--";
        String damageDealt = "--";

        String wins = "--";
        String losses = "--";
        String roundsplayed = "--";
        String top10s = "--";
        String mostKills1 = "--";
        String revives = "--";
        String timeSurv = "--";
        String suicides = "--";

        String rideDistance = "--";
        String walkDistance = "--";

        String boosts = "--";
        String killPoints = "--";
        String longestTimeSurv = "--";
        String roundMostKill = "--";
        String weaponsAquired = "--";
        String weeklyKills = "--";
        String winPoints = "--";
        String vehicleDestroys = "--";

        if (dataSnapshot != null) {
            kills = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("kills").getValue().toString();
            deaths = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("losses").getValue().toString();
            headKills = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("headshotKills").getValue().toString();
            assists = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("assists").getValue().toString();
            roadKills = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("roadKills").getValue().toString();
            DBNOs = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("dBNOs").getValue().toString();
            longestKill = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("longestKill").getValue().toString();
            teamKills = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("teamKills").getValue().toString();
            killStreak = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("maxKillStreaks").getValue().toString();
            heals = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("heals").getValue().toString();
            damageDealt = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("damageDealt").getValue().toString();

            wins = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("wins").getValue().toString();
            losses = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("losses").getValue().toString();
            roundsplayed = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("roundsPlayed").getValue().toString();
            top10s = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("top10s").getValue().toString();
            mostKills1 = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("roundMostKills").getValue().toString();
            revives = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("revives").getValue().toString();
            timeSurv = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("timeSurvived").getValue().toString();
            suicides = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("suicides").getValue().toString();

            rideDistance = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("rideDistance").getValue().toString();
            walkDistance = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("walkDistance").getValue().toString();

            boosts = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("boosts").getValue().toString();
            killPoints = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("killPoints").getValue().toString();
            longestTimeSurv = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("longestTimeSurvived").getValue().toString();
            roundMostKill = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("roundMostKills").getValue().toString();
            weaponsAquired = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("weaponsAcquired").getValue().toString();
            weeklyKills = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("weeklyKills").getValue().toString();
            winPoints = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("winPoints").getValue().toString();
            vehicleDestroys = dataSnapshot.child("/data/attributes/gameModeStats").child(gamemode).child("vehicleDestroys").getValue().toString();
        }

        /*else if (jsonObject != null) {
            kills = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("kills");
            deaths = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("losses");
            headKills = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("headshotKills");
            assists = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("assists");
            roadKills = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("roadKills");
            DBNOs = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("dBNOs");
            longestKill = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("longestKill");
            teamKills = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("teamKills");
            killStreak = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("maxKillStreaks");
            heals = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("heals");
            damageDealt = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("damageDealt");

            wins = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("wins");
            losses = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("losses");
            roundsplayed = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("roundsPlayed");
            top10s = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("top10s");
            mostKills1 = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("roundMostKills");
            revives = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("revives");
            timeSurv = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("timeSurvived");
            suicides = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("suicides");

            rideDistance = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("rideDistance");
            walkDistance = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("walkDistance");

            boosts = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("boosts");
            killPoints = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("killPoints");
            longestTimeSurv = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("longestTimeSurvived");
            roundMostKill = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("roundMostKills");
            weaponsAquired = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("weaponsAcquired");
            weeklyKills = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("weeklyKills");
            winPoints = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("winPoints");
            vehicleDestroys = jsonObject.getJSONObject("data").getJSONObject("attributes").getJSONObject("gameModeStats").getJSONObject(gamemode).getString("vehicleDestroys");
        }*/
        killsTV.setText(kills);

        deathsTV.setText(deaths);

        if (!kills.equals("--") && !deaths.equals("--"))
        kdTV.setText(String.format("%.2f", (Double.valueOf(kills) / Double.valueOf(deaths))));

        winsTV.setText(wins);
        lossesTV.setText(losses);

        if (!wins.equals("--") && !losses.equals("--"))
        winlossTV.setText(String.format("%.2f", (Double.valueOf(wins) / Double.valueOf(losses))));

        roundsPlayedTV.setText(roundsplayed);
        top10sTV.setText(top10s);
        mostKills.setText(mostKills1);
        revivesTV.setText(revives);

        if (!timeSurv.equals("--"))
        timeSurvived.setText(String.format("%.0f", Math.ceil(Double.valueOf(timeSurv) / 60)) + " Min");

        suicidesTV.setText(suicides);

        headshotKillsTV.setText(headKills);
        assistsTV.setText(assists);
        roadKillsTV.setText(roadKills);
        DBNOsTV.setText(DBNOs);

        if (!longestKill.equals("--"))
        longestKillTV.setText(String.format("%.0f", Math.rint(Double.valueOf(longestKill))) + "m");

        teamKillsTV.setText(teamKills);
        killStreakTV.setText(killStreak);
        healsTV.setText(heals);

        if (!damageDealt.equals("--"))
        damageDealtTV.setText(String.format("%.0f", Math.rint(Double.valueOf(damageDealt))));

        if (!rideDistance.equals("--"))
        rideDistanceTV.setText(String.format("%.0f", Math.rint(Double.valueOf(rideDistance))) + "m");

        if (!walkDistance.equals("--"))
        walkDistanceTV.setText(String.format("%.0f", Math.rint(Double.valueOf(walkDistance))) + "m");

        boostsTV.setText(boosts);

        if (!killPoints.equals("--"))
        killPointsTV.setText(String.format("%.0f", Math.rint(Double.valueOf(killPoints))));

        if (!longestTimeSurv.equals("--"))
        longestSurvTV.setText(String.format("%.0f", Math.rint(Double.valueOf(longestTimeSurv) / 60 )) + " Min");

        weaponsAquiredTV.setText(weaponsAquired);
        weeklyPointsTV.setText(weeklyKills);

        if (!winPoints.equals("--"))
        winPointsTV.setText(String.format("%.0f", Math.rint(Double.valueOf(winPoints))));

        vehicleDestroysTV.setText(vehicleDestroys);

        //
        swipeRefreshLayout.setRefreshing(false);
        //mProgressBar.setVisibility(View.GONE);

    }

    public long getTimeSinceLastUpdated(String timeLastUpdated) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date1 = simpleDateFormat.parse(timeLastUpdated);
            Date date2 = new Date();

            long different = date2.getTime() - date1.getTime();

            return different;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }

}
