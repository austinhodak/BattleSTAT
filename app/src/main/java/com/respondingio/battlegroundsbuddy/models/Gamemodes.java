package com.respondingio.battlegroundsbuddy.models;

import android.content.Context;
import android.content.SharedPreferences;

public class Gamemodes {

    private static Gamemodes INSTANCE;
    private SharedPreferences mSharedPreferences;
    private Context mContext;

    public Gamemodes(Context context) {
        this.mContext = context.getApplicationContext();
        mSharedPreferences = mContext.getSharedPreferences("com.austinhodak.pubgcenter", Context.MODE_PRIVATE);
    }

    public static Gamemodes getInstance(Context context)
    {
        if( INSTANCE == null )
            INSTANCE = new Gamemodes(context);

        return INSTANCE;
    }
}
