package com.austinhodak.pubgcenter;

import android.content.SharedPreferences;
import android.support.multidex.MultiDexApplication;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import io.fabric.sdk.android.Fabric;

public class Application extends MultiDexApplication {

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        Fabric.with(this, crashlyticsKit);

        FirebaseApp.initializeApp(this);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

        sharedPreferences = this.getSharedPreferences("com.austinhodak.pubgcenter", MODE_PRIVATE);
        //sharedPreferences.edit().putBoolean("removeAds", false).apply();

        //Log.d("TOKEN", FirebaseInstanceId.getInstance().getToken());

    }
}
