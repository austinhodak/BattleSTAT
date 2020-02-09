package com.brokenstrawapps.battlebuddy

import androidx.multidex.MultiDexApplication
import com.brokenstrawapps.battlebuddy.utils.Ads
import com.brokenstrawapps.battlebuddy.utils.Premium
import com.brokenstrawapps.battlebuddy.utils.Seasons
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.iid.FirebaseInstanceId
import timber.log.Timber

class Application : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            initDebug()
        } else {
            initRelease()
        }

        Premium.init(applicationContext)


        FirebaseApp.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

        FirebaseFirestore.getInstance().firestoreSettings = settings

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        /*val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()

        mFirebaseRemoteConfig.setConfigSettings(configSettings)
        mFirebaseRemoteConfig.fetchAndActivate().addOnSuccessListener {
            doAsync {
                Seasons.getInstance().withAPIKey(mFirebaseRemoteConfig.getString(PUBG_API_KEY)).loadSeasons()

            }
        }*/

        Seasons.init()

        Ads.init(applicationContext)

        MobileAds.initialize(this, "ca-app-pub-2981302488834327~6662457919")

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            Timber.d("${it.id} -- ${it.token}")
        }
    }

    private fun initDebug() {
        Timber.plant(Timber.DebugTree())
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false)
    }

    private fun initRelease() {
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)
    }

    override fun onTerminate() {
        super.onTerminate()
        Premium.onDestroy()
    }
}
