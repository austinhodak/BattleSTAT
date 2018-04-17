package com.austinhodak.pubgcenter

import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.graphics.Color
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
import android.widget.ImageView
import com.afollestad.materialdialogs.MaterialDialog
import com.android.vending.billing.IInAppBillingService
import com.anjlab.android.iab.v3.Constants.BILLING_RESPONSE_RESULT_OK
import com.austinhodak.pubgcenter.R.string
import com.austinhodak.pubgcenter.ammo.HomeAmmoList
import com.austinhodak.pubgcenter.attachments.HomeAttachmentsFragment
import com.austinhodak.pubgcenter.damage_calculator.DamageCalcActivity
import com.austinhodak.pubgcenter.info.ControlsFragment
import com.austinhodak.pubgcenter.info.TimerFragment
import com.austinhodak.pubgcenter.loadout.LoadoutBestTabs
import com.austinhodak.pubgcenter.loadout.LoadoutCreateMain
import com.austinhodak.pubgcenter.profile.ProfileActivity
import com.austinhodak.pubgcenter.weapons.HomeWeaponsFragment
import com.bumptech.glide.Glide
import com.crashlytics.android.Crashlytics
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
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
import de.mateware.snacky.Snacky
import kotlinx.android.synthetic.main.activity_main.appbar
import kotlinx.android.synthetic.main.activity_main.main_toolbar
import kotlinx.android.synthetic.main.activity_main.toolbar_title
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast
import java.io.File
import java.util.Arrays

public class MainActivity : AppCompatActivity() {

    private lateinit var result: Drawer

    private lateinit var headerResult: AccountHeader

    private var mAuth: FirebaseAuth? = null

    private var iap: IInAppBillingService? = null

    private lateinit var mSharedPreferences: SharedPreferences

    private val removeAds = PrimaryDrawerItem().withIdentifier(9001).withName("Remove Ads").withIcon(R.drawable.icons8_remove_ads_96).withSelectable(false)

    private val signInDrawerItem = SecondaryDrawerItem().withIcon(R.drawable.icons8_password).withTextColor(Color.WHITE).withSelectable(false).withName("Login or Sign Up").withIdentifier(90001)

    private val profileItem = SecondaryDrawerItem().withIcon(R.drawable.icons8_user).withTextColor(Color.WHITE).withName("My Profile").withSelectable(false).withIdentifier(90002)

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    private lateinit var fbDatabase: FirebaseDatabase

    private lateinit var mInterstitialAd: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (!isGooglePlayServicesAvailable(this)) {
            //Play Services Not Available, stop.
            return
        }

        checkIfMobileFilesExists()

        setSupportActionBar(main_toolbar)
        toolbar_title.text = getString(R.string.drawer_title_weapons)

        initializeFirebase()

        mSharedPreferences = this.getSharedPreferences("com.austinhodak.pubgcenter", Context.MODE_PRIVATE)

        setupDrawer()

        val serviceIntent = Intent("com.android.vending.billing.InAppBillingService.BIND")
        serviceIntent.`package` = "com.android.vending"
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        RateDialog.with(this, 1, 5)

        MobileAds.initialize(this,
                "ca-app-pub-2379265021766723~5729735109")

        if (!mSharedPreferences.getBoolean("removeAds", false)) {
            mInterstitialAd = InterstitialAd(this)
            mInterstitialAd.adUnitId = "ca-app-pub-2379265021766723/5346591720"
            mInterstitialAd.loadAd(AdRequest.Builder().build())
            mInterstitialAd.adListener = object : AdListener() {
                override fun onAdClosed() {
                    finish()
                }
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
        }
        if (this::mSharedPreferences.isInitialized && mSharedPreferences.getBoolean("removeAds", false)) {
            super.onBackPressed()
            return
        } else if (!this::mSharedPreferences.isInitialized) {
            super.onBackPressed()
            return
        }
        var launchCount = mSharedPreferences.getInt("launchCount", 0)
        if (launchCount >= 3) {
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

    override fun onStop() {
        super.onStop()

    }

    override fun onDestroy() {
        super.onDestroy()
        if (iap != null) {
            unbindService(serviceConnection)
        }
    }

    override fun onResume() {
        super.onResume()
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            notifyLoggedIn(false)
        }
    }

    private fun updateToolbarElevation(int: Float) {
        ViewCompat.setElevation(appbar, int)
    }

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
                        ProfileDrawerItem().withIdentifier(1).withName("BattleGuide for PUBG").withEmail("Level 1 (Free)").withIcon(R.drawable.icon1),
                        ProfileSettingDrawerItem().withName("Logout").withIcon(R.drawable.icons8_logout).withOnDrawerItemClickListener { _, _, _ ->

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

        val updates = SecondaryDrawerItem().withIdentifier(100).withTextColor(Color.WHITE).withName(R.string.drawer_title_update).withIcon(R.drawable.rss)
        val weapons = PrimaryDrawerItem().withIdentifier(1).withName(R.string.drawer_title_weapons).withIcon(R.drawable.icons8_rifle).withIconTintingEnabled(false)
        val attachment = PrimaryDrawerItem().withIdentifier(2).withName(R.string.drawer_title_attachments).withIcon(R.drawable.icons8_magazine).withIconTintingEnabled(false)
        val ammo = PrimaryDrawerItem().withIdentifier(3).withName(R.string.drawer_title_ammo).withIcon(R.drawable.icons8_ammo).withIconTintingEnabled(false)
        val consumables = PrimaryDrawerItem().withIdentifier(4).withName(R.string.drawer_title_consumables).withIcon(R.drawable.icons8_syringe).withIconTintingEnabled(false)
        val equipment = PrimaryDrawerItem().withIdentifier(5).withName(R.string.drawer_title_equipment).withIcon(R.drawable.icons8_helmet).withIconTintingEnabled(false)
        val settings = SecondaryDrawerItem().withIdentifier(901).withName(R.string.drawer_title_about).withSelectable(false).withIcon(R.drawable.icons8_info)

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
                        DividerDrawerItem(),
                        PrimaryDrawerItem().withName(R.string.drawer_title_controls).withIcon(R.drawable.icons8_game_controller_96).withIdentifier(997),
                        PrimaryDrawerItem().withName(getString(string.drawer_title_damagecalc)).withIcon(R.drawable.shield).withIdentifier(998).withSelectable(false),
                        ExpandableDrawerItem().withName(getString(string.drawer_title_loadouts)).withSelectable(false).withIcon(R.drawable.icon_sack).withSubItems(SecondaryDrawerItem().withIdentifier(301).withName(R.string.drawer_title_bestloadouts).withIcon(R.drawable.loadout_star)),
                        PrimaryDrawerItem().withName(getString(string.drawer_title_maps)).withSelectable(false).withIcon(R.drawable.map_96).withIdentifier(200),
                        //DividerDrawerItem(),
                        SecondaryDrawerItem().withName(R.string.drawer_title_timer).withTextColor(Color.WHITE).withSelectable(true).withIcon(R.drawable.stopwatch).withIdentifier(503).withBadge("BETA").withTextColor(Color.WHITE),
                        updates,
                        DividerDrawerItem(),
                        SecondaryDrawerItem().withName("Win Skins with Hellcase").withIcon(R.drawable.icons8_fire).withSelectable(false).withIdentifier(504),
                        settings,
                        SecondaryDrawerItem().withName(getString(string.drawer_title_suggestion)).withIcon(R.drawable.icon_hint).withSelectable(false).withIdentifier(501),
                        SecondaryDrawerItem().withName(getString(string.drawer_title_share)).withIcon(R.drawable.icons8_share).withSelectable(false).withIdentifier(502)
                )
                .withOnDrawerItemClickListener { _, _, drawerItem ->
                    if (drawerItem.identifier.toString() == "504") {
                        val dialog = MaterialDialog.Builder(this)
                                .customView(R.layout.hellcase1, false)
                                .show()

                        val view = dialog.customView
                        val imageView = view?.find<ImageView>(R.id.hellcase)
                        imageView?.onClick {
                            val i = Intent(Intent.ACTION_VIEW)
                            i.data = Uri.parse("https://pubg.hellcase.com/fbattleguide")
                            startActivity(i)
                        }
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
                        customTabsIntent.launchUrl(this, Uri.parse("https://pubgmap.io/"))

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
                                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                                .withActivityTitle("BattleGuide for PUBG")
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
                                "?subject=" + Uri.encode("BattleGuide for PUBG Suggestion")

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
            result.addStickyFooterItem(removeAds)
        }

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

                Crashlytics.setBool("loggedIn", true)
            }
        } else {
            if (result.getStickyFooterPosition(90001) == -1) {
                result.addStickyFooterItem(signInDrawerItem)
            }

            Crashlytics.setBool("loggedIn", false)
        }
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
            result.addItemAtPosition(DividerDrawerItem().withIdentifier(91001), 2)
        }

        if (setupAccount)
        setupAccount(currentUser)
    }

    private fun setupAccount(currentUser: FirebaseUser?) {
        val userRef = fbDatabase.getReference("users/" + currentUser?.uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(userSnapshot: DataSnapshot?) {
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
        header.withName("BattleGuide for PUBG")
        headerResult.updateProfile(header)

        result.removeItem(90002)
        result.removeItem(91001)
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
                Snacky.builder().setActivity(this).error().setText(response?.error?.message.toString()).show()
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
                requestCode)
    }
}
