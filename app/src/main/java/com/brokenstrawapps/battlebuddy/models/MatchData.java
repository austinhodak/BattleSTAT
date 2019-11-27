package com.brokenstrawapps.battlebuddy.models;

import android.text.format.DateUtils;

import com.brokenstrawapps.battlebuddy.R;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class MatchData {

    public MatchData() {
    }

    boolean isLoading = false;
    long duration;
    DocumentSnapshot matchTopData, currentPlayerData;
    String createdAt;
    String matchID;

    public String getMatchID() {
        return matchID;
    }

    public void setMatchID(final String matchID) {
        this.matchID = matchID;
    }

    public String getCreatedAt() {
        return getMatchTopData().getString("createdAt");
    }

    public void setCreatedAt(final String createdAt) {
        this.createdAt = createdAt;
    }

    public DocumentSnapshot getCurrentPlayerData() {
        return currentPlayerData;
    }

    public void setCurrentPlayerData(final DocumentSnapshot currentPlayerData) {
        this.currentPlayerData = currentPlayerData;
    }

    public DocumentSnapshot getMatchTopData() {
        return matchTopData;
    }

    public void setMatchTopData(final DocumentSnapshot matchTopData) {
        this.matchTopData = matchTopData;
    }

    public String getDuration() {
        return DateUtils.formatElapsedTime(duration);
    }

    public void setDuration(final long duration) {
        this.duration = duration;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(final boolean loading) {
        isLoading = loading;
    }


    public int getMapIcon() {
        if (matchTopData == null) return -1;
        String mapData = matchTopData.getString("mapName");
        switch (mapData) {
            case "Savage_Main":
                return R.drawable.sanhok_icon;
            case "Erangel_Main":
                return R.drawable.erangel_icon;
            case "Desert_Main":
                return R.drawable.cactu;
            default:
                return R.drawable.snowflake;
        }
    }

    public String getTotalDistanceTravelled() {
        String distance = "";
        long distanceLong = 0;

        distanceLong += getCurrentPlayerData().getLong("rideDistance");
        distanceLong += getCurrentPlayerData().getLong("walkDistance");
        distanceLong += getCurrentPlayerData().getLong("swimDistance");

        distance = String.format("%.0f", Math.rint(distanceLong)) + "m";

        return distance;
    }

    public String getFormattedCreatedAt() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        long time = 0;
        try {
            time = sdf.parse(getMatchTopData().getString("createdAt")).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int offset = TimeZone.getDefault().getRawOffset() + TimeZone.getDefault().getDSTSavings();
        long now = System.currentTimeMillis();

        CharSequence ago =
                DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);

        return (String) ago;
    }
}
