package com.austinhodak.pubgcenter

import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.graphics.Typeface
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.IBinder
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.android.vending.billing.IInAppBillingService
import com.anjlab.android.iab.v3.Constants.BILLING_RESPONSE_RESULT_OK
import com.austinhodak.pubgcenter.ammo.HomeAmmoList
import com.austinhodak.pubgcenter.attachments.HomeAttachmentsFragment
import com.austinhodak.pubgcenter.info.ControlsFragment
import com.austinhodak.pubgcenter.info.TimerFragment
import com.austinhodak.pubgcenter.loadout.LoadoutBestTabs
import com.austinhodak.pubgcenter.loadout.LoadoutCreateMain
import com.austinhodak.pubgcenter.map.MapViewFragment
import com.austinhodak.pubgcenter.weapons.HomeWeaponsFragment
import com.bumptech.glide.Glide
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.marcoscg.ratedialog.RateDialog
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.holder.BadgeStyle
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import de.mateware.snacky.Snacky
import kotlinx.android.synthetic.main.activity_main.appbar
import kotlinx.android.synthetic.main.activity_main.main_toolbar
import kotlinx.android.synthetic.main.activity_main.toolbar_title
import org.jetbrains.anko.toast
import java.util.Arrays


class MainActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    private var iap: IInAppBillingService? = null

    private lateinit var mSharedPreferences: SharedPreferences

    private val removeAds = PrimaryDrawerItem().withIdentifier(9001).withName("Remove Ads").withIcon(R.drawable.icons8_remove_ads_96).withSelectable(false)

    private val signInDrawerItem = SecondaryDrawerItem().withIcon(R.drawable.icons8_password).withSelectable(false).withName("Login or Sign Up").withIdentifier(90001)

    private val profileItem = SecondaryDrawerItem().withIcon(R.drawable.icons8_user).withName("My Profile").withSelectable(false).withIdentifier(90002)

    private var serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            iap = null
        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            iap = IInAppBillingService.Stub.asInterface(p1)

            loadPurchases()
        }
    }

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val phosphate = Typeface.createFromAsset(assets, "fonts/Phosphate-Solid.ttf")
        //toolbar_title.typeface = phosphate
        toolbar_title.text = "Weapons"

        if (!isGooglePlayServicesAvailable(this)) {

            return
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        mSharedPreferences = this.getSharedPreferences("com.austinhodak.pubgcenter", Context.MODE_PRIVATE)

        MobileAds.initialize(this, "ca-app-pub-1946691221734928~1341566099")

        setSupportActionBar(main_toolbar)

        if (!mSharedPreferences.getBoolean("removeAds", false)) {
            //loadAds()
        }

        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance()
        }

        if (mAuth != null && mAuth?.currentUser == null) {
            mAuth!!.signInAnonymously()
                    .addOnCompleteListener(this) {

                    }
        }

        setupDrawer()

        val serviceIntent = Intent("com.android.vending.billing.InAppBillingService.BIND")
        serviceIntent.`package` = "com.android.vending"
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        RateDialog.with(this)
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
                    result.addStickyFooterItem(removeAds)
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

    public override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (iap != null) {
            unbindService(serviceConnection)
        }
    }

    private fun loadAds() {
//        adView.visibility = View.GONE
//        val adRequest = AdRequest.Builder().build()
//        adView.loadAd(adRequest)
//        adView.adListener = object : AdListener() {
//            override fun onAdLoaded() {
//                adView.visibility = View.VISIBLE
//            }
//
//            override fun onAdFailedToLoad(errorCode: Int) {
//                adView.visibility = View.GONE
//            }
//
//            override fun onAdOpened() {
//            }
//
//            override fun onAdLeftApplication() {
//            }
//
//            override fun onAdClosed() {
//            }
//        }
//

    }

    private fun updateToolbarElevation(int: Float) {
        ViewCompat.setElevation(appbar, int)
    }

    private lateinit var result: Drawer

    private lateinit var headerResult: AccountHeader

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
                //.withHeaderBackground(R.drawable.header1)
                .addProfiles(
                        ProfileDrawerItem().withIdentifier(1).withName("BattleGuide for PUBG").withEmail("Level 1 (Free)").withIcon(R.drawable.icon1),
                        ProfileSettingDrawerItem().withName("Logout").withIcon(R.drawable.icons8_logout).withOnDrawerItemClickListener { view, position, drawerItem ->

                            AuthUI.getInstance().signOut(this).addOnCompleteListener {
                                Snacky.builder().setActivity(this).info().setText("Signed Out.").show()

                                notifySignedOut()

                                if (result.getStickyFooterPosition(90001) == -1) {
                                    result.addStickyFooterItem(signInDrawerItem)
                                }
                            }

                            return@withOnDrawerItemClickListener true
                        }
                        //ProfileSettingDrawerItem().withName("Upgrade").withIcon(R.drawable.icons8_buy_upgrade_96)

                )
                .withOnAccountHeaderListener { view, profile, currentProfile -> false }
                .build()

        Glide.with(this).load(R.drawable.header2).into(headerResult.headerBackgroundView)

        val badge = BadgeStyle().withTextColorRes(R.color.md_black_1000).withColorRes(R.color.md_white_1000)

        val updates = SecondaryDrawerItem().withIdentifier(100).withName("Game Updates").withIcon(R.drawable.update1)
        val map1 = SecondaryDrawerItem().withIdentifier(200).withName("Erangel").withBadge("ORIGINAL")
        val map2 = SecondaryDrawerItem().withIdentifier(201).withName("Miramar").withBadge("NEW")
        val map3 = SecondaryDrawerItem().withIdentifier(202).withName("Savage").withBadge("IN DEV")

        val weapons = PrimaryDrawerItem().withIdentifier(1).withName("Weapons").withIcon(R.drawable.icons8_rifle).withIconTintingEnabled(false)
        val attachment = PrimaryDrawerItem().withIdentifier(2).withName("Attachments").withIcon(R.drawable.icons8_magazine).withIconTintingEnabled(false)
        val ammo = PrimaryDrawerItem().withIdentifier(3).withName("Ammo").withIcon(R.drawable.icons8_ammo).withIconTintingEnabled(false)
        val consumables = PrimaryDrawerItem().withIdentifier(4).withName("Consumables").withIcon(R.drawable.icons8_syringe).withIconTintingEnabled(false)
        val equipment = PrimaryDrawerItem().withIdentifier(5).withName("Equipment").withIcon(R.drawable.icons8_helmet).withIconTintingEnabled(false)

        val settings = SecondaryDrawerItem().withIdentifier(901).withName("About").withSelectable(false).withIcon(R.drawable.icons8_info)

        //SecondaryDrawerItem().withName("Create a Loadout").withIcon(R.drawable.loadout_create),

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
                        DividerDrawerItem(),
                        PrimaryDrawerItem().withName("Controls").withIcon(R.drawable.icons8_game_controller_96).withIdentifier(997),
                        PrimaryDrawerItem().withName("Damage Calculator").withIcon(R.drawable.shield).withIdentifier(998).withSelectable(false),
                        ExpandableDrawerItem().withName("Loadouts").withSelectable(false).withIcon(R.drawable.icon_sack).withSubItems(SecondaryDrawerItem().withIdentifier(301).withName("Best Loadouts").withIcon(R.drawable.loadout_star)),
                        PrimaryDrawerItem().withName("Maps").withSelectable(false).withIcon(R.drawable.map_96).withIdentifier(200),
                        DividerDrawerItem(),
                        SecondaryDrawerItem().withName("Match Timer").withSelectable(true).withIcon(R.drawable.stopwatch).withIdentifier(503).withBadge("BETA"),
                        updates,
                        DividerDrawerItem(),
                        settings,
                        SecondaryDrawerItem().withName("Send Suggestion").withIcon(R.drawable.icon_hint).withSelectable(false).withIdentifier(501),
                        SecondaryDrawerItem().withName("Share App").withIcon(R.drawable.icons8_share).withSelectable(false).withIdentifier(502)
                )
                .withOnDrawerItemClickListener { view, position, drawerItem ->
                    if (drawerItem.identifier.toString() == "100") {
                        val homeFragment3 = HomeFragmentBottom()
                        supportFragmentManager.beginTransaction().replace(R.id.main_frame, homeFragment3)
                                .commit()

                        toolbar_title.text = "Updates"

                        updateToolbarElevation(15f)

                        val bundle = Bundle()
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME,
                                "game_updates")
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "drawer_select")
                        mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                    }

                    if (drawerItem.identifier.toString() == "1") {
                        val homeFragment3 = HomeWeaponsFragment()
                        supportFragmentManager.beginTransaction().replace(R.id.main_frame, homeFragment3)
                                .commit()

                        toolbar_title.text = "Weapons"

                        updateToolbarElevation(0f)

                        val bundle = Bundle()
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME,
                                "weapons_home")
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "drawer_select")
                        mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                    }

                    if (drawerItem.identifier.toString() == "2") {
                        val homeFragment2 = HomeAttachmentsFragment()
                        supportFragmentManager.beginTransaction().replace(R.id.main_frame, homeFragment2)
                                .commit()

                        toolbar_title.text = "Attachments"

                        updateToolbarElevation(0f)
                    }

                    if (drawerItem.identifier.toString() == "3") {
                        val homeFragment2 = HomeAmmoList()
                        supportFragmentManager.beginTransaction().replace(R.id.main_frame, homeFragment2)
                                .commit()

                        toolbar_title.text = "Ammo"

                        updateToolbarElevation(15f)

                        val bundle = Bundle()
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME,
                                "ammo_home")
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "drawer_select")
                        mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                    }

                    if (drawerItem.identifier.toString() == "4") {
                        val homeFragment2 = HomeConsumablesList()
                        supportFragmentManager.beginTransaction().replace(R.id.main_frame, homeFragment2)
                                .commit()

                        toolbar_title.text = "Consumables"

                        updateToolbarElevation(15f)

                        val bundle = Bundle()
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME,
                                "consumables_home")
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "drawer_select")
                        mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                    }

                    if (drawerItem.identifier.toString() == "5") {
                        val homeFragment2 = HomeEquipmentList()
                        supportFragmentManager.beginTransaction().replace(R.id.main_frame, homeFragment2)
                                .commit()

                        toolbar_title.text = "Equipment"

                        updateToolbarElevation(15f)

                        val bundle = Bundle()
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME,
                                "equipment_home")
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "drawer_select")
                        mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                    }

                    if (drawerItem.identifier.toString() == "200") {

                        val intentBuilder = CustomTabsIntent.Builder()

                        intentBuilder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))

                        val customTabsIntent = intentBuilder.build()

                        customTabsIntent.launchUrl(this, Uri.parse("https://pubgmap.io/"))

                        //toolbar_title.text = "Erangel"
                        //updateToolbarElevation(15f)

                        val bundle2 = Bundle()
                        bundle2.putString(FirebaseAnalytics.Param.ITEM_NAME,
                                "map_erangel")
                        bundle2.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "drawer_select")
                        mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle2)
                    }

                    if (drawerItem.identifier.toString() == "201") {
                        val mapFrag = MapViewFragment()
                        val bundle = Bundle()
                        bundle.putInt("map", 1)
                        mapFrag.arguments = bundle
                        supportFragmentManager.beginTransaction().replace(R.id.main_frame, mapFrag)
                                .commit()

                        toolbar_title.text = "Miramar"
                        updateToolbarElevation(15f)

                        val bundle2 = Bundle()
                        bundle2.putString(FirebaseAnalytics.Param.ITEM_NAME,
                                "map_miramar")
                        bundle2.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "drawer_select")
                        mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle2)

                    }

                    if (drawerItem.identifier.toString() == "202") {
                        val mapFrag = MapViewFragment()
                        val bundle = Bundle()
                        bundle.putInt("map", 2)
                        mapFrag.arguments = bundle
                        supportFragmentManager.beginTransaction().replace(R.id.main_frame, mapFrag)
                                .commit()

                        toolbar_title.text = "Savage"
                        updateToolbarElevation(15f)

                        val bundle2 = Bundle()
                        bundle2.putString(FirebaseAnalytics.Param.ITEM_NAME,
                                "map_savage")
                        bundle2.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "drawer_select")
                        mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle2)

                    }

                    if (drawerItem.identifier.toString() == "997") {
                        val controlsFragment = ControlsFragment()
                        supportFragmentManager.beginTransaction().replace(R.id.main_frame, controlsFragment)
                                .commit()

                        toolbar_title.text = "Controls"

                        updateToolbarElevation(15f)

                        val bundle2 = Bundle()
                        bundle2.putString(FirebaseAnalytics.Param.ITEM_NAME,
                                "controls_home")
                        bundle2.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "drawer_select")
                        mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle2)
                    }

                    if (drawerItem.identifier.toString() == "503") {
                        val timerFragment = TimerFragment()
                        supportFragmentManager.beginTransaction().replace(R.id.main_frame, timerFragment)
                                .commit()

                        toolbar_title.text = "Match Timer"

                        updateToolbarElevation(0f)
                    }

                    if (drawerItem.identifier.toString() == "901") {
                        LibsBuilder()
                                //provide a style (optional) (LIGHT, DARK, LIGHT_DARK_TOOLBAR)
                                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                                //start the activity
                                .withActivityTitle("BattleGuide for PUBG")
                                .withAboutDescription("<b>Changelog v0.5.0 • 04/05/2018</b><br><br>" +
                                        "• Added parachute icon to weapons that are only found in air drops.<br>" +
                                        "• Controls section now has all controls.<br>" +
                                        "• Added \"Send Suggestion\" button in drawer (Send them!)<br>" +
                                        "• Compare weapons button now works on Android 4.4.4 & below.<br>" +
                                        "• Added Flare Gun + Description<br>" +
                                        "• Added New \"Savage\" Map<br>" +
                                        "• Added a Best Loadouts Section<br>" +
                                        "• Squashin' Bugs ᕙ( ^ ₒ^ c)<br>")
                                .start(this)
                    }

                    if (drawerItem.identifier.toString() == "999") {
                        val intent = Intent(this, LoadoutCreateMain::class.java)
                        startActivity(intent)
                    }

                    if (drawerItem.identifier.toString() == "998") {
                        val intent = Intent(this, DamageCalcActivity::class.java)
                        startActivity(intent)

                        val bundle2 = Bundle()
                        bundle2.putString(FirebaseAnalytics.Param.ITEM_NAME,
                                "damage_calc")
                        bundle2.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "drawer_select")
                        mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle2)
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

                        val bundle2 = Bundle()
                        bundle2.putString(FirebaseAnalytics.Param.ITEM_NAME,
                                "remove_ads")
                        bundle2.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "drawer_select")
                        mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle2)
                    }

                    if (drawerItem.identifier.toString() == "501") {
                        val mailto = "mailto:fireappsdev@gmail.com" +
                                "?subject=" + Uri.encode("BattleGuide for PUBG Suggestion")

                        val emailIntent = Intent(Intent.ACTION_SENDTO)
                        emailIntent.data = Uri.parse(mailto)
                        startActivity(emailIntent)

                        val bundle2 = Bundle()
                        bundle2.putString(FirebaseAnalytics.Param.ITEM_NAME,
                                "suggestion")
                        bundle2.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "drawer_select")
                        mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle2)
                    }

                    if (drawerItem.identifier.toString() == "301") {
                        val loadoutBest = LoadoutBestTabs()
                        supportFragmentManager.beginTransaction().replace(R.id.main_frame, loadoutBest)
                                .commit()

                        toolbar_title.text = "Best Loadouts"

                        updateToolbarElevation(0f)

                        val bundle2 = Bundle()
                        bundle2.putString(FirebaseAnalytics.Param.ITEM_NAME,
                                "best_loadouts")
                        bundle2.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "drawer_select")
                        mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle2)
                    }

                    if (drawerItem.identifier.toString() == "502") {
                        val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
                        sharingIntent.type = "text/plain"
                        val shareBody = "https://play.google.com/store/apps/details?id=com.austinhodak.pubgcenter"
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out BattleGuide for PUBG on the Google Play Store!")
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
                        startActivity(sharingIntent)
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
            //result.addStickyFooterItem(removeAds)
        }

        if (FirebaseAuth.getInstance().currentUser != null) {
            if (FirebaseAuth.getInstance().currentUser!!.isAnonymous) {
                if (result.getStickyFooterPosition(90001) == -1) {
                    result.addStickyFooterItem(signInDrawerItem)
                }
            } else {
                notifyLoggedIn()
            }
        } else {
            if (result.getStickyFooterPosition(90001) == -1) {
                result.addStickyFooterItem(signInDrawerItem)
            }
        }
    }

    private fun launchSignIn() {
        val RC_SIGN_IN = 123

        val providers: List<AuthUI.IdpConfig> = Arrays.asList(AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(),
                AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build())

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.mipmap.ic_launcher_round)
                        .build(),
                RC_SIGN_IN)
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
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                // ...
                result.removeAllStickyFooterItems()

                Snacky.builder().setActivity(this).success().setText("Logged In!").show()

                notifyLoggedIn()
            } else {
                // Sign in failed, check response for error code
                // ...
                Snacky.builder().setActivity(this).error().setText("Login Failed.").show()
            }
        }
    }

    private fun notifyLoggedIn() {
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

        result.addItemAtPosition(profileItem, 1)
        result.addItemAtPosition(DividerDrawerItem().withIdentifier(91001), 2)
    }

    private fun notifySignedOut() {
        val header = headerResult.profiles[0]
        header.withName("BattleGuide for PUBG")
        headerResult.updateProfile(header)

        result.removeItem(90002)
        result.removeItem(91001)
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
}
