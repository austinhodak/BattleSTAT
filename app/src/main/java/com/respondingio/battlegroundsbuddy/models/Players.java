package com.respondingio.battlegroundsbuddy.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.respondingio.battlegroundsbuddy.exceptions.UserNotLoggedInException;

public class Players {

    private static Players INSTANCE;

    private final FirebaseUser currentUser;

    private final Context mContext;

    private final SharedPreferences mSharedPreferences;

    private DatabaseReference mReference;

    public Players(Context context) {
        this.mContext = context.getApplicationContext();
        mSharedPreferences = mContext.getSharedPreferences("com.austinhodak.pubgcenter", Context.MODE_PRIVATE);
        mReference = FirebaseDatabase.getInstance().getReference();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            throw new RuntimeException("User not logged in.", new UserNotLoggedInException());
        } else {
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
        }
    }

    public static Players getInstance(Context context)
    {
        if( INSTANCE == null )
            INSTANCE = new Players(context);

        return INSTANCE;
    }

    public void getPlayerFromDB() {
        mReference.child("users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild("pubg_player")) {
                    mSharedPreferences.edit().remove("stats_selected_player").remove("stats_selected_player_id").apply();
                }
            }

            @Override
            public void onCancelled(@NonNull final DatabaseError databaseError) {

            }
        });
    }

}
