package com.austinh.battlebuddy

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.drawerlayout.widget.DrawerLayout
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.badgeable.secondaryItem
import co.zsmb.materialdrawerkt.draweritems.divider
import co.zsmb.materialdrawerkt.draweritems.expandable.expandableItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.ads.consent.*
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.instabug.bug.BugReporting
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.austinh.battlebuddy.ammo.HomeAmmoList
import com.austinh.battlebuddy.attachments.HomeAttachmentsFragment
import com.austinh.battlebuddy.damage_calculator.DamageCalcActivity
import com.austinh.battlebuddy.info.ControlsFragment
import com.austinh.battlebuddy.info.TimerFragment
import com.austinh.battlebuddy.map.MapDropRouletteActivity
import com.austinh.battlebuddy.models.PrefPlayer
import com.austinh.battlebuddy.models.SeasonStatsAll
import com.austinh.battlebuddy.premium.UpgradeActivity
import com.austinh.battlebuddy.profile.ProfileMain
import com.austinh.battlebuddy.rss.HomeUpdatesFragment
import com.austinh.battlebuddy.snacky.Snacky
import com.austinh.battlebuddy.stats.PlayerListDialog
import com.austinh.battlebuddy.stats.main.StatsHome
import com.austinh.battlebuddy.utils.*
import com.austinh.battlebuddy.weapons.HomeWeaponsFragment
import kotlinx.android.synthetic.main.activity_new_home.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.browse
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.topPadding
import java.net.MalformedURLException
import java.net.URL
import java.util.*

class MainActivityKT : AppCompatActivity() {

    private var mDrawer: Drawer? = null
    private var mRightDrawer: Drawer? = null
    private val signInDrawerItem = SecondaryDrawerItem()
            .withIcon(R.drawable.icons8_password)
            .withSelectable(false)
            .withName(R.string.drawer_title_login)
            .withIdentifier(1)
            .withOnDrawerItemClickListener { view, position, drawerItem ->
                launchSignIn()
                true
            }
    private val logoutDrawerItem = PrimaryDrawerItem()
            .withIcon(R.drawable.icons8_person_male)
            .withSelectable(false)
            .withName("Profile")
            .withIdentifier(2)
            .withOnDrawerItemClickListener { view, position, drawerItem ->
                startActivity<ProfileMain>()
                true
            }

    private var mSharedPreferences: SharedPreferences? = null
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.HomeTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_home)
        setSupportActionBar(newHomeToolbar)

        mSharedPreferences = this.getSharedPreferences("com.austinh.battlebuddy", Context.MODE_PRIVATE)

        if (intent != null) {
            if (intent.hasExtra("url")) {
                browse(intent.getStringExtra("url"), true)
            }
        }

        checkConsent()

        setupDrawer()

        if (Premium.isPremiumUser())
            setupRightDrawer()

        if (!Premium.isAdFreeUser()) {
            val adView = com.google.android.gms.ads.AdView(this)
            adView.adSize = com.google.android.gms.ads.AdSize.BANNER
            adView.adUnitId = "ca-app-pub-2318893623894354/2646896834"
            adView.loadAd(Ads.getAdBuilder())
            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    newHomeTop?.removeAllViews()
                    newHomeTop?.addView(adView)
                }

                override fun onAdFailedToLoad(errorCode: Int) {
                    // Code to be executed when an ad request fails.
                }

                override fun onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                }

                override fun onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                }

                override fun onAdClosed() {
                    // Code to be executed when when the user is about to return
                    // to the app after tapping on an ad.
                }
            }

            val launchCount = mSharedPreferences?.getInt("launchCount", 0) ?: 0
            if (launchCount >= 2) {
                mInterstitialAd = InterstitialAd(this)
                mInterstitialAd?.adUnitId = "ca-app-pub-2318893623894354/5085357913"
                mInterstitialAd?.loadAd(Ads.getAdBuilder())
            }

            mSharedPreferences?.edit()?.putInt("launchCount", launchCount + 1)?.apply()
        }
    }

    private fun checkLogin() {
        val headerText = mDrawer?.header?.findViewById<TextView>(R.id.header_name)
        if (FirebaseAuth.getInstance().currentUser == null) {
            FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener {
                checkLogin()
            }
            return
        }

        Auth.setupAccount(FirebaseAuth.getInstance().currentUser)

        if (Auth.isUserAnon()) {
            headerText?.text = "Battle Buddy"

            mDrawer?.removeAllStickyFooterItems()
            mDrawer?.addStickyFooterItem(signInDrawerItem)

            mDrawer?.removeItem(2)
        } else {
            //User is NOT ANON
            headerText?.text = Auth.getUser().displayName ?: "Battle Buddy"
            mDrawer?.removeAllStickyFooterItems()

            if (!mDrawer?.drawerItems!!.contains(logoutDrawerItem))
                mDrawer?.addItemAtPosition(logoutDrawerItem, 1)
        }

        if (mRightDrawer != null && Premium.isPremiumUser()) {
            loadPlayers()
        }
    }

    private fun setupDrawer() {
        mDrawer = drawer {
            headerDivider = false
            headerViewRes = R.layout.main_drawer_header
            toolbar = newHomeToolbar
            primaryItem(R.string.drawer_title_stats_enw) {
                icon = R.drawable.chart_color
                selectable = false
                onClick { _ ->
                    startActivity<PlayerListDialog>()
                    true
                }
            }
            primaryItem("Map Drop Roulette") {
                icon = R.drawable.ic_roulette
                selectable = false
                onClick { _ ->
                    startActivity<MapDropRouletteActivity>()
                    true
                }
            }
            divider()
            primaryItem(R.string.drawer_title_weapons) {
                icon = R.drawable.icons8_rifle
                identifier = 100
                onClick { _ ->
                    newHomeToolbar?.title = "Weapons"
                    supportFragmentManager.beginTransaction().replace(R.id.mainFrame, HomeWeaponsFragment()).commit()
                    updateToolbarElevation(0f)
                    false
                }
            }
            primaryItem(R.string.drawer_title_attachments) {
                icon = R.drawable.icons8_magazine
                onClick { _ ->
                    newHomeToolbar?.title = "Attachments"
                    supportFragmentManager.beginTransaction().replace(R.id.mainFrame, HomeAttachmentsFragment()).commit()
                    updateToolbarElevation(0f)
                    false
                }
            }
            primaryItem(R.string.drawer_title_ammo) {
                icon = R.drawable.icons8_ammo
                onClick { _ ->
                    newHomeToolbar?.title = "Ammo"
                    supportFragmentManager.beginTransaction().replace(R.id.mainFrame, HomeAmmoList()).commit()
                    updateToolbarElevation(15f)
                    false
                }
            }
            primaryItem(R.string.drawer_title_equipment) {
                icon = R.drawable.icons8_helmet
                onClick { _ ->
                    newHomeToolbar?.title = "Equipment"
                    supportFragmentManager.beginTransaction().replace(R.id.mainFrame, HomeEquipmentList()).commit()
                    updateToolbarElevation(15f)
                    false
                }
            }
            primaryItem(R.string.drawer_title_consumables) {
                icon = R.drawable.icons8_syringe
                onClick { _ ->
                    newHomeToolbar?.title = "Consumables"
                    supportFragmentManager.beginTransaction().replace(R.id.mainFrame, HomeConsumablesList()).commit()
                    updateToolbarElevation(15f)
                    false
                }
            }
            primaryItem(R.string.drawer_title_vehicles) {
                icon = R.drawable.icons8_car
                onClick { _ ->
                    newHomeToolbar?.title = "Vehicles"
                    supportFragmentManager.beginTransaction().replace(R.id.mainFrame, VehiclesFragment()).commit()
                    updateToolbarElevation(15f)
                    false
                }
            }
            expandableItem(R.string.drawer_title_more) {
                icon = R.drawable.icons8_view_more_96
                selectable = false
                divider()
                primaryItem(R.string.drawer_title_controls) {
                    icon = R.drawable.icons8_game_controller_96
                    onClick { _ ->
                        newHomeToolbar?.title = "Controls"
                        supportFragmentManager.beginTransaction().replace(R.id.mainFrame, ControlsFragment()).commit()
                        updateToolbarElevation(15f)
                        false
                    }
                }
                primaryItem(R.string.drawer_title_damagecalc) {
                    icon = R.drawable.shield
                    selectable = false
                    onClick { _ ->
                        startActivity<DamageCalcActivity>()
                        false
                    }
                }
                primaryItem(R.string.drawer_title_maps) {
                    icon = R.drawable.map_96
                    selectable = false
                    onClick { _ ->
                        val intentBuilder = CustomTabsIntent.Builder()
                        intentBuilder.setToolbarColor(ContextCompat.getColor(this@MainActivityKT, R.color.colorPrimary))
                        intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(this@MainActivityKT, R.color.colorPrimaryDark))
                        val customTabsIntent = intentBuilder.build()

                        try {
                            customTabsIntent.launchUrl(this@MainActivityKT, Uri.parse("https://pubgmap.io/"))
                        } catch (e: Exception) {
                            Snacky.builder().setActivity(this@MainActivityKT).error().setText("Error loading map.").show()
                        }
                        true
                    }
                }
                primaryItem(R.string.drawer_title_timer) {
                    icon = R.drawable.stopwatch
                    onClick { _ ->
                        newHomeToolbar?.title = "Controls"
                        supportFragmentManager.beginTransaction().replace(R.id.mainFrame, TimerFragment()).commit()
                        updateToolbarElevation(0f)
                        false
                    }

                }
                primaryItem(R.string.drawer_title_update) {
                    icon = R.drawable.rss
                    onClick { _ ->
                        //MediationTestSuite.launch(this@MainActivityKT, "ca-app-pub-1946691221734928~8934220899")

                        newHomeToolbar?.title = "RSS Feed"
                        supportFragmentManager.beginTransaction().replace(R.id.mainFrame, HomeUpdatesFragment()).commit()
                        updateToolbarElevation(15f)
                        false
                    }
                }
            }
            divider()
            secondaryItem(R.string.drawer_title_about) {
                icon = R.drawable.icons8_info
                selectable = false
                onClick { _ ->
                    LibsBuilder()
                            .withAboutIconShown(true)
                            .withAboutVersionShown(true)
                            .withActivityStyle(Libs.ActivityStyle.DARK)
                            .withActivityTitle("Battlegrounds Battle Buddy")
                            .withAboutDescription("")
                            .withAboutSpecial1("Twitter")
                            .withAboutSpecial1Description("<a href=\"https://twitter.com/pubgbuddy\">Follow us on Twitter!</a>")
                            .withAboutSpecial2("Discord")
                            .withAboutSpecial2Description("<a href=\"https://discord.gg/5bbJNvx\">Join Our Discord!</a>")
                            .start(this@MainActivityKT)
                    true
                }
            }
            secondaryItem(R.string.drawer_title_alerts) {
                icon = R.drawable.notification
                selectable = false
                onClick { _, _, _ ->
                    startActivity<AlertManager>()
                    true
                }
            }
            secondaryItem(R.string.drawer_title_suggestion) {
                icon = R.drawable.ic_love
                selectable = false
                onClick { _ ->
                    BugReporting.invoke()
                    true
                }
            }
            secondaryItem(R.string.drawer_title_share) {
                icon = R.drawable.icons8_share
                selectable = false
                onClick { _ ->
                    val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
                    sharingIntent.type = "text/plain"
                    val shareBody = "https://play.google.com/store/apps/details?id=com.austinh.battlebuddy"
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out Battlegrounds Battle Buddy on the Google Play Store!")
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
                    startActivity(sharingIntent)
                    true
                }
            }
            if (Premium.getUserLevel() != Premium.Level.LEVEL_3) {
                secondaryItem(R.string.drawer_title_upgrade) {
                    selectable = false
                    icon = R.drawable.upgrade
                    onClick { _ ->
                        startActivity<UpgradeActivity>()
                        true
                    }
                }
            }
        }
        mDrawer?.recyclerView?.isVerticalScrollBarEnabled = false

        mDrawer?.setSelection(100)
        updateToolbarElevation(0f)
    }

    var mRightAdapter: SlimAdapter? = null
    var players: MutableList<PrefPlayer> = ArrayList()
    var userAccountID = ""

    private fun setupRightDrawer() {
        mRightDrawer = drawer {
            primaryDrawer = mDrawer
            gravity = Gravity.END
        }

        mRightDrawer?.drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END)

        mRightDrawer?.recyclerView?.clipToPadding = false
        mRightDrawer?.recyclerView?.setPadding(0, mRightDrawer?.recyclerView?.topPadding!!, 0, 24)

        mRightAdapter = SlimAdapter.create().attachTo(mRightDrawer?.recyclerView).updateData(players).register<PrefPlayer>(R.layout.home_player_list_item) { player, injector ->
            val cardView = injector.findViewById<CardView>(R.id.playerListCard)
            injector.invisible(R.id.game_version_icon)
            cardView.setCardBackgroundColor(resources.getColor(Ranks.getRankColor(0.0)))

            if (userAccountID == player.playerID) {
                injector.text(R.id.player_select_name, "${player.playerName}  \uf2bd ")
            } else {
                injector.text(R.id.player_select_name, player.playerName)
            }

            injector.text(R.id.playerListSubtitle, Regions.getNewRegionName(player.defaultShardID))

            injector.clicked(R.id.constraintLayout) {

            }

            val currentSeason = com.austinh.battlebuddy.utils.Seasons.getCurrentSeasonForShard(player.defaultShardID)

            injector.gone(R.id.player_pg)

            if (player.selectedShardID.equals("xbox", true)) {
                player.selectedShardID = "xbox-na"
            }

            val searchShardID: String

            if (player.defaultShardID == "xbox") {
                if (player.isSeasonNewFormat("xbox")) {
                    searchShardID = "xbox"
                } else {
                    searchShardID = player.oldXboxShard ?: "xbox-na"
                }
            } else {
                searchShardID = player.defaultShardID
            }

            FirebaseDatabase.getInstance().getReference("user_stats/${player.playerID}/season_data/${searchShardID.toLowerCase()}/$currentSeason/stats").addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    val rankIcon = injector.findViewById<ImageView>(R.id.game_version_icon)
                    val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()

                    if (!p0.exists()) {
                        Glide.with(this@MainActivityKT)
                                .load(Ranks.getRankIcon(0.0))
                                .transition(DrawableTransitionOptions.withCrossFade(factory))
                                .into(rankIcon)

                        injector.invisible(R.id.game_version_icon)
                        cardView.setCardBackgroundColor(resources.getColor(Ranks.getRankColor(0.0)))

                        injector.visible(R.id.player_pg)

                        Log.d("PLAYER", "LOAD ${player.playerID} - ${player.selectedShardID} - ${player.selectedSeason}")
                        return
                    }

                    injector.gone(R.id.player_pg)

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

                    injector.visible(R.id.game_version_icon)

                    Glide.with(applicationContext)
                            .load(Ranks.getRankIcon(pointsList[0]))
                            .transition(DrawableTransitionOptions.withCrossFade(factory))
                            .into(rankIcon)

                    cardView.setCardBackgroundColor(resources.getColor(Ranks.getRankColor(pointsList[0])))

                    cardView.setOnClickListener {
                        startActivity<StatsHome>("selectedPlayer" to player)
                    }
                }
            })
        }

        mRightDrawer?.recyclerView?.adapter = mRightAdapter
    }

    override fun onResume() {
        super.onResume()
        updateHeader()
        checkLogin()
    }

    private fun updateToolbarElevation(int: Float) {
        ViewCompat.setElevation(newHomeAppBar, int)
    }

    private fun updateHeader() {
        val levelText = mDrawer?.header?.findViewById<TextView>(R.id.header_upgrade)
        val headerText = mDrawer?.header?.findViewById<TextView>(R.id.header_name)

        levelText?.text = Premium.getLevelText(Premium.getUserLevel())
        headerText?.text = FirebaseAuth.getInstance().currentUser?.displayName ?: "Battle Buddy"

        FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().currentUser?.uid}/pubgAccountID").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                val icon = mDrawer?.header?.findViewById<ImageView>(R.id.header_rank_icon)
                //val pubgName = mDrawer?.header?.findViewById<TextView>(R.id.header_ingame_name)
                if (!p0.exists() || !p0.hasChild("accountID") || !p0.hasChild("shardID")) {
                    icon?.visibility = View.GONE
                    return
                }

                val accountID = p0.child("accountID").value.toString()
                val shardID = p0.child("shardID").value.toString()

                if (shardID == "xbox") {
                    if (isSeasonNewFormat(shardID)) {
                        FirebaseDatabase.getInstance().getReference("user_stats/$accountID/season_data/xbox/${Seasons.xboxCurrentSeason}/stats").addListenerForSingleValueEvent(object : ValueEventListener {
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

                                icon?.visibility = View.VISIBLE

                                Glide.with(applicationContext)
                                        .load(Ranks.getRankIcon(pointsList[0]))
                                        .transition(DrawableTransitionOptions.withCrossFade(factory))
                                        .into(icon!!)


                            }
                        })
                    } else {
                        FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().currentUser?.uid}/pubg_players/$accountID").addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                if (!p0.exists()) return

                                val oldXboxShard = p0.child("oldXboxShard").value.toString()

                                Log.d("HEADER", "${p0.value.toString()} -- /user_stats/$accountID/season_data/$oldXboxShard/${Seasons.xboxCurrentSeason}/stats")

                                FirebaseDatabase.getInstance().getReference("user_stats/$accountID/season_data/$oldXboxShard/${Seasons.xboxCurrentSeason}/stats").addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {

                                    }

                                    override fun onDataChange(p0: DataSnapshot) {
                                        Log.d("HEADER", p0.ref.toString())
                                        if (!p0.exists()) return

                                        Log.d("HEADER", p0.ref.toString())

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

                                        icon?.visibility = View.VISIBLE

                                        Glide.with(applicationContext)
                                                .load(Ranks.getRankIcon(pointsList[0]))
                                                .transition(DrawableTransitionOptions.withCrossFade(factory))
                                                .into(icon!!)


                                    }
                                })
                            }
                        })
                    }
                } else {
                    FirebaseDatabase.getInstance().getReference("user_stats/$accountID/season_data/$shardID/${Seasons.pcCurrentSeason}/stats").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(data: DataSnapshot) {
                            if (!data.exists()) {

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

                            icon?.visibility = View.VISIBLE

                            Glide.with(applicationContext)
                                    .load(Ranks.getRankIcon(pointsList[0]))
                                    .transition(DrawableTransitionOptions.withCrossFade(factory))
                                    .into(icon!!)
                        }
                    })
                }
            }
        })
    }

    private var listener: ValueEventListener? = null
    private var listenerRef: DatabaseReference? = null

    private fun loadPlayers() {
        if (listener != null && listenerRef != null) {
            listenerRef!!.removeEventListener(listener!!)
            listener = null
            listenerRef = null
        }
        try {
            players.clear()
        } catch (e: Exception) {
        }
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener {
                loadPlayers()
            }
            return
        }

        val ref = FirebaseDatabase.getInstance().reference.child("users").child(currentUser.uid)

        listenerRef = ref.ref
        listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (!p0.exists()) {
                    return
                }

                if (p0.hasChild("pubgAccountID") && !p0.hasChild("pubgAccountID/accountID") && !p0.hasChild("pubgAccountID/shardID")) {
                    userAccountID = p0.child("pubgAccountID").value.toString()
                    p0.ref.child("pubgAccountID/accountID").setValue(userAccountID)
                }

                if (p0.hasChild("pubgAccountID/accountID")) {
                    userAccountID = p0.child("pubgAccountID/accountID").value.toString()
                }

                players.clear()

                for (child in p0.child("pubg_players").children) {
                    var shardID = child.child("shardID").value.toString().toLowerCase()
                    var oldXboxShard: String? = null
                    if (shardID.contains("kakao") && shardID != "kakao") {
                        child.ref.child("shardID").setValue("kakao")
                        shardID = "kakao"
                    } else if (shardID.contains("pc")) {
                        child.ref.child("shardID").setValue("steam")
                        shardID = "steam"
                    } else if (shardID.contains("xbox") && shardID != "xbox") {
                        child.ref.child("shardID").setValue("xbox")
                        child.ref.child("oldXboxShard").setValue(shardID)
                        oldXboxShard = shardID
                        shardID = "xbox"
                    }

                    if (!p0.hasChild("pubgAccountID/shardID") && userAccountID == child.key) {
                        p0.ref.child("pubgAccountID/shardID").setValue(shardID)
                    }

                    val player = PrefPlayer(
                            playerID = child.key.toString(),
                            playerName = child.child("playerName").value.toString(),
                            defaultShardID = shardID.toLowerCase(),
                            selectedGamemode = "solo"
                    )

                    if (oldXboxShard != null) {
                        player.oldXboxShard = oldXboxShard
                    } else if (child.hasChild("oldXboxShard")) {
                        player.oldXboxShard = child.child("oldXboxShard").value.toString()
                    }

                    Log.d("PLAYER", player.playerID)

                    players.add(player)
                }

                players = players.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.playerName }).toMutableList()

                mRightAdapter?.updateData(players)

                mRightDrawer?.drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && data != null) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == RESULT_OK) {
                mDrawer?.removeAllStickyFooterItems()
                Snacky.builder().setActivity(this).success().setText("Logged In!").show()
                checkLogin()
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
                                    checkLogin()
                                    mDrawer?.removeAllStickyFooterItems()
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
    }

    private fun launchSignIn() {
        val requestCode = 123

        val providers: List<AuthUI.IdpConfig> = Arrays.asList(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.PhoneBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build(),
                AuthUI.IdpConfig.FacebookBuilder().build(),
                AuthUI.IdpConfig.TwitterBuilder().build())

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
        //onsentInformation.getInstance(this).debugGeography = DebugGeography.DEBUG_GEOGRAPHY_EEA

        val consentInformation = ConsentInformation.getInstance(this)
        val publisherIds = arrayOf("pub-1946691221734928")
        consentInformation.requestConsentInfoUpdate(publisherIds, object : ConsentInfoUpdateListener {
            override fun onConsentInfoUpdated(consentStatus: ConsentStatus) {
                // User's consent status successfully updated.
                Log.d("CONSENT", "User's consent status: $consentStatus")
                if (ConsentInformation.getInstance(this@MainActivityKT).isRequestLocationInEeaOrUnknown && consentStatus == ConsentStatus.UNKNOWN) {
                    var privacyUrl: URL? = null
                    try {
                        privacyUrl = URL("https://www.freeprivacypolicy.com/privacy/view/f7a9373ab150a1a29ce5cc66a224a87e")
                    } catch (e: MalformedURLException) {
                        e.printStackTrace()
                    }

                    consentForm = ConsentForm.Builder(this@MainActivityKT, privacyUrl)
                            .withListener(object : ConsentFormListener() {
                                override fun onConsentFormLoaded() {
                                    super.onConsentFormLoaded()
                                    if (consentForm != null) {
                                        try {
                                            consentForm?.show()
                                        } catch (e: Exception) {
                                        }
                                    }
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


                                    Ads.setConsentGiven(consentStatus == ConsentStatus.PERSONALIZED)
                                    ConsentInformation.getInstance(this@MainActivityKT).consentStatus = consentStatus
                                }
                            })
                            .withPersonalizedAdsOption()
                            .withNonPersonalizedAdsOption()
                            .withAdFreeOption()
                            .build()

                    consentForm?.load()
                }

                Ads.setConsentGiven(consentStatus == ConsentStatus.PERSONALIZED || consentStatus == ConsentStatus.UNKNOWN)
            }

            override fun onFailedToUpdateConsentInfo(errorDescription: String) {
                // User's consent status failed to update.
            }
        })
    }

    override fun onBackPressed() {
        if (mDrawer != null && mDrawer?.isDrawerOpen!!) {
            mDrawer?.closeDrawer()
            return
        } else if (mRightDrawer != null && mRightDrawer?.isDrawerOpen!!) {
            mRightDrawer?.closeDrawer()
            return
        } else {
            super.onBackPressed()
        }
        if (mSharedPreferences != null) {
            if (Premium.isAdFreeUser()) {
                super.onBackPressed()
                return
            } else {
                super.onBackPressed()
            }
        } else if (mSharedPreferences == null) {
            super.onBackPressed()
            return
        } else {
            super.onBackPressed()
        }
        val launchCount = mSharedPreferences?.getInt("launchCount", 0) ?: 0
        Log.d("LAUNCH", "$launchCount")
        if (launchCount >= 2) {
            try {
                if (mInterstitialAd?.isLoaded == true) {
                    mInterstitialAd?.show()
                    mSharedPreferences?.edit()?.putInt("launchCount", 0)?.apply()
                } else {
                    super.onBackPressed()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    fun isSeasonNewFormat(shardID: String): Boolean {
        val region = Regions.getNewRegion(shardID)
        if (region == Regions.Region.KAKAO || region == Regions.Region.STEAM) {
            return when (Seasons.pcCurrentSeason) {
                "pc-2018-01" -> true
                "2018-01",
                "2018-02",
                "2018-03",
                "2018-04",
                "2018-05",
                "2018-06",
                "2018-07",
                "2018-08",
                "2018-09" -> false
                else -> true
            }
        } else if (region == Regions.Region.XBOX) {
            return when (Seasons.xboxCurrentSeason) {
                "2018-08",
                "2018-01",
                "2018-02",
                "2018-03",
                "2018-04",
                "2018-05",
                "2018-06",
                "2018-07" -> false
                else -> true
            }
        } else {
            return true
        }
    }
}