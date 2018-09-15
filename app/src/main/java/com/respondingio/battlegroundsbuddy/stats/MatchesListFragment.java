package com.respondingio.battlegroundsbuddy.stats;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.FirebaseFunctionsException.Code;
import com.google.firebase.functions.HttpsCallableResult;
import com.respondingio.battlegroundsbuddy.R;
import com.respondingio.battlegroundsbuddy.models.MatchData;
import com.respondingio.battlegroundsbuddy.models.PrefPlayer;
import com.respondingio.battlegroundsbuddy.stats.MatchesListAdapter.OnItemClickListener;
import de.mateware.snacky.Snacky;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.idik.lib.slimadapter.SlimAdapter;
import net.idik.lib.slimadapter.SlimInjector;
import net.idik.lib.slimadapter.viewinjector.IViewInjector;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class MatchesListFragment extends Fragment {

    String gameModeMatch = "matchesSolo";

    String[] gamemodeMatches = {"matchesSolo", "matchesSoloFPP", "matchesDuo", "matchesDuoFPP", "matchesSquad", "matchesSquadFPP"};
    public static String[] modesList = {"solo", "solo-fpp", "duo", "duo-fpp", "squad", "squad-fpp"};

    MatchesListAdapter mAdapter;

    @BindView(R.id.matches_RV) RecyclerView mRecyclerView;

    SwipeRefreshLayout mSwipeRefreshLayout;

    List<MatchData> matchList = new ArrayList<>();

    private DatabaseReference mDatabase;

    private FirebaseFirestore mFirestore;

    private FirebaseFunctions mFunctions;

    private SharedPreferences mSharedPreferences;

    private int order = -1;

    private String playerName, playerID, region, gamemode;

    private RequestQueue queue;

    private String seasonLong, seasonShort;

    private View view;

    private long lastUpdated;

    long updateTimeout = 15;

    List<String> confettiList = new ArrayList<>();

    public MatchesListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_matches_list, container, false);
        ButterKnife.bind(this, view);

        if (getArguments() == null) {
            return null;
        }

        mSharedPreferences = getActivity().getSharedPreferences("com.respondingio.battlegroundsbuddy", android.content.Context.MODE_PRIVATE);

        if (mSharedPreferences.getBoolean("premiumV1", false)) {
            updateTimeout = 2;
        }

        queue = Volley.newRequestQueue(getActivity());

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFunctions = FirebaseFunctions.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        PrefPlayer player = (PrefPlayer) getArguments().getSerializable("player");

        playerName = player.getPlayerName();
        playerID = player.getPlayerID();
        region = player.getSelectedShardID();
        gamemode = player.getSelectedGamemode();
        gameModeMatch = gamemodeMatches[Arrays.asList(modesList).indexOf(player.getSelectedGamemode())];
        seasonLong = "division.bro.official." + player.getSelectedSeason();
        seasonShort = player.getSelectedSeason();

        Log.d("MATCHES", "matches/" + region + "/" + seasonShort + "/" + gameModeMatch);

        setupAdapter();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new MatchesListAdapter(matchList, getActivity(), new OnItemClickListener() {
            @Override
            public void onItemClick(final MatchData item) {
                if (getActivity() == null) return;
                try {
                    Intent intent = new Intent(getActivity(), MatchDetailActivity.class);
                    intent.putExtra("matchID", item.getMatchID());
                    intent.putExtra("playerID", playerID);
                    intent.putExtra("regionID", item.getMatchTopData().getString("shardId"));
                    startActivity(intent);
                } catch (Exception e) {
                    Snacky.builder().setActivity(getActivity()).error().setText("Error Occurred, please try again.").show();
                    e.printStackTrace();
                }
            }
        });
        mRecyclerView.setAdapter(mAdapter);

        loadMatches(playerID);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.md_orange_500, R.color.md_pink_500, R.color.md_orange_500, R.color.md_black_1000);
        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if (getTimeSinceLastUpdated(lastUpdated) / 60 > updateTimeout) {
                mSwipeRefreshLayout.setRefreshing(true);
                startPullNewData();
            } else {
                mSwipeRefreshLayout.setRefreshing(false);
                Snacky.builder().setActivity(getActivity()).setBackgroundColor(Color.parseColor("#3F51B5")).setText("You can refresh once every " + updateTimeout + " minutes.").setDuration(5000).setActionTextColor(Color.WHITE).build().show();
            }
        });

        return view;
    }

    public long getTimeSinceLastUpdated(long timeLastUpdated) {
        return Math.abs(timeLastUpdated - (System.currentTimeMillis() / 1000));
    }

    private void loadMatchData(final String matchID) {
        final MatchData match = new MatchData();
        mFirestore.collection("matchData").document(matchID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable final DocumentSnapshot documentSnapshot, @Nullable final FirebaseFirestoreException e) {
                match.setLoading(true);
                int index = -1;
                if (documentSnapshot == null || !documentSnapshot.exists()) {
                    Log.d("MATCHES", "Match not found, loading new. - " + matchID);
                    addMatchData(matchID, region).addOnCompleteListener(new OnCompleteListener<Map<String, Object>>() {
                        @Override
                        public void onComplete(@NonNull final Task<Map<String, Object>> task) {
                            if (!task.isSuccessful()) {
                                try {
                                    for (Object object : matchList) {
                                        if (object instanceof MatchData) {
                                            if (((MatchData) object).getMatchID().equals(documentSnapshot.getId())) {
                                                matchList.remove(matchList.indexOf(object));
                                                mAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    }
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                                return;
                            }
                        }
                    });

                    try {
                        for (Object object : matchList) {
                            if (object instanceof MatchData) {
                                if (((MatchData) object).getMatchID().equals(documentSnapshot.getId())) {
                                    index = matchList.indexOf(object);
                                }
                            }
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                    match.setMatchID(documentSnapshot.getId());

                    if (index != -1) {
                        matchList.set(index, match);
                    } else {
                        matchList.add(match);
                    }

                    mAdapter.notifyDataSetChanged();

                    Log.d("MATCHES", "Inside Doc Null");
                    return;
                }

                try {
                    for (Object object : matchList) {
                        if (object instanceof MatchData) {
                            if (((MatchData) object).getMatchID().equals(documentSnapshot.getId())) {
                                index = matchList.indexOf(object);
                            }
                        }
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

                match.setDuration(documentSnapshot.getLong("duration"));
                match.setMatchTopData(documentSnapshot);
                match.setMatchID(documentSnapshot.getId());

                if (index != -1) {
                    matchList.set(index, match);
                } else {
                    matchList.add(match);
                }
                mAdapter.notifyDataSetChanged();

                Log.d("MATCHES", "Inside Doc Reg");

                CollectionReference ref = mFirestore.collection("matchData").document(matchID).collection("participants");
                com.google.firebase.firestore.Query query = ref.whereEqualTo("playerId", "account." + playerID);
                query.whereEqualTo("playerId", "account." + playerID)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                int index = matchList.indexOf(match);

                                match.setCurrentPlayerData(document);
                                match.setLoading(false);

                                matchList.set(index, match);
                                mAdapter.notifyDataSetChanged();
                                return;
                            }
                        }
                    }
                });

                try {
                    sortList();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void sortList() throws Exception {
        Collections.sort(matchList, (Comparator<Object>) (o1, o2) -> {
            MatchData m1 = (MatchData) o1;
            MatchData m2 = (MatchData) o2;
            return m2.getCreatedAt().compareTo(m1.getCreatedAt());
        });

        mAdapter.notifyDataSetChanged();
    }

    private void loadMatches(final String playerID) {
        mSwipeRefreshLayout.setRefreshing(true);
        matchList.clear();

        Query query = mDatabase.child("user_stats").child(playerID).child("matches/" + region + "/" + seasonShort + "/" + gameModeMatch);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onCancelled(@NonNull final DatabaseError databaseError) {

            }

            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable final String s) {
                loadMatchData(dataSnapshot.getKey());
                Log.d("MATCH", dataSnapshot.getKey() + " - " + playerID);
            }

            @Override
            public void onChildChanged(@NonNull final DataSnapshot dataSnapshot, @Nullable final String s) {

            }

            @Override
            public void onChildMoved(@NonNull final DataSnapshot dataSnapshot, @Nullable final String s) {

            }

            @Override
            public void onChildRemoved(@NonNull final DataSnapshot dataSnapshot) {
                try {
                    for (Object object : matchList) {
                        if (object instanceof MatchData) {
                            if (((MatchData) object).getMatchTopData().getId().equals(dataSnapshot.getKey())) {
                                int index;
                                index = matchList.indexOf(object);
                                matchList.remove(index);
                            }
                        }
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        mDatabase.child("user_stats").child(playerID).child("matches/" + region + "/" + seasonShort).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mSwipeRefreshLayout.setRefreshing(false);

                    lastUpdated = (long) dataSnapshot.child("lastUpdated").getValue();
                    MainStatsActivity activity = (MainStatsActivity) getActivity();

                    if (activity != null) {
                        if (getTimeSinceLastUpdated(lastUpdated) / 60 > updateTimeout) {
                            //activity.showOutdated();
                        } else {
                            //activity.hideOutdated();
                        }
                    }
                } else {
                    mSwipeRefreshLayout.setRefreshing(true);
                    startPullNewData();
                }
            }

            @Override
            public void onCancelled(@NonNull final DatabaseError databaseError) {

            }
        });
    }

    private void setupAdapter() {


        SlimAdapter.create().register(R.layout.stats_match_item, new SlimInjector<MatchData>() {
            @Override
            public void onInject(final MatchData data, final IViewInjector injector) {
                if (data.isLoading()) {
                    injector.visible(R.id.match_pg);
                } else {
                    injector.invisible(R.id.match_pg);
                }

                LinearLayout linearLayout = (LinearLayout) injector.findViewById(R.id.ll);
                KonfettiView confetti = (KonfettiView) injector.findViewById(R.id.viewKonfetti);

                if (data.getMatchTopData() != null) {
                    injector.text(R.id.match_duration, data.getDuration());
                    injector.image(R.id.map_icon, data.getMapIcon());

                    injector.text(R.id.match_time, data.getFormattedCreatedAt());
                }

                if (data.getCurrentPlayerData() != null) {
                    Long winPlace = data.getCurrentPlayerData().getLong("winPlace");
                    Long totalPlayers = data.getMatchTopData().getLong("participantCount");

                    injector.text(R.id.match_place, "#" + winPlace + "/" + totalPlayers);
                    injector.text(R.id.match_kills, data.getCurrentPlayerData().getLong("kills").toString());
                    injector.text(R.id.match_damage, data.getCurrentPlayerData().getLong("damageDealt").toString());
                    injector.text(R.id.match_distance, data.getTotalDistanceTravelled());

                    if (winPlace == 1 && !confettiList.contains(data.getMatchTopData().getId())) {
                        confettiList.add(data.getMatchTopData().getId());
                        confetti.build()
                                .addColors(getResources().getColor(R.color.md_deep_orange_500), Color.YELLOW,
                                        getResources().getColor(R.color.md_pink_500),
                                        getResources().getColor(R.color.md_orange_500))
                                .setDirection(0.0, 359.0)
                                .setSpeed(4f, 8f)
                                .setFadeOutEnabled(true)
                                .setTimeToLive(1000L)
                                .addShapes(Shape.RECT, Shape.CIRCLE)
                                .addSizes(new Size(12, 5f))
                                .setPosition(-50f, mRecyclerView.getWidth() - 50f, -50f, -50f)
                                .streamFor(150, 2000L);
                    } else {
                        confetti.reset();
                    }

                    if (winPlace == 1) {
                        injector.background(R.id.match_div, R.color.md_green_A400);
                    } else if (winPlace <= 10) {
                        injector.background(R.id.match_div, R.color.md_orange_A400);
                    } else {
                        injector.background(R.id.match_div, R.color.md_light_dividers);
                    }
                }
            }
        }).attachTo(mRecyclerView).updateData(matchList);
    }

    private void startPullNewData() {
        loadNewData(playerID, region, seasonShort).addOnCompleteListener(new OnCompleteListener<Map<String, Object>>() {
            @Override
            public void onComplete(@NonNull final Task<Map<String, Object>> task) {
                if (!task.isSuccessful()) {
                    Exception e = task.getException();
                    if (e instanceof FirebaseFunctionsException) {
                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                        FirebaseFunctionsException.Code code = ffe.getCode();
                        Object details = ffe.getDetails();

                        Log.e("PullNewStats", "onComplete: " + code.toString());

                        if (code == Code.NOT_FOUND) {
                            Snacky.builder().setActivity(getActivity()).info().setText("No matches found for selected gamemode.").setDuration(
                                    Snacky.LENGTH_LONG).show();
                        } else if (code == Code.RESOURCE_EXHAUSTED) {
                            Snacky.builder().setActivity(getActivity()).error().setText("API limit reached, try again in a minute.").setDuration(
                                    Snacky.LENGTH_LONG).show();
                        } else {
                            Snacky.builder().setActivity(getActivity()).error().setText("Unknown error.").setDuration(
                                    Snacky.LENGTH_LONG).show();
                        }
                    }

                    mSwipeRefreshLayout.setRefreshing(false);
                    return;
                }


            }
        });
    }

    private Task<Map<String, Object>> addMatchData(String matchID, String shardID) {
        Map<String, Object> data = new HashMap<>();
        data.put("matchID", matchID);
        data.put("shardID", shardID);

        return mFunctions.getHttpsCallable("addMatchData").call(data).continueWith(new Continuation<HttpsCallableResult, Map<String, Object>>() {
            @Override
            public Map<String, Object> then(@NonNull final Task<HttpsCallableResult> task) throws Exception {
                Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                Log.d("REQUEST", String.valueOf(result));
                return result;
            }
        });
    }

    private Task<Map<String, Object>> loadNewData(String playerID, String shardID, String seasonID) {
        Map<String, Object> data = new HashMap<>();
        data.put("playerID", playerID);
        data.put("shardID", shardID);
        data.put("seasonID", seasonID);

        return mFunctions.getHttpsCallable("loadPlayerStats").call(data).continueWith(new Continuation<HttpsCallableResult, Map<String, Object>>() {
            @Override
            public Map<String, Object> then(@NonNull final Task<HttpsCallableResult> task) throws Exception {
                Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                Log.d("REQUEST", String.valueOf(result));
                return result;
            }
        });
    }

}
