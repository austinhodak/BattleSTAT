package com.austinh.battlebuddy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.ads.consent.*
import com.google.ads.consent.ConsentStatus.PERSONALIZED
import com.google.ads.consent.ConsentStatus.UNKNOWN
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.instabug.bug.BugReporting
import com.instabug.library.Instabug
import com.marcoscg.ratedialog.RateDialog
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.*
import com.austinh.battlebuddy.R.string
import com.austinh.battlebuddy.ammo.HomeAmmoList
import com.austinh.battlebuddy.attachments.HomeAttachmentsFragment
import com.austinh.battlebuddy.damage_calculator.DamageCalcActivity
import com.austinh.battlebuddy.info.ControlsFragment
import com.austinh.battlebuddy.info.TimerFragment
import com.austinh.battlebuddy.loadout.LoadoutBestTabs
import com.austinh.battlebuddy.loadout.LoadoutCreateMain
import com.austinh.battlebuddy.models.SeasonStatsAll
import com.austinh.battlebuddy.premium.UpgradeActivity
import com.austinh.battlebuddy.rss.HomeUpdatesFragment
import com.austinh.battlebuddy.snacky.Snacky
import com.austinh.battlebuddy.stats.MainStatsActivity
import com.austinh.battlebuddy.stats.PlayerListDialog
import com.austinh.battlebuddy.utils.Ads
import com.austinh.battlebuddy.utils.Premium
import com.austinh.battlebuddy.utils.Ranks
import com.austinh.battlebuddy.utils.Seasons
import com.austinh.battlebuddy.weapons.HomeWeaponsFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.browse
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast
import java.net.MalformedURLException
import java.net.URL
import java.util.Arrays
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.set

public class MainActivity : AppCompatActivity() {

    private lateinit var result: Drawer

    private lateinit var headerResult: AccountHeader

    private var mAuth: FirebaseAuth? = null

    private lateinit var mSharedPreferences: SharedPreferences

    lateinit var newSharedPreferences: SharedPreferences

    private val removeAds = SecondaryDrawerItem().withIdentifier(9001).withName(R.string.drawer_title_upgrade).withIcon(R.drawable.upgrade).withSelectable(false)

    private val signInDrawerItem = SecondaryDrawerItem().withIcon(R.drawable.icons8_password).withSelectable(false).withName(R.string.drawer_title_login).withIdentifier(90001)

    private val profileItem = PrimaryDrawerItem().withIcon(R.drawable.chart_color).withName(R.string.drawer_title_stats_enw).withBadge("NEW").withSelectable(false).withIdentifier(90002)

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    private lateinit var fbDatabase: FirebaseDatabase

    private lateinit var mInterstitialAd: InterstitialAd

    lateinit private var billingClient: BillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!isGooglePlayServicesAvailable(this)) {
            //Play Services Not Available, stop.
            return
        }

        checkConsent()

        if (intent != null) {
            if (intent.hasExtra("url")) {
                browse(intent.getStringExtra("url"), true)
            }
        }

        mSharedPreferences = this.getSharedPreferences("com.austinhodak.pubgcenter", Context.MODE_PRIVATE)
        newSharedPreferences = this.getSharedPreferences("com.austinh.battlebuddy", Context.MODE_PRIVATE)

        setSupportActionBar(main_toolbar)
        toolbar_title.text = getString(R.string.drawer_title_weapons)

        initializeFirebase()

        setupDrawer()
        //setupNewDrawer()

        RateDialog.with(this, 1, 5)

        updatePremiumStuff()

        billingClient = BillingClient.newBuilder(this).setListener { responseCode, purchases ->
            if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
                for (purchase in purchases) {
                    Premium.handlePurchase(purchase)
                }

                updatePremiumStuff()
            } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
            } else {
                // Handle any other error codes.
            }
        }.build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {}

            override fun onBillingSetupFinished(responseCode: Int) {
                if (responseCode == BillingClient.BillingResponse.OK) {
                    // The billing client is ready. You can query purchases here.
                    val purchases = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
                    for (purchase in purchases.purchasesList) {
                        Premium.handlePurchase(purchase)
                    }

                    updatePremiumStuff()
                }
            }
        })

        if (!Premium.isAdFreeUser()) {
            mInterstitialAd = InterstitialAd(this)
            mInterstitialAd.adUnitId = "ca-app-pub-1946691221734928/5517720061"
            mInterstitialAd.loadAd(Ads.getAdBuilder())


            val adView = com.google.android.gms.ads.AdView(this)
            adView.adSize = com.google.android.gms.ads.AdSize.BANNER
            adView.adUnitId = "ca-app-pub-1946691221734928/9814981003"
            //adView.loadAd(Ads.getAdBuilder())
            //main_ll?.addView(adView)
        }
    }

    private fun updatePremiumStuff() {
        if (!this::headerResult.isInitialized) return
        val header = headerResult.profiles[0]
        val string: String = when (Premium.getUserLevel()) {
            Premium.Level.FREE -> {
                "Free"
            }
            Premium.Level.LEVEL_1 -> {
                "Level 1"
            }
            Premium.Level.LEVEL_2 -> {
                "Level 2"
            }
            Premium.Level.LEVEL_3 -> {
                "Level 3"
            }
        }
        headerResult.updateProfile(header)
        header.withEmail(string)
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
            if (Premium.isAdFreeUser()) {
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
        val launchCount = mSharedPreferences.getInt("launchCount", 0)
        Log.d("LAUNCH", "$launchCount")
        if (launchCount >= 2) {
            try {
                if (mInterstitialAd.isLoaded) {
                    mInterstitialAd.show()
                    mSharedPreferences.edit().putInt("launchCount", 0).apply()
                } else {
                    super.onBackPressed()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                super.onBackPressed()
            }
        } else {
            mSharedPreferences.edit().putInt("launchCount", launchCount + 1).apply()
            super.onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isGooglePlayServicesAvailable(this)) {
            return
        }

        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            if (!FirebaseAuth.getInstance().currentUser?.isAnonymous!!)
            notifyLoggedIn(false)
        }
    }

    private fun updateToolbarElevation(int: Float) {
        ViewCompat.setElevation(appbar, int)
    }

    private var new_fragment: Fragment? = null

    private fun setupDrawer() {

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            supportActionBar?.elevation = 0.0f
            appbar.elevation = 0.0f
        }

        headerResult = AccountHeaderBuilder()
                .withActivity(this)
                .withDividerBelowHeader(false)
                .addProfiles(
                        ProfileDrawerItem().withIdentifier(1).withName(R.string.app_name).withEmail("Level 1").withIcon(R.drawable.icon1)
                )
                .withOnAccountHeaderListener { _, _, _ -> false }
                .build()

        Glide.with(this).load(R.drawable.header).into(headerResult.headerBackgroundView)

        val updates = PrimaryDrawerItem().withIdentifier(100).withName(R.string.drawer_title_update).withIcon(R.drawable.rss)
        val weapons = PrimaryDrawerItem().withIdentifier(1).withName(R.string.drawer_title_weapons).withIcon(R.drawable.icons8_rifle).withIconTintingEnabled(false)
        val attachment = PrimaryDrawerItem().withIdentifier(2).withName(R.string.drawer_title_attachments).withIcon(R.drawable.icons8_magazine).withIconTintingEnabled(false)
        val ammo = PrimaryDrawerItem().withIdentifier(3).withName(R.string.drawer_title_ammo).withIcon(R.drawable.icons8_ammo).withIconTintingEnabled(false)
        val consumables = PrimaryDrawerItem().withIdentifier(4).withName(R.string.drawer_title_consumables).withIcon(R.drawable.icons8_syringe).withIconTintingEnabled(false)
        val equipment = PrimaryDrawerItem().withIdentifier(5).withName(R.string.drawer_title_equipment).withIcon(R.drawable.icons8_helmet).withIconTintingEnabled(false)
        val settings = SecondaryDrawerItem().withIdentifier(901).withName(R.string.drawer_title_about).withSelectable(false).withIcon(R.drawable.icons8_info)

        result = DrawerBuilder()
                .withActivity(this)
                .withToolbar(main_toolbar)
                .withHeader(R.layout.main_drawer_header)
                .withHeaderDivider(false)
                //.withAccountHeader(headerResult)
                .addDrawerItems(
                        weapons,
                        attachment,
                        ammo,
                        equipment,
                        consumables,
                        PrimaryDrawerItem().withName(getString(string.drawer_title_vehicles)).withIcon(R.drawable.icons8_car).withIdentifier(610),
                        //DividerDrawerItem(),
                        ExpandableDrawerItem().withName(getString(string.drawer_title_more)).withIcon(R.drawable.icons8_view_more_96).withSelectable(false).withSubItems(
                                DividerDrawerItem(),
                                PrimaryDrawerItem().withName(getString(string.drawer_title_alerts)).withBadge("BETA").withIcon(R.drawable.notification).withSelectable(false).withOnDrawerItemClickListener { view, position, drawerItem ->
                                    startActivity<AlertManager>()
                                    true
                                },
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
                        SecondaryDrawerItem().withName(getString(R.string.drawer_title_suggestion)).withIcon(R.drawable.icon_hint).withSelectable(false).withIdentifier(501),
                        SecondaryDrawerItem().withName(getString(string.drawer_title_share)).withIcon(R.drawable.icons8_share).withSelectable(false).withIdentifier(502)

                )
                .withOnDrawerItemClickListener { _, _, drawerItem ->
                    if (drawerItem.identifier.toString() == "9999") {
//                        if (FirebaseAuth.getInstance().currentUser == null) {
//                            Snacky.builder().setActivity(this).warning().setText("You must be logged in to use this feature.").setDuration(BaseTransientBottomBar.LENGTH_LONG).setAction("LOGIN") {
//                                launchSignIn()
//                            }.show()
//                            return@withOnDrawerItemClickListener false
//                        }

                        Handler().postDelayed({
                            startActivity(Intent(this, MainStatsActivity::class.java))
                        }, 400)
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
                        Log.d("MainApp", "weapons_home")
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
                            Snacky.builder().setActivity(this).error().setText("Error loading map.").show()
                        }

                        //startActivity<MapDropRouletteActivity>()

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
//                        val flowParams = BillingFlowParams.newBuilder()
//                                .setSku("level_1")
//                                .setType(BillingClient.SkuType.INAPP)
//                                .build()
//
//                        val responseCode = billingClient.launchBillingFlow(this@MainActivity, flowParams)
//
//                        logDrawerEvent("remove_ads")

                        startActivityForResult<UpgradeActivity>(100)
                    }

                    if (drawerItem.identifier.toString() == "501") {
                        BugReporting.invoke()

                        logDrawerEvent("feedback")
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
                        val shareBody = "https://play.google.com/store/apps/details?id=com.austinh.battlebuddy"
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out Battlegrounds Battle Buddy on the Google Play Store!")
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
                        startActivity(sharingIntent)
                    }

                    if (drawerItem.identifier.toString() == "90001") {
                        launchSignIn()
                    }

                    if (drawerItem.identifier.toString() == "90002") {
                        startActivity<PlayerListDialog>()
                        //startActivity(Intent(this, ProfileActivity::class.java))
                    }
                    false
                }
                .build()

        result.recyclerView.isVerticalScrollBarEnabled = false

        result.setSelection(1)

        if (Premium.getUserLevel() != Premium.Level.LEVEL_3) {
            result.addItem(removeAds)
        }

        result.addItemAtPosition(PrimaryDrawerItem().withName("Player Stats").withBadge("OLD").withIcon(R.drawable.icons8_chart).withSelectable(false).withIconTintingEnabled(false).withIdentifier(9999), 1)
        result.addItemAtPosition(DividerDrawerItem().withIdentifier(91001), 2)

        if (FirebaseAuth.getInstance().currentUser != null) {
            //User is not null, either logged in or anon.
            if (FirebaseAuth.getInstance().currentUser?.isAnonymous!!) {
                //User is anon, show login signup stuff.
                if (result.getStickyFooterPosition(90001) == -1) {
                    result.addStickyFooterItem(signInDrawerItem)
                }
            } else {
                //User is logged in
                notifyLoggedIn(true)
            }
        } else {
            FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener {
                //User is anon logged in.
            }

            if (result.getStickyFooterPosition(90001) == -1) {
                result.addStickyFooterItem(signInDrawerItem)
            }
        }

        updateHeader()
    }

    private fun notifyLoggedIn(setupAccount: Boolean) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        Log.d("USER", currentUser?.isAnonymous.toString())

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

        headerResult.removeProfileByIdentifier(9999)

        headerResult.addProfile(ProfileSettingDrawerItem().withIdentifier(9999).withName("Logout").withIcon(R.drawable.icons8_logout).withOnDrawerItemClickListener { _, _, _ ->
            mSharedPreferences.edit().remove("stats_selected_player").remove("stats_selected_player_id").apply()
            newSharedPreferences.edit().remove("selected-player-id").apply()

            AuthUI.getInstance().signOut(this).addOnCompleteListener {
                Snacky.builder().setActivity(this).info().setText("Signed Out.").show()

                notifySignedOut()

                if (result.getStickyFooterPosition(90001) == -1) {
                    result.addStickyFooterItem(signInDrawerItem)
                }

                FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener {
                    //User is anon logged in.
                }
            }
            return@withOnDrawerItemClickListener true
        }, 1)
    }

    private fun setupAccount(currentUser: FirebaseUser?) {
        val userRef = fbDatabase.getReference("users/" + currentUser?.uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(userSnapshot: DataSnapshot) {
                if (userSnapshot == null || !userSnapshot.exists()) {
                    Log.d("USER", "User not created, creating...")

                    var childUpdates = HashMap<String, Any?>()
                    childUpdates["users/" + currentUser?.uid + "/last_logon"] = ServerValue.TIMESTAMP
                    childUpdates["users/" + currentUser?.uid + "/email"] = null
                    childUpdates["users/" + currentUser?.uid + "/phone"] = null
                    childUpdates["users/" + currentUser?.uid + "/display_name"] = currentUser?.displayName.toString()
                    fbDatabase.reference.updateChildren(childUpdates)
                    return
                }

                Log.d("USER", "User found, updating account info." + currentUser?.email)

                var childUpdates = HashMap<String, Any?>()
                childUpdates["users/" + currentUser?.uid + "/last_logon"] = ServerValue.TIMESTAMP
                childUpdates["users/" + currentUser?.uid + "/email"] = null
                childUpdates["users/" + currentUser?.uid + "/phone"] = null
                childUpdates["users/" + currentUser?.uid + "/display_name"] = currentUser?.displayName.toString()
                fbDatabase.reference.updateChildren(childUpdates)
            }

        })
    }

    private fun notifySignedOut() {
        val header = headerResult.profiles[0]
        header.withName("Battle Buddy")
        headerResult.updateProfile(header)

        result.removeItem(90002)

        Instabug.logoutUser()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && data != null) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                result.removeAllStickyFooterItems()
                Snacky.builder().setActivity(this).success().setText("Logged In!").show()
                notifyLoggedIn(true)
            } else {
                when {
                    response?.error?.message == null -> Snacky.builder().setActivity(this).error().setText("Unknown Error.").show()
                    response.error?.errorCode == ErrorCodes.ANONYMOUS_UPGRADE_MERGE_CONFLICT -> {
                        val anonUserID = FirebaseAuth.getInstance().currentUser!!.uid
                        val nonAnonCredential = response.credentialForLinking

                        FirebaseDatabase.getInstance().getReference("/users/$anonUserID").addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {}

                            override fun onDataChange(p0: DataSnapshot) {
                                if (!p0.exists()) return

                                FirebaseAuth.getInstance().signInWithCredential(nonAnonCredential!!).addOnSuccessListener {
                                    notifyLoggedIn(true)
                                    result.removeAllStickyFooterItems()
                                    if (p0.hasChild("pubg_players")) {
                                        val pubgPlayers = HashMap<String, Any>()
                                        for (child in p0.child("pubg_players").children) {
                                            pubgPlayers["/users/${it.user.uid}/pubg_players/${child.key}"] = child.value!!
                                        }

                                        FirebaseDatabase.getInstance().reference.updateChildren(pubgPlayers)
                                    }
                                }
                            }
                        })
                    }
                    else -> Snacky.builder().setActivity(this).error().setText(response.error?.errorCode.toString()).show()
                }
            }
        }

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            //Purchase Successful
            Snacky.builder().setActivity(this@MainActivity).success().setText("Thank you for your purchase!").show()
            updatePremiumStuff()
        }
    }

    private fun updateFragment(fragment: Fragment) {
        if (supportFragmentManager.findFragmentById(R.id.main_frame) != null) {
            supportFragmentManager.beginTransaction().remove(supportFragmentManager.findFragmentById(R.id.main_frame)!!).commitAllowingStateLoss()

            Handler().postDelayed({
                supportFragmentManager.beginTransaction().replace(R.id.main_frame, fragment).setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .commitAllowingStateLoss()
            }, 400)
        } else {
            supportFragmentManager.beginTransaction().replace(R.id.main_frame, fragment).setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .commitAllowingStateLoss()
        }
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

    private fun launchSignIn() {
        val requestCode = 123

        val providers: List<AuthUI.IdpConfig> = Arrays.asList(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.PhoneBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build(),
                AuthUI.IdpConfig.FacebookBuilder().build(),
                AuthUI.IdpConfig.TwitterBuilder().build(),
                AuthUI.IdpConfig.AnonymousBuilder().build())

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .enableAnonymousUsersAutoUpgrade()
                        .setAvailableProviders(providers)
                        .setLogo(R.mipmap.ic_launcher)
                        .setTheme(R.style.SignInTheme)
                        .build(),
                requestCode)
    }

    var consentForm: ConsentForm? = null

    private fun checkConsent() {
        //TESTING GDPR
        //ConsentInformation.getInstance(this).addTestDevice("FBE7B6C060C778D1A44EF3F2184E089B")
        //ConsentInformation.getInstance(this).debugGeography = DebugGeography.DEBUG_GEOGRAPHY_EEA
        /**/

        val consentInformation = ConsentInformation.getInstance(this)
        val publisherIds = arrayOf("pub-1946691221734928")
        consentInformation.requestConsentInfoUpdate(publisherIds, object : ConsentInfoUpdateListener {
            override fun onConsentInfoUpdated(consentStatus: ConsentStatus) {
                // User's consent status successfully updated.
                Log.d("CONSENT", "User's consent status: $consentStatus")
                if (ConsentInformation.getInstance(this@MainActivity).isRequestLocationInEeaOrUnknown && consentStatus == UNKNOWN) {
                    var privacyUrl: URL? = null
                    try {
                        privacyUrl = URL("https://www.freeprivacypolicy.com/privacy/view/f7a9373ab150a1a29ce5cc66a224a87e")
                    } catch (e: MalformedURLException) {
                        e.printStackTrace()
                    }

                    consentForm = ConsentForm.Builder(this@MainActivity, privacyUrl)
                            .withListener(object : ConsentFormListener() {
                                override fun onConsentFormOpened() {
                                    super.onConsentFormOpened()
                                }

                                override fun onConsentFormLoaded() {
                                    super.onConsentFormLoaded()
                                    consentForm?.show()
                                }

                                override fun onConsentFormError(reason: String?) {
                                    super.onConsentFormError(reason)
                                    Log.d("CONSENT", reason)
                                }

                                override fun onConsentFormClosed(consentStatus: ConsentStatus?, userPrefersAdFree: Boolean?) {
                                    super.onConsentFormClosed(consentStatus, userPrefersAdFree)

                                    if (userPrefersAdFree!!) {
                                        startActivity<UpgradeActivity>()
                                        return
                                    }


                                    Ads.setConsentGiven(consentStatus == PERSONALIZED)
                                    ConsentInformation.getInstance(this@MainActivity).consentStatus = consentStatus
                                }
                            })
                            .withPersonalizedAdsOption()
                            .withNonPersonalizedAdsOption()
                            .withAdFreeOption()
                            .build()

                    consentForm?.load()
                }

                Ads.setConsentGiven(consentStatus == PERSONALIZED || consentStatus == UNKNOWN)
            }

            override fun onFailedToUpdateConsentInfo(errorDescription: String) {
                // User's consent status failed to update.
            }
        })
    }

    private fun updateHeader() {
        val levelText = result.header.findViewById<TextView>(R.id.header_upgrade)
        val headerText = result.header.findViewById<TextView>(R.id.header_name)

        levelText.text = Premium.getLevelText(Premium.getUserLevel())
        headerText.text = "Battle Buddy"

        FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().currentUser?.uid}/pubgAccountID").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                val icon = result.header.findViewById<ImageView>(R.id.header_rank_icon)
                val pubgName = result.header.findViewById<TextView>(R.id.header_ingame_name)
                if (!p0.exists()) {
                    icon.visibility = View.GONE
                    return
                }



                FirebaseDatabase.getInstance().getReference("user_stats/${p0.value}/season_data/pc-na/${Seasons.pcCurrentSeason}/stats").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(data: DataSnapshot) {
                        if (!data.exists()) {
                            FirebaseDatabase.getInstance().getReference("user_stats/${p0.value}/season_data/xbox-na/${Seasons.xboxCurrentSeason}/stats").addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {

                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    if (!p0.exists()) return

                                    Log.d("HEADER", p0.value.toString())

                                    val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()

                                    val seasonStats = p0.getValue(SeasonStatsAll::class.java)!!
                                    val pointsList: MutableList<Double> = ArrayList()

                                    pointsList.add(seasonStats.solo.rankPoints)
                                    pointsList.add(seasonStats.`solo-fpp`.rankPoints)
                                    pointsList.add(seasonStats.duo.rankPoints)
                                    pointsList.add(seasonStats.`duo-fpp`.rankPoints)
                                    pointsList.add(seasonStats.squad.rankPoints)
                                    pointsList.add(seasonStats.`squad-fpp`.rankPoints)

                                    pointsList.sort()
                                    pointsList.reverse()

                                    icon.visibility = View.VISIBLE

                                    Glide.with(applicationContext)
                                            .load(Ranks.getRankIcon(pointsList[0]))
                                            .transition(DrawableTransitionOptions.withCrossFade(factory))
                                            .into(icon)


                                }
                            })
                            Log.d("HEADER", p0.value.toString())
                            return
                        }

                        val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()

                        val seasonStats = data.getValue(SeasonStatsAll::class.java)!!
                        val pointsList: MutableList<Double> = ArrayList()

                        pointsList.add(seasonStats.solo.rankPoints)
                        pointsList.add(seasonStats.`solo-fpp`.rankPoints)
                        pointsList.add(seasonStats.duo.rankPoints)
                        pointsList.add(seasonStats.`duo-fpp`.rankPoints)
                        pointsList.add(seasonStats.squad.rankPoints)
                        pointsList.add(seasonStats.`squad-fpp`.rankPoints)

                        pointsList.sort()
                        pointsList.reverse()

                        icon.visibility = View.VISIBLE

                        Glide.with(applicationContext)
                                .load(Ranks.getRankIcon(pointsList[0]))
                                .transition(DrawableTransitionOptions.withCrossFade(factory))
                                .into(icon)
                    }
                })
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::billingClient.isInitialized && billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}
