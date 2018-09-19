package com.respondingio.battlegroundsbuddy.models;

import android.os.Parcel;
import android.os.Parcelable;

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
}
