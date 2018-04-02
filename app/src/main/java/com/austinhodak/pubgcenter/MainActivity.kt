package com.austinhodak.pubgcenter

import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.IBinder
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.android.vending.billing.IInAppBillingService
import com.anjlab.android.iab.v3.Constants.BILLING_RESPONSE_RESULT_OK
import com.appodeal.ads.Appodeal
import com.austinhodak.pubgcenter.ammo.HomeAmmoList
import com.austinhodak.pubgcenter.attachments.HomeAttachmentsFragment
import com.austinhodak.pubgcenter.loadout.LoadoutCreateMain
import com.austinhodak.pubgcenter.map.MapViewFragment
import com.austinhodak.pubgcenter.weapons.HomeWeaponsFragment
import com.bumptech.glide.Glide
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuth
import com.marcoscg.ratedialog.RateDialog
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


class MainActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    private var iap: IInAppBillingService? = null

    private lateinit var mSharedPreferences: SharedPreferences

    private var serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            iap = null
        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            iap = IInAppBillingService.Stub.asInterface(p1)

            loadPurchases()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val phosphate = Typeface.createFromAsset(assets, "fonts/Phosphate-Solid.ttf")
        toolbar_title.typeface = phosphate
        toolbar_title.text = "Weapons"

        val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)
        }

        mSharedPreferences = this.getSharedPreferences("com.austinhodak.pubgcenter", Context.MODE_PRIVATE)

        try {
            val appKey = "d3075b30dfee1acd300873e535ca0e22b8baf89da92f1890"
            Appodeal.disableLocationPermissionCheck()
            Appodeal.initialize(this, appKey, Appodeal.NATIVE or Appodeal.INTERSTITIAL or Appodeal.BANNER)
            Appodeal.setBannerViewId(R.id.appodealBannerView)
            Appodeal.set728x90Banners(false)
        } catch (e: Exception) {
        }

        MobileAds.initialize(this, "ca-app-pub-1946691221734928~1341566099")

        setSupportActionBar(main_toolbar)

        if (!mSharedPreferences.getBoolean("removeAds", false)) {
            //loadAds()
            Appodeal.show(this, Appodeal.BANNER_VIEW)
        }

        setupDrawer()

        mAuth = FirebaseAuth.getInstance()

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

        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance()
        }

        if (mAuth != null && mAuth?.currentUser == null) {
            mAuth?.signInAnonymously()
                    ?.addOnCompleteListener(this) { }
        }
    }

    public override fun onResume() {
        super.onResume()
        Appodeal.onResume(this, Appodeal.BANNER)
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

    private fun setupDrawer() {
        val homeFragment = HomeWeaponsFragment()
        supportFragmentManager.beginTransaction().replace(R.id.main_frame, homeFragment)
                .commit()

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            supportActionBar?.elevation = 0.0f
            appbar.elevation = 0.0f
        }

        val headerResult = AccountHeaderBuilder()
                .withActivity(this)
                //.withHeaderBackground(R.drawable.header1)
                .addProfiles(
                        ProfileDrawerItem().withName("PUBG BattleGuide").withEmail("Level 1 (Free)").withIcon(R.drawable.icon1),
                        ProfileSettingDrawerItem().withName("Upgrade").withIcon(R.drawable.icons8_buy_upgrade_96)
                )
                .withOnAccountHeaderListener { view, profile, currentProfile -> false }
                .build()

        Glide.with(this).load(R.drawable.header2).into(headerResult.headerBackgroundView)

        val badge = BadgeStyle().withTextColorRes(R.color.md_black_1000).withColorRes(R.color.md_white_1000)

        val home = PrimaryDrawerItem().withIdentifier(100).withName("Game Updates").withIcon(R.drawable.update1)
        val map1 = SecondaryDrawerItem().withIdentifier(200).withName("Erangel").withBadge("ORIGINAL")
        val map2 = SecondaryDrawerItem().withIdentifier(201).withName("Miramar").withBadge("NEW")


        val weapons = PrimaryDrawerItem().withIdentifier(1).withName("Weapons").withIcon(R.drawable.icons8_rifle).withIconTintingEnabled(false)
        val attachment = PrimaryDrawerItem().withIdentifier(2).withName("Attachments").withIcon(R.drawable.icons8_magazine).withIconTintingEnabled(false)
        val ammo = PrimaryDrawerItem().withIdentifier(3).withName("Ammo").withIcon(R.drawable.icons8_ammo).withIconTintingEnabled(false)
        val consumables = PrimaryDrawerItem().withIdentifier(4).withName("Consumables").withIcon(R.drawable.icons8_syringe).withIconTintingEnabled(false)
        val equipment = PrimaryDrawerItem().withIdentifier(5).withName("Equipment").withIcon(R.drawable.icons8_helmet).withIconTintingEnabled(false)

        val settings = SecondaryDrawerItem().withIdentifier(901).withName("About").withSelectable(false).withIcon(R.drawable.icons8_info)

        val removeAds = PrimaryDrawerItem().withIdentifier(9001).withName("Remove Ads").withIcon(R.drawable.icons8_remove_ads_96).withSelectable(false)

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
                        PrimaryDrawerItem().withName("Damage Calculator").withIcon(R.drawable.shield).withIdentifier(998).withSelectable(false),
                        home,
                        ExpandableDrawerItem().withName("Maps").withSelectable(false).withIcon(R.drawable.map_96).withSubItems(map1, map2),
                        DividerDrawerItem(),
                        settings
                )
                .withOnDrawerItemClickListener { view, position, drawerItem ->
                    if (drawerItem.identifier.toString() == "100") {
                        val homeFragment3 = HomeFragmentBottom()
                        supportFragmentManager.beginTransaction().replace(R.id.main_frame, homeFragment3)
                                .commit()

                        toolbar_title.text = "Updates"

                        updateToolbarElevation(15f)
                    }

                    if (drawerItem.identifier.toString() == "1") {
                        val homeFragment3 = HomeWeaponsFragment()
                        supportFragmentManager.beginTransaction().replace(R.id.main_frame, homeFragment3)
                                .commit()

                        toolbar_title.text = "Weapons"

                        updateToolbarElevation(0f)
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
                    }

                    if (drawerItem.identifier.toString() == "4") {
                        val homeFragment2 = HomeConsumablesList()
                        supportFragmentManager.beginTransaction().replace(R.id.main_frame, homeFragment2)
                                .commit()

                        toolbar_title.text = "Consumables"

                        updateToolbarElevation(15f)
                    }

                    if (drawerItem.identifier.toString() == "5") {
                        val homeFragment2 = HomeEquipmentList()
                        supportFragmentManager.beginTransaction().replace(R.id.main_frame, homeFragment2)
                                .commit()

                        toolbar_title.text = "Equipment"

                        updateToolbarElevation(15f)
                    }

                    if (drawerItem.identifier.toString() == "200") {
                        val mapFrag = MapViewFragment()
                        val bundle = Bundle()
                        bundle.putInt("map", 0)
                        mapFrag.arguments = bundle
                        supportFragmentManager.beginTransaction().replace(R.id.main_frame, mapFrag)
                                .commit()

                        toolbar_title.text = "Erangel"
                        updateToolbarElevation(15f)

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

                    }

                    if (drawerItem.identifier.toString() == "901") {
//                        LibsBuilder()
//                                //provide a style (optional) (LIGHT, DARK, LIGHT_DARK_TOOLBAR)
//                                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
//                                //start the activity
//                                .withActivityTitle("PUBG BattleGuide")
//                                .withAboutDescription("<b>Changelog v0.3.0 • 02/13/2018</b><br><br>" +
//                                        "• New Damage Calculator!<br>")
//                                .start(this)

                        val intent = Intent(this, ProfileActivity::class.java)
                        startActivity(intent)

                    }

                    if (drawerItem.identifier.toString() == "999") {
                        val intent = Intent(this, LoadoutCreateMain::class.java)
                        startActivity(intent)
                    }

                    if (drawerItem.identifier.toString() == "998") {
                        val intent = Intent(this, DamageCalcActivity::class.java)
                        startActivity(intent)
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
                    }
                    false
                }
                .build()

        result.setSelection(1)


        if (!mSharedPreferences.getBoolean("removeAds", false)) {
            result.addStickyFooterItem(removeAds)
        }
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
                Snacky.builder().info().setText("Thanks! Please restart the app to remove ads!").show()
            }
        }
    }

    private fun isGooglePlayServicesAvailable(activity: Activity): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(activity)
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                //googleApiAvailability.getErrorDialog(activity, status, 2404).show()
            }
            return false
        }
        return true
    }
}
