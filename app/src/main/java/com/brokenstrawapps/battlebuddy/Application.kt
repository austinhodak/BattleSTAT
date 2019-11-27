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
import com.instabug.library.Instabug
import com.instabug.library.invocation.InstabugInvocationEvent
import com.instabug.library.ui.onboarding.WelcomeMessage
import timber.log.Timber

class Application : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.VERSION_NAME.contains("Beta", true)) {
            Instabug.Builder(this, "33e193600a878be09243378fd2a0aa05")
                    .setInvocationEvents(InstabugInvocationEvent.TWO_FINGER_SWIPE_LEFT)
                    .build()
        } else {
            Instabug.Builder(this, "b78ba0c060ea6b8883b4de328ca0ed93")
                    .setInvocationEvents(InstabugInvocationEvent.TWO_FINGER_SWIPE_LEFT)
                    .build()
        }

        if (BuildConfig.DEBUG) {
            initDebug()
        } else {
            initRelease()
        }

        Premium.init(applicationContext)

        Instabug.setWelcomeMessageState(WelcomeMessage.State.DISABLED)
        Instabug.setPrimaryColor(resources.getColor(R.color.md_red_500))

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

        MobileAds.initialize(this, "ca-app-pub-1646739421365093~2508030809")

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
