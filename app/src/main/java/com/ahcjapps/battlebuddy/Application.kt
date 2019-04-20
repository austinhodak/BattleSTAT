package com.ahcjapps.battlebuddy

import android.util.Log
import androidx.multidex.MultiDexApplication
import com.ahcjapps.battlebuddy.models.Seasons
import com.ahcjapps.battlebuddy.utils.Ads
import com.ahcjapps.battlebuddy.utils.Premium
import com.crashlytics.android.Crashlytics
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.instabug.library.Instabug
import com.instabug.library.invocation.InstabugInvocationEvent
import com.instabug.library.ui.onboarding.WelcomeMessage
import io.fabric.sdk.android.Fabric
import org.jetbrains.anko.doAsync

class Application : MultiDexApplication() {

    private val PUBG_API_KEY = "pubg_api_key"

    override fun onCreate() {
        super.onCreate()

        //if (LeakCanary.isInAnalyzerProcess(this)) return

        if (BuildConfig.VERSION_NAME.contains("Beta", true)) {
            //LeakCanary.install(this)
            Instabug.Builder(this, "33e193600a878be09243378fd2a0aa05")
                    .setInvocationEvents(InstabugInvocationEvent.TWO_FINGER_SWIPE_LEFT)
                    .build()
        } else {
            Instabug.Builder(this, "33e193600a878be09243378fd2a0aa05")
                    .setInvocationEvents(InstabugInvocationEvent.TWO_FINGER_SWIPE_LEFT)
                    .build()
        }

        Premium.init(applicationContext)

        Instabug.setWelcomeMessageState(WelcomeMessage.State.DISABLED)
        Instabug.setPrimaryColor(resources.getColor(R.color.md_red_500))

        if (BuildConfig.DEBUG) {
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false)
        } else {
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)
            Fabric.with(this, Crashlytics())
        }

        FirebaseApp.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

        FirebaseFirestore.getInstance().firestoreSettings = settings

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()

        mFirebaseRemoteConfig.setConfigSettings(configSettings)
        mFirebaseRemoteConfig.fetchAndActivate().addOnSuccessListener {
            doAsync {
                Seasons.getInstance().withAPIKey(mFirebaseRemoteConfig.getString(PUBG_API_KEY)).loadSeasons()

            }
        }

        com.ahcjapps.battlebuddy.utils.Seasons.init()

        Ads.init(applicationContext)

        MobileAds.initialize(this, "ca-app-pub-1646739421365093~2508030809")

        //GoodPrefs.init(applicationContext)

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            Log.d("TOKEN", "${it.id} -- ${it.token}")
        }

    }

    override fun onTerminate() {
        super.onTerminate()
        Premium.onDestroy()
    }
}
