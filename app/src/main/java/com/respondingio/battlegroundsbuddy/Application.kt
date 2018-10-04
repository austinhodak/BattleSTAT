package com.respondingio.battlegroundsbuddy

import androidx.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.respondingio.battlegroundsbuddy.models.Seasons
import com.squareup.leakcanary.LeakCanary
import io.fabric.sdk.android.Fabric
import org.jetbrains.anko.doAsync
import com.instabug.library.invocation.InstabugInvocationEvent
import com.instabug.library.Instabug
import com.instabug.library.ui.onboarding.WelcomeMessage


class Application : MultiDexApplication() {

    private val PUBG_API_KEY = "pubg_api_key"

    override fun onCreate() {
        super.onCreate()

        if (LeakCanary.isInAnalyzerProcess(this)) return
        //LeakCanary.install(this)

        if (BuildConfig.DEBUG) {
            Instabug.Builder(this, "b88ab4ff06e0bb4240f1ee0f261b78d4")
                    .setInvocationEvents(InstabugInvocationEvent.TWO_FINGER_SWIPE_LEFT, InstabugInvocationEvent.SCREENSHOT)
                    .build()
        } else {
            Instabug.Builder(this, "b31b0a70fb49b434daf6d39005418842")
                    .setInvocationEvents(InstabugInvocationEvent.TWO_FINGER_SWIPE_LEFT, InstabugInvocationEvent.SCREENSHOT)
                    .build()
        }

        Instabug.setWelcomeMessageState(WelcomeMessage.State.DISABLED)
        Instabug.setPrimaryColor(resources.getColor(R.color.md_red_500))

        if (BuildConfig.DEBUG) {
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false)
        } else {
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)
            val fabric = Fabric.Builder(this)
                    .kits(Crashlytics())
                    .build()
            Fabric.with(fabric)
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
        mFirebaseRemoteConfig.fetch().addOnSuccessListener {
            mFirebaseRemoteConfig.activateFetched()
            doAsync {
                Seasons.getInstance().withAPIKey(mFirebaseRemoteConfig.getString(PUBG_API_KEY)).loadSeasons()
            }
        }

        MobileAds.initialize(this, "ca-app-pub-1946691221734928~8934220899")
    }
}
