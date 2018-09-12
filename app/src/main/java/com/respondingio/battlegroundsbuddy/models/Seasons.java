package com.respondingio.battlegroundsbuddy.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Seasons {

    private static Seasons INSTANCE;

    private String apiKey;

    private DatabaseReference mDBref;

    private Map<String, Boolean> pcSeasons = new HashMap<>();
    private Map<String, Boolean> xboxSeasons = new HashMap<>();
    private String pcCurrentSeason = null;
    private String xboxCurrentSeason = null;

    public Seasons() {
        mDBref = FirebaseDatabase.getInstance().getReference();
    }

    public Seasons withAPIKey(final String apiKey) {
        this.apiKey = apiKey;
        return getInstance();
    }

    public static Seasons getInstance()
    {
        if( INSTANCE == null )
            INSTANCE = new Seasons();

        return INSTANCE;
    }

    public void loadSeasons() {
        mDBref.child("seasons").child("pc-na").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable final String s) {
                pcSeasons.put(dataSnapshot.getKey(), (Boolean) dataSnapshot.getValue());
                if ((boolean) dataSnapshot.getValue()) {
                    pcCurrentSeason = dataSnapshot.getKey();
                }
            }

            @Override
            public void onChildChanged(@NonNull final DataSnapshot dataSnapshot, @Nullable final String s) {

            }

            @Override
            public void onChildRemoved(@NonNull final DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull final DataSnapshot dataSnapshot, @Nullable final String s) {

            }

            @Override
            public void onCancelled(@NonNull final DatabaseError databaseError) {

            }
        });

        mDBref.child("seasons").child("xbox-na").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable final String s) {
                xboxSeasons.put(dataSnapshot.getKey(), (Boolean) dataSnapshot.getValue());
                if ((boolean) dataSnapshot.getValue()) {
                    xboxCurrentSeason = dataSnapshot.getKey();
                }
            }

            @Override
            public void onChildChanged(@NonNull final DataSnapshot dataSnapshot, @Nullable final String s) {

            }

            @Override
            public void onChildRemoved(@NonNull final DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull final DataSnapshot dataSnapshot, @Nullable final String s) {

            }

            @Override
            public void onCancelled(@NonNull final DatabaseError databaseError) {

            }
        });
    }

    public List<String> getSeasonListArray(int region) {
        //1-pc,2-xbox
        Log.d("SEASONS OBJ", "getSeasonListArray: " + region);
        List<String> seasonList = new ArrayList<>();
        if (region == 1) {
            for (Map.Entry entry: getPcSeasons().entrySet()) {
                if ((boolean) entry.getValue()) {
                    pcCurrentSeason = (String) entry.getKey();
                    seasonList.add(entry.getKey() + " (Current)");
                    continue;
                }
                seasonList.add((String) entry.getKey());
            }
        } else if (region == 2) {
            for (Map.Entry entry: getXboxSeasons().entrySet()) {
                if ((boolean) entry.getValue()) {
                    xboxCurrentSeason = (String) entry.getKey();
                    seasonList.add(entry.getKey() + " (Current)");
                    continue;
                }
                seasonList.add((String) entry.getKey());
            }
        }

        Collections.sort(seasonList);
        Collections.reverse(seasonList);
        return seasonList;
    }

    public List<String> getSeasonListArrayOG(int region) {
        //1-pc,2-xbox
        Log.d("SEASONS OBJ", "getSeasonListArray: " + region);
        List<String> seasonList = new ArrayList<>();
        if (region == 1) {
            for (Map.Entry entry: getPcSeasons().entrySet()) {
                seasonList.add((String) entry.getKey());
            }
        } else if (region == 2) {
            for (Map.Entry entry: getXboxSeasons().entrySet()) {
                seasonList.add((String) entry.getKey());
            }
        }

        Collections.sort(seasonList);
        Collections.reverse(seasonList);
        return seasonList;
    }

    public Map<String, Boolean> getPcSeasons() {
        return pcSeasons;
    }

    public Map<String, Boolean> getXboxSeasons() {
        return xboxSeasons;
    }

    public String getPcCurrentSeason() {
        return pcCurrentSeason;
    }

    public String getXboxCurrentSeason() {
        return xboxCurrentSeason;
    }

    public int getCurrentSeasonInt(int region) {
        switch (region) {
            case 1:
                return this.getSeasonListArray(region).indexOf(Seasons.getInstance().getPcCurrentSeason() + " (Current)");
            case 2:
                return this.getSeasonListArray(region).indexOf(Seasons.getInstance().getXboxCurrentSeason() + " (Current)");
            default:
                return this.getSeasonListArray(1).indexOf(Seasons.getInstance().getPcCurrentSeason() + " (Current)");
        }

    }
}
