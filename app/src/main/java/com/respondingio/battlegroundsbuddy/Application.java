package com.respondingio.battlegroundsbuddy;

import android.content.SharedPreferences;
import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.respondingio.battlegroundsbuddy.models.Seasons;
import com.squareup.leakcanary.LeakCanary;
import io.fabric.sdk.android.Fabric;

public class Application extends MultiDexApplication {

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        //LeakCanary.install(this);

        if (BuildConfig.DEBUG) {
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false);
        } else {
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true);
            final Fabric fabric = new Fabric.Builder(this)
                    .kits(new Crashlytics())
                    .debuggable(true)
                    .build();
            Fabric.with(fabric);
        }

        FirebaseApp.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        FirebaseRemoteConfig.getInstance().fetch().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(final Void aVoid) {
                FirebaseRemoteConfig.getInstance().activateFetched();
                Seasons.getInstance().withAPIKey(FirebaseRemoteConfig.getInstance().getString("pubg_api_key")).loadSeasons();
            }
        });
    }

//    public int getTheme() {
//        boolean isNightMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("night_mode", false);
//        if (isNightMode) {
//            return R.style.AppTheme;
//        } else {
//            return R.style.AppThemeLight;
//        }
//    }
}
