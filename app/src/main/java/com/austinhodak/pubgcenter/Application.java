package com.austinhodak.pubgcenter;

import android.content.SharedPreferences;
import android.support.multidex.MultiDexApplication;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import io.fabric.sdk.android.Fabric;

public class Application extends MultiDexApplication {

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        if (BuildConfig.DEBUG) {
            Fabric.with(this, crashlyticsKit);
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false);
        } else {
            Fabric.with(this, crashlyticsKit, new Answers());
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true);
        }

        FirebaseApp.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

        FirebaseRemoteConfig.getInstance().fetch().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(final Void aVoid) {
                FirebaseRemoteConfig.getInstance().activateFetched();
            }
        });

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("NOTIID", "Refreshed token: " + refreshedToken);

    }
}
