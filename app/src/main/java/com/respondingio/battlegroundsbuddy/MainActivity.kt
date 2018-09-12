package com.respondingio.battlegroundsbuddy

import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.support.customtabs.CustomTabsIntent
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.android.vending.billing.IInAppBillingService
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anjlab.android.iab.v3.Constants.BILLING_RESPONSE_RESULT_OK
import com.bumptech.glide.Glide
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.ads.consent.ConsentForm
import com.google.ads.consent.ConsentFormListener
import com.google.ads.consent.ConsentInfoUpdateListener
import com.google.ads.consent.ConsentInformation
import com.google.ads.consent.ConsentStatus
import com.google.ads.consent.ConsentStatus.PERSONALIZED
import com.google.ads.consent.ConsentStatus.UNKNOWN
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.marcoscg.ratedialog.RateDialog
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.respondingio.battlegroundsbuddy.R.string
import com.respondingio.battlegroundsbuddy.ammo.HomeAmmoList
import com.respondingio.battlegroundsbuddy.attachments.HomeAttachmentsFragment
import com.respondingio.battlegroundsbuddy.damage_calculator.DamageCalcActivity
import com.respondingio.battlegroundsbuddy.info.ControlsFragment
import com.respondingio.battlegroundsbuddy.info.TimerFragment
import com.respondingio.battlegroundsbuddy.loadout.LoadoutBestTabs
import com.respondingio.battlegroundsbuddy.loadout.LoadoutCreateMain
import com.respondingio.battlegroundsbuddy.profile.ProfileActivity
import com.respondingio.battlegroundsbuddy.rss.HomeUpdatesFragment
import com.respondingio.battlegroundsbuddy.stats.MainStatsActivity
import com.respondingio.battlegroundsbuddy.weapons.HomeWeaponsFragment
import de.mateware.snacky.Snacky
import kotlinx.android.synthetic.main.activity_main.appbar
import kotlinx.android.synthetic.main.activity_main.main_toolbar
import kotlinx.android.synthetic.main.activity_main.toolbar_title
import org.jetbrains.anko.toast
import java.io.File
import java.net.MalformedURLException
import java.net.URL
import java.text.DecimalFormat
import java.util.Arrays
import kotlin.collections.set

public class MainActivity : AppCompatActivity() {

    private lateinit var result: Drawer

    private lateinit var headerResult: AccountHeader

    private var mAuth: FirebaseAuth? = null

    private var iap: IInAppBillingService? = null

    private lateinit var mSharedPreferences: SharedPreferences

    lateinit var newSharedPreferences: SharedPreferences

    private val removeAds = SecondaryDrawerItem().withIdentifier(9001).withName("Remove Ads").withIcon(R.drawable.icons8_remove_ads_96).withSelectable(false)

    private val signInDrawerItem = SecondaryDrawerItem().withIcon(R.drawable.icons8_password).withSelectable(false).withName("Login or Sign Up").withIdentifier(90001)

    private val profileItem = PrimaryDrawerItem().withIcon(R.drawable.icons8_user).withName("My Profile").withSelectable(false).withIdentifier(90002)

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    private lateinit var fbDatabase: FirebaseDatabase

    private lateinit var mInterstitialAd: InterstitialAd

    private lateinit var listener: OnSharedPreferenceChangeListener

    override fun onCreate(savedInstanceState: Bundle?) {
        mSharedPreferences = this.getSharedPreferences("com.austinhodak.pubgcenter", Context.MODE_PRIVATE)
        newSharedPreferences = this.getSharedPreferences("com.respondingio.battlegroundsbuddy", Context.MODE_PRIVATE)

        if (mSharedPreferences.getBoolean("night_mode", true)) {
            setTheme(R.style.AppTheme)
        } else {
            setTheme(R.style.AppThemeLight)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!isGooglePlayServicesAvailable(this)) {
            //Play Services Not Available, stop.
            return
        }

        setSupportActionBar(main_toolbar)
        toolbar_title.text = getString(R.string.drawer_title_weapons)

        initializeFirebase()

        isNightMode = mSharedPreferences.getBoolean("night_mode", false)

        setupDrawer()

        val serviceIntent = Intent("com.android.vending.billing.InAppBillingService.BIND")
        serviceIntent.`package` = "com.android.vending"
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        RateDialog.with(this, 1, 5)

        MobileAds.initialize(this,
                "ca-app-pub-1946691221734928~8934220899")

        checkConsent()

        if (!mSharedPreferences.getBoolean("removeAds", false)) {
            mInterstitialAd = InterstitialAd(this)
            mInterstitialAd.adUnitId = "ca-app-pub-1946691221734928/5517720061"

            if (!mSharedPreferences.getBoolean("personalized_ads", false)) {
                var extra = Bundle()
                extra.putString("npa", "1")
                mInterstitialAd.loadAd(AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extra).build())
            }
            mInterstitialAd.loadAd(AdRequest.Builder().build())
            mInterstitialAd.adListener = object : AdListener() {
                override fun onAdClosed() {
                    finish()
                }
            }
        }

        listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, s ->
            Log.d("SHARED_PREF", s)
            if (s.equals("night_mode")) {
                reloadTheme()
                scheduledRestart = true
                onResume()
            }
        }

    }

    private fun checkIfMobileFilesExists() {
        var file = File(Environment.getExternalStorageDirectory(), "/Android/data/com.tencent.ig/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini")
        if(file.exists()) {
            Log.d("FILE", "UserCustom.ini File Exists!")
        } else {
            Log.d("FILE", "UserCustom.ini File DOESNT Exist!")
        }
    }

    private fun initializeFirebase() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        fbDatabase = FirebaseDatabase.getInstance()
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance()
        }
    }

    override fun onBackPressed() {
        if (this::result.isInitialized && result.isDrawerOpen) {
            result.closeDrawer()
            return
        } else {
            super.onBackPressed()
        }
        if (this::mSharedPreferences.isInitialized) {
            if (mSharedPreferences.getBoolean("removeAds", false)) {
                super.onBackPressed()
                return
            } else {
                super.onBackPressed()
            }
        } else if (!this::mSharedPreferences.isInitialized) {
            super.onBackPressed()
            return
        } else {
            super.onBackPressed()
        }
        var launchCount = mSharedPreferences.getInt("launchCount", 0)
        if (launchCount >= 2) {
            try {
                if (mInterstitialAd.isLoaded) {
                    mInterstitialAd.show()
                    mSharedPreferences.edit().putInt("launchCount", 0).apply()
                } else {
                    super.onBackPressed()
                }
            } catch (e: Exception) {
                super.onBackPressed()
            }
        } else {
            mSharedPreferences.edit().putInt("launchCount", launchCount + 1).apply()
            super.onBackPressed()
        }

    }

    private var isNightMode: Boolean = false

    public fun reloadTheme() {
        isNightMode = mSharedPreferences.getBoolean("night_mode", false)
    }

    private fun loadPurchases() {
        try {
            val ownedItems = iap?.getPurchases(3, packageName, "inapp", null)
            val response = ownedItems?.getInt("RESPONSE_CODE")
            Log.d("OWNED", response.toString() + "")
            if (response == BILLING_RESPONSE_RESULT_OK) {
                val ownedItems2 = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST")
                Log.d("OWNED", ownedItems2.toString())
                if (ownedItems2.contains("remove_ads")) {
                    Log.d("OWNED", "ADS REMOVED")
                    mSharedPreferences.edit().putBoolean("removeAds", true).apply()
                    result.removeItem(9001)
                } else {
                    mSharedPreferences.edit().putBoolean("removeAds", false).apply()
                    //result.addStickyFooterItem(removeAds)
                }

                if (ownedItems2.contains("plus_v1")) {
                    newSharedPreferences.edit().putBoolean("premiumV1", true).apply()
                } else {
                    newSharedPreferences.edit().putBoolean("premiumV1", false).apply()
                }
            }
        } catch (e: Exception) {
            Log.e("OWNED", e.toString() + e.message)
        }
    }

    override fun onStart() {
        super.onStart()

        if (!isGooglePlayServicesAvailable(this)) {
            return
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (iap != null) {
            unbindService(serviceConnection)
        }
    }

    override fun onPause() {
        super.onPause()
        if (this::listener.isInitialized)
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private var scheduledRestart: Boolean = false

    override fun onResume() {
        super.onResume()
        if (this::listener.isInitialized)
        mSharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        checkAuthStatus()

        try {
            if (scheduledRestart) {
                scheduledRestart = false
                val i : Intent = baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(i)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        } catch (e: Exception) {

        }
    }

    private fun checkAuthStatus() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            notifyLoggedIn(false)
        }
    }

    private fun updateToolbarElevation(int: Float) {
        ViewCompat.setElevation(appbar, int)
    }

    private var new_fragment: Fragment? = null

    private fun setupDrawer() {
        val homeFragment = HomeWeaponsFragment()
        supportFragmentManager.beginTransaction().replace(R.id.main_frame, homeFragment)
                .commit()

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            supportActionBar?.elevation = 0.0f
            appbar.elevation = 0.0f
        }

        headerResult = AccountHeaderBuilder()
                .withActivity(this)
                .withDividerBelowHeader(false)
                .addProfiles(
                        ProfileDrawerItem().withIdentifier(1).withName(R.string.app_name).withEmail("Level 1").withIcon(R.drawable.icon1),
                        ProfileSettingDrawerItem().withName("Logout").withIcon(R.drawable.icons8_logout).withOnDrawerItemClickListener { _, _, _ ->

                            mSharedPreferences.edit().remove("stats_selected_player").remove("stats_selected_player_id").apply()
                            newSharedPreferences.edit().remove("selected-player-id").apply()

                            AuthUI.getInstance().signOut(this).addOnCompleteListener {
                                Snacky.builder().setActivity(this).info().setText("Signed Out.").show()

                                notifySignedOut()

                                if (result.getStickyFooterPosition(90001) == -1) {
                                    result.addStickyFooterItem(signInDrawerItem)
                                }
                            }
                            return@withOnDrawerItemClickListener true
                        }
                )
                .withOnAccountHeaderListener { _, _, _ -> false }
                .build()

        Glide.with(this).load(R.drawable.header2).into(headerResult.headerBackgroundView)

        val updates = PrimaryDrawerItem().withIdentifier(100).withName(R.string.drawer_title_update).withIcon(R.drawable.rss)
        val weapons = PrimaryDrawerItem().withIdentifier(1).withName(R.string.drawer_title_weapons).withIcon(R.drawable.icons8_rifle).withIconTintingEnabled(false)
        val attachment = PrimaryDrawerItem().withIdentifier(2).withName(R.string.drawer_title_attachments).withIcon(R.drawable.icons8_magazine).withIconTintingEnabled(false)
        val ammo = PrimaryDrawerItem().withIdentifier(3).withName(R.string.drawer_title_ammo).withIcon(R.drawable.icons8_ammo).withIconTintingEnabled(false)
        val consumables = PrimaryDrawerItem().withIdentifier(4).withName(R.string.drawer_title_consumables).withIcon(R.drawable.icons8_syringe).withIconTintingEnabled(false)
        val equipment = PrimaryDrawerItem().withIdentifier(5).withName(R.string.drawer_title_equipment).withIcon(R.drawable.icons8_helmet).withIconTintingEnabled(false)
        val settings = SecondaryDrawerItem().withIdentifier(901).withName(R.string.drawer_title_about).withSelectable(false).withIcon(R.drawable.icons8_info)

        var color = if (!isNightMode) {
            R.color.md_light_secondary
        } else {
            R.color.md_dark_secondary
        }

        result = DrawerBuilder()
                .withActivity(this)
                .withToolbar(main_toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        weapons,
                        attachment,
                        ammo,
                        equipment,
                        consumables,
                        PrimaryDrawerItem().withName("Vehicles").withIcon(R.drawable.icons8_car).withIdentifier(610),
                        //DividerDrawerItem(),
                        ExpandableDrawerItem().withName("More").withIcon(R.drawable.icons8_view_more_96).withSelectable(false).withSubItems(
                                DividerDrawerItem(),
                        PrimaryDrawerItem().withName(R.string.drawer_title_controls).withIcon(R.drawable.icons8_game_controller_96).withIdentifier(997),
                        PrimaryDrawerItem().withName(getString(string.drawer_title_damagecalc)).withIcon(R.drawable.shield).withIdentifier(998).withSelectable(false),
                        //ExpandableDrawerItem().withName(getString(string.drawer_title_loadouts)).withSelectable(false).withIcon(R.drawable.icon_sack).withSubItems(
                                //SecondaryDrawerItem().withIdentifier(302).withName("Loadout Creator").withIcon(R.drawable.loadout_create).withBadge("BETA"),
                                //SecondaryDrawerItem().withIdentifier(301).withName(R.string.drawer_title_bestloadouts).withIcon(R.drawable.loadout_star)
                        //),
                            PrimaryDrawerItem().withName(getString(string.drawer_title_maps)).withSelectable(false).withIcon(R.drawable.map_96).withIdentifier(200),
                            PrimaryDrawerItem().withName(R.string.drawer_title_timer).withSelectable(true).withIcon(R.drawable.stopwatch).withIdentifier(503).withBadge("BETA"),
                        updates
                        ),
                        DividerDrawerItem(),
                        settings,
                        SecondaryDrawerItem().withName(getString(string.drawer_title_suggestion)).withIcon(R.drawable.icon_hint).withSelectable(false).withIdentifier(501),
                        SecondaryDrawerItem().withName(getString(string.drawer_title_share)).withIcon(R.drawable.icons8_share).withSelectable(false).withIdentifier(502)
//                        SwitchDrawerItem().withName("Night Mode").withIcon(R.drawable.icons8_moon_96).withTextColorRes(color).withSelectable(false).withChecked(mSharedPreferences.getBoolean("night_mode", false)).withOnCheckedChangeListener { drawerItem, buttonView, isChecked ->
//                            Log.d("ISCHECKED", "$isChecked")
//                            if (isChecked) {
//                                mSharedPreferences.edit().putBoolean("night_mode", true).apply()
//                            } else {
//                                mSharedPreferences.edit().putBoolean("night_mode", false).apply()
//                            }
//
////                            try {
////
////                                reloadTheme()
////                                scheduledRestart = true
////                                onResume()
////                            } catch (e: Exception) {
////                            }
//                        }
                )
                .withOnDrawerItemClickListener { _, _, drawerItem ->
                    if (drawerItem.identifier.toString() == "9999") {
                        if (FirebaseAuth.getInstance().currentUser == null) {
                            Snacky.builder().setActivity(this).error().setText("You must be logged in to use this feature.").setDuration(Snacky.LENGTH_LONG).setAction("LOGIN") {
                                launchSignIn()
                            }.show()
                            return@withOnDrawerItemClickListener false
                        }

                        startActivity(Intent(this, MainStatsActivity::class.java))
                    }

                    if (drawerItem.identifier.toString() == "610") {
                        updateFragment(VehiclesFragment())
                        toolbar_title.text = "Vehicles"
                        updateToolbarElevation(15f)
                        logDrawerEvent("vehicle")
                    }

                    if (drawerItem.identifier.toString() == "100") {
                        updateFragment(HomeUpdatesFragment())
                        toolbar_title.text = getString(string.drawer_title_update)
                        updateToolbarElevation(15f)
                        logDrawerEvent("updates")
                    }

                    if (drawerItem.identifier.toString() == "1") {
                        updateFragment(HomeWeaponsFragment())
                        toolbar_title.text = getString(string.drawer_title_weapons)
                        updateToolbarElevation(0f)
                        logDrawerEvent("weapons_home")
                    }

                    if (drawerItem.identifier.toString() == "2") {
                        updateFragment(HomeAttachmentsFragment())
                        toolbar_title.text = getString(string.drawer_title_attachments)
                        updateToolbarElevation(0f)
                        logDrawerEvent("attachments")
                    }

                    if (drawerItem.identifier.toString() == "3") {
                        new_fragment = HomeAmmoList()
                        updateFragment(HomeAmmoList())
                        toolbar_title.text = getString(string.drawer_title_ammo)
                        updateToolbarElevation(15f)
                        logDrawerEvent("ammo_home")
                    }

                    if (drawerItem.identifier.toString() == "4") {
                        updateFragment(HomeConsumablesList())
                        toolbar_title.text = getString(string.drawer_title_consumables)
                        updateToolbarElevation(15f)
                        logDrawerEvent("consumables")
                    }

                    if (drawerItem.identifier.toString() == "5") {
                        updateFragment(HomeEquipmentList())
                        toolbar_title.text = getString(string.drawer_title_equipment)
                        updateToolbarElevation(15f)
                        logDrawerEvent("equipment_home")
                    }

                    if (drawerItem.identifier.toString() == "200") {
                        val intentBuilder = CustomTabsIntent.Builder()
                        intentBuilder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        val customTabsIntent = intentBuilder.build()

                        try {
                            customTabsIntent.launchUrl(this, Uri.parse("https://pubgmap.io/"))
                        } catch (e: Exception) {
                            //Crashlytics.logException(e)
                            Snacky.builder().setActivity(this).error().setText("Error loading map.").show()
                        }

                        logDrawerEvent("maps")
                    }

                    if (drawerItem.identifier.toString() == "997") {
                        updateFragment(ControlsFragment())
                        toolbar_title.text = getString(string.drawer_title_controls)
                        updateToolbarElevation(15f)
                        logDrawerEvent("controls")
                    }

                    if (drawerItem.identifier.toString() == "503") {
                        updateFragment(TimerFragment())
                        toolbar_title.text = getString(string.drawer_title_timer)
                        updateToolbarElevation(0f)
                        logDrawerEvent("match_timer")
                    }

                    if (drawerItem.identifier.toString() == "901") {
                        LibsBuilder()
                                .withAboutIconShown(true)
                                .withAboutVersionShown(true)
                                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                                .withActivityTitle("Battlegrounds Battle Buddy")
                                .withAboutDescription("")
                                .start(this)
                    }

                    if (drawerItem.identifier.toString() == "999") {
                        startActivity(Intent(this, LoadoutCreateMain::class.java))
                    }

                    if (drawerItem.identifier.toString() == "998") {
                        startActivity(Intent(this, DamageCalcActivity::class.java))
                        logDrawerEvent("damage_calc")
                    }

                    if (drawerItem.identifier.toString() == "9001") {
                        val buyIntentBundle = iap?.getBuyIntent(3, packageName,
                                "remove_ads", "inapp", "")

                        val pendingIntent = buyIntentBundle?.getParcelable<PendingIntent>("BUY_INTENT")

                        if (pendingIntent != null) {

                            val REQUEST_CODE = 1001
                            startIntentSenderForResult(pendingIntent.intentSender,
                                    REQUEST_CODE, Intent(), Integer.valueOf(0)!!, Integer.valueOf(0)!!,
                                    Integer.valueOf(0)!!)
                        }

                        logDrawerEvent("remove_ads")

                    }

                    if (drawerItem.identifier.toString() == "501") {
                        val mailto = "mailto:fireappsdev@gmail.com" +
                                "?subject=" + Uri.encode("Battlegrounds Battle Buddy Suggestion")

                        val emailIntent = Intent(Intent.ACTION_SENDTO)
                        emailIntent.data = Uri.parse(mailto)
                        startActivity(emailIntent)

                        logDrawerEvent("suggestion")
                    }

                    if (drawerItem.identifier.toString() == "301") {
                        updateFragment(LoadoutBestTabs())
                        toolbar_title.text = getString(string.drawer_title_bestloadouts)
                        updateToolbarElevation(0f)
                        logDrawerEvent("best_loadouts")
                    }

                    if (drawerItem.identifier.toString() == "302") {
                        startActivity(Intent(this, LoadoutCreateMain::class.java))
                    }

                    if (drawerItem.identifier.toString() == "502") {
                        val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
                        sharingIntent.type = "text/plain"
                        val shareBody = "https://play.google.com/store/apps/details?id=com.respondingio.battlegroundsbuddy"
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out Battlegrounds Battle Buddy on the Google Play Store!")
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
                        startActivity(sharingIntent)

                        //val addPlayerBottomSheet = AddPlayerBottomSheet()
                        //addPlayerBottomSheet.show(supportFragmentManager, addPlayerBottomSheet.tag)
                    }

                    if (drawerItem.identifier.toString() == "90001") {
                        launchSignIn()
                    }

                    if (drawerItem.identifier.toString() == "90002") {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    }
                    false
                }
                .build()

        result.setSelection(1)

        if (!mSharedPreferences.getBoolean("removeAds", false)) {
            result.addItem(removeAds)
        }

        //if (BuildConfig.DEBUG) {
            result.addItemAtPosition(PrimaryDrawerItem().withName("Player Stats").withBadge("BETA").withIcon(R.drawable.icons8_chart).withSelectable(false).withIconTintingEnabled(false).withIdentifier(9999), 1)
            result.addItemAtPosition(DividerDrawerItem().withIdentifier(91001), 2)
        //}

        if (FirebaseAuth.getInstance().currentUser != null) {
            //Logged In
            if (FirebaseAuth.getInstance().currentUser!!.isAnonymous) {
                //Anon User, log out and show sign up.
                AuthUI.getInstance().signOut(this)
                if (result.getStickyFooterPosition(90001) == -1) {
                    result.addStickyFooterItem(signInDrawerItem)
                }
            } else {
                notifyLoggedIn(true)

                //Crashlytics.setBool("loggedIn", true)
            }
        } else {
            if (result.getStickyFooterPosition(90001) == -1) {
                result.addStickyFooterItem(signInDrawerItem)
            }

           // Crashlytics.setBool("loggedIn", false)
        }

        loadSteamUserCount()
    }

    private fun notifyLoggedIn(setupAccount: Boolean) {


        val currentUser = FirebaseAuth.getInstance().currentUser

        var displayName = currentUser?.displayName
        if (displayName.isNullOrEmpty()) {
            displayName = currentUser?.email
            if (displayName.isNullOrEmpty()) {
                displayName = currentUser?.phoneNumber
            }
        }

        val header = headerResult.profiles[0]
        header.withName(displayName)
        headerResult.updateProfile(header)

        if (result.getDrawerItem(90002) == null) {
            result.addItemAtPosition(profileItem, 1)
        }

        if (setupAccount)
        setupAccount(currentUser)
    }

    private fun setupAccount(currentUser: FirebaseUser?) {
        val userRef = fbDatabase.getReference("users/" + currentUser?.uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(userSnapshot: DataSnapshot) {
                if (userSnapshot == null || !userSnapshot.exists()) {
                    Log.d("USER", "User not created, creating...")

                    var childUpdates = HashMap<String, Any>()
                    childUpdates["users/" + currentUser?.uid + "/last_logon"] = ServerValue.TIMESTAMP
                    childUpdates["users/" + currentUser?.uid + "/email"] = currentUser?.email.toString()
                    childUpdates["users/" + currentUser?.uid + "/phone"] = currentUser?.phoneNumber.toString()
                    childUpdates["users/" + currentUser?.uid + "/display_name"] = currentUser?.displayName.toString()
                    fbDatabase.reference.updateChildren(childUpdates)
                    return
                }

                Log.d("USER", "User found, updating account info.")

                var childUpdates = HashMap<String, Any>()
                childUpdates["users/" + currentUser?.uid + "/last_logon"] = ServerValue.TIMESTAMP
                childUpdates["users/" + currentUser?.uid + "/email"] = currentUser?.email.toString()
                childUpdates["users/" + currentUser?.uid + "/phone"] = currentUser?.phoneNumber.toString()
                childUpdates["users/" + currentUser?.uid + "/display_name"] = currentUser?.displayName.toString()
                fbDatabase.reference.updateChildren(childUpdates)
            }

        })
    }

    private fun notifySignedOut() {
        val header = headerResult.profiles[0]
        header.withName("Battlegrounds Battle Buddy")
        headerResult.updateProfile(header)

        result.removeItem(90002)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001) {
            val responseCode = data?.getIntExtra("RESPONSE_CODE", 0)
            val purchaseData = data?.getStringExtra("INAPP_PURCHASE_DATA")
            val dataSignature = data?.getStringExtra("INAPP_DATA_SIGNATURE")

            if (resultCode == Activity.RESULT_OK) {
                mSharedPreferences.edit().putBoolean("removeAds", true).apply()
                loadPurchases()
                Snacky.builder().setActivity(this).info().setText("Thanks! Please restart the app to remove ads!").show()
            }
        }

        if (requestCode == 123) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                result.removeAllStickyFooterItems()
                Snacky.builder().setActivity(this).success().setText("Logged In!").show()
                notifyLoggedIn(true)
            } else {
                if (response?.error?.message == null) {
                    Snacky.builder().setActivity(this).error().setText("Unknown Error.").show()
                } else {
                    Snacky.builder().setActivity(this).error().setText(response.error?.message.toString()).show()
                }
            }
        }
    }

    private fun updateFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.main_frame, fragment)
                .commit()
    }

    private fun logDrawerEvent(eventName: String) {
        val bundle2 = Bundle()
        bundle2.putString(FirebaseAnalytics.Param.ITEM_NAME,
                eventName)
        bundle2.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "drawer_select")
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle2)
    }

    private fun isGooglePlayServicesAvailable(activity: Activity): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(activity)
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show()
            } else {
                toast("Google Play Services must be installed for this app to work.")
                finish()
            }
            return false
        }
        return true
    }

    private var serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            iap = null
        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            iap = IInAppBillingService.Stub.asInterface(p1)

            loadPurchases()
        }
    }

    private fun launchSignIn() {
        val requestCode = 123

        val providers: List<AuthUI.IdpConfig> = Arrays.asList(AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.PhoneBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build(),
                AuthUI.IdpConfig.FacebookBuilder().build())

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.mipmap.ic_launcher)
                        .build(),
                requestCode)
    }

    private fun loadSteamUserCount() {
        try {
            val queue = Volley.newRequestQueue(this)
            val url = "https://api.steampowered.com/ISteamUserStats/GetNumberOfCurrentPlayers/v1/?appid=578080"

            val stringRequest = JsonObjectRequest(Request.Method.GET, url, null,
                    Response.Listener { response ->
                        if (!this::headerResult.isInitialized) {
                            return@Listener
                        }
                        val amount = java.lang.Double.parseDouble(response.getJSONObject("response").getString("player_count"))
                        val formatter = DecimalFormat("#,###")
                        val formatted = formatter.format(amount)

                        val header = headerResult.profiles[0]
                        header.withEmail("$formatted Current Steam Players")

                        headerResult.updateProfile(header)
                    },
                    Response.ErrorListener { error ->
                        // TODO: Handle error
                    })


            queue.add(stringRequest)
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
        }

    }

    private fun checkConsent() {
        var consentInformation = ConsentInformation.getInstance(this)
        val publisherIds = arrayOf("pub-1946691221734928")
        consentInformation.requestConsentInfoUpdate(publisherIds, object : ConsentInfoUpdateListener {
            override fun onConsentInfoUpdated(consentStatus: ConsentStatus) {
                // User's consent status successfully updated.
                Log.d("CONSENT", "User's consent status: $consentStatus")
                if (ConsentInformation.getInstance(this@MainActivity).isRequestLocationInEeaOrUnknown && consentStatus == UNKNOWN) {
                    var privacyUrl: URL? = null
                    try {
                        // TODO: Replace with your app's privacy policy URL.
                        privacyUrl = URL("https://www.freeprivacypolicy.com/privacy/view/f7a9373ab150a1a29ce5cc66a224a87e")
                    } catch (e: MalformedURLException) {
                        e.printStackTrace()
                        // Handle error.
                    }

                    val form = ConsentForm.Builder(this@MainActivity, privacyUrl)
                            .withListener(object : ConsentFormListener() {
                                override fun onConsentFormOpened() {
                                    super.onConsentFormOpened()
                                }

                                override fun onConsentFormLoaded() {
                                    super.onConsentFormLoaded()
                                }

                                override fun onConsentFormError(reason: String?) {
                                    super.onConsentFormError(reason)
                                    Log.d("CONSENT", reason)
                                }

                                override fun onConsentFormClosed(consentStatus: ConsentStatus?, userPrefersAdFree: Boolean?) {
                                    super.onConsentFormClosed(consentStatus, userPrefersAdFree)

                                    if (userPrefersAdFree!!) {
                                        val buyIntentBundle = iap?.getBuyIntent(3, packageName,
                                                "remove_ads", "inapp", "")

                                        val pendingIntent = buyIntentBundle?.getParcelable<PendingIntent>("BUY_INTENT")

                                        if (pendingIntent != null) {

                                            val REQUEST_CODE = 1001
                                            startIntentSenderForResult(pendingIntent.intentSender,
                                                    REQUEST_CODE, Intent(), Integer.valueOf(0)!!, Integer.valueOf(0)!!,
                                                    Integer.valueOf(0)!!)
                                        }

                                        return
                                    }

                                    if (consentStatus == PERSONALIZED) {
                                        mSharedPreferences.edit().putBoolean("personalized_ads", true).apply()
                                    } else {
                                        mSharedPreferences.edit().putBoolean("personalized_ads", false).apply()
                                    }
                                }
                            })
                            .withPersonalizedAdsOption()
                            .withNonPersonalizedAdsOption()
                            .withAdFreeOption()
                            .build()

                    form.load()
                    form.show()
                }

                if (consentStatus == PERSONALIZED || consentStatus == UNKNOWN) {
                    mSharedPreferences.edit().putBoolean("personalized_ads", true).apply()
                } else {
                    mSharedPreferences.edit().putBoolean("personalized_ads", false).apply()
                }
            }

            override fun onFailedToUpdateConsentInfo(errorDescription: String) {
                // User's consent status failed to update.
            }
        })
    }


}
