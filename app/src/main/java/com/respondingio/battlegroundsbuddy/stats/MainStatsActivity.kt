package com.respondingio.battlegroundsbuddy.stats

import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.customListAdapter
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.android.vending.billing.IInAppBillingService
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.Telemetry
import com.respondingio.battlegroundsbuddy.models.PrefPlayer
import com.respondingio.battlegroundsbuddy.models.Seasons
import com.respondingio.battlegroundsbuddy.snacky.Snacky
import kotlinx.android.synthetic.main.activity_stats_main_new.bottom_navigation
import kotlinx.android.synthetic.main.activity_stats_main_new.mainStatsRefreshLayout
import kotlinx.android.synthetic.main.activity_stats_main_new.no_player
import kotlinx.android.synthetic.main.activity_stats_main_new.stats_gamemode_picker
import kotlinx.android.synthetic.main.activity_stats_main_new.stats_gamemode_text
import kotlinx.android.synthetic.main.activity_stats_main_new.stats_player_picker
import kotlinx.android.synthetic.main.activity_stats_main_new.stats_player_text
import kotlinx.android.synthetic.main.activity_stats_main_new.stats_region_picker
import kotlinx.android.synthetic.main.activity_stats_main_new.stats_region_text
import kotlinx.android.synthetic.main.activity_stats_main_new.stats_season_picker
import kotlinx.android.synthetic.main.activity_stats_main_new.stats_season_text
import kotlinx.android.synthetic.main.activity_stats_main_new.weapon_detail_toolbar
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.support.v4.onRefresh

class MainStatsActivity : AppCompatActivity(), RewardedVideoAdListener {


    var regionList = arrayOf("XBOX-AS", "XBOX-EU", "XBOX-NA", "XBOX-OC", "PC-KRJP", "PC-JP", "PC-NA", "PC-EU", "PC-RU", "PC-OC", "PC-KAKAO", "PC-SEA", "PC-SA", "PC-AS")
    var regions = arrayOf("Xbox Asia", "Xbox Europe", "Xbox North America", "Xbox Oceania", "PC Korea", "PC Japan", "PC North America", "PC Europe", "PC Russia", "PC Oceania", "PC Kakao", "PC South East Asia", "PC South and Central America", "PC Asia")
    var modesList = arrayOf("solo", "solo-fpp", "duo", "duo-fpp", "squad", "squad-fpp")

    lateinit var mSharedPreferences: SharedPreferences
    public var playersMap = HashMap<String, String>()
    var currentPlayer: PrefPlayer? = null
    private var updateTimeout: Long = 15

    private var isPremium: Boolean = false
    var lastUpdated: Long? = null

    private var mRewardedVideoAd: RewardedVideoAd? = null

    private var iap: IInAppBillingService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats_main_new)

        setSupportActionBar(weapon_detail_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val serviceIntent = Intent("com.android.vending.billing.InAppBillingService.BIND")
        serviceIntent.`package` = "com.android.vending"
        applicationContext?.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        mSharedPreferences = this.getSharedPreferences("com.respondingio.battlegroundsbuddy", Context.MODE_PRIVATE)
        isPremium = mSharedPreferences.getBoolean("premiumV1", false)

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(applicationContext)
        mRewardedVideoAd?.rewardedVideoAdListener = this

        if (mSharedPreferences.getBoolean("premiumV1", false)) {
            updateTimeout = 2
        } else {
            mRewardedVideoAd?.loadAd("ca-app-pub-1946691221734928/1941699809",
                    AdRequest.Builder().build())
        }

        if (mSharedPreferences.getBoolean("isStatsFirstLaunch", true)) {
            MaterialDialog(this)
                    .title(text = "Player Stats (Beta)")
                    .message(R.string.statsOnBoarding)
                    .positiveButton(text = "OKAY") {
                        dialog ->
                        dialog.dismiss()
                        mSharedPreferences.edit().putBoolean("isStatsFirstLaunch", false).apply()
                    }.show()
        }

        setupTopNav()
        setupBottomNav()
        loadPlayers()

        if (mSharedPreferences.contains("selected-player-id")) {
            no_player.visibility = View.GONE
            setPlayerSelected(mSharedPreferences.getString("selected-player-id", "")!!)
        } else {
            //TODO SETUP NO PLAYER SELECTED STUFF.
            no_player.visibility = View.VISIBLE
            stats_player_text.text = "Select Player"
            stats_region_text.text = "Select Region"
            stats_gamemode_text.text = "Select Gamemode"
            stats_season_text.text = "Select Season"
        }

        mainStatsRefreshLayout?.setColorSchemeResources(R.color.md_orange_500, R.color.md_pink_500)

        mainStatsRefreshLayout?.onRefresh {
            if (lastUpdated == null) {
                mainStatsRefreshLayout?.isRefreshing = false
                Log.e("MainStats", "Last updated long is null, either player isn't selected or stats haven't loaded yet...")
                return@onRefresh
            }

            if (currentPlayer != null && currentPlayer?.playerID != null && currentPlayer?.selectedShardID != null && currentPlayer?.selectedGamemode != null && currentPlayer?.selectedSeason != null) {
                if (getTimeSinceLastUpdated() / 60 > updateTimeout) {
                    mainStatsRefreshLayout.isRefreshing = true
                    reloadStats(currentPlayer!!)
                } else if (!isPremium) {
                    mainStatsRefreshLayout.isRefreshing = false
                    Snacky.builder().setActivity(this).setBackgroundColor(Color.parseColor("#3F51B5")).setText("You can refresh once every $updateTimeout minutes.")
                            .setActionText("UPGRADE OR WATCH AN AD").setDuration(5000).setActionTextColor(Color.WHITE).setActionClickListener {
                                MaterialDialog(this)
                                        .title(text = "Upgrade or Watch An Ad")
                                        .message(R.string.upgrade)
                                        .positiveButton(text = "GO PREMIUM ($2.99)") {
                                            val buyIntentBundle = iap?.getBuyIntent(3, packageName,
                                                    "plus_v1", "inapp", "")

                                            val pendingIntent = buyIntentBundle?.getParcelable<PendingIntent>("BUY_INTENT")

                                            if (pendingIntent != null) {

                                                val REQUEST_CODE = 1002

                                                startIntentSenderForResult(pendingIntent.intentSender,
                                                        REQUEST_CODE, Intent(), 0, 0,
                                                        0)
                                            }
                                        }
                                        .neutralButton(text = "NEVERMIND") { dialog -> dialog.dismiss() }
                                        .negativeButton(text = "WATCH AN AD") { dialog ->
                                            if (mRewardedVideoAd?.isLoaded == true) {
                                                mRewardedVideoAd?.show()
                                            } else {
                                                Snacky.builder().setActivity(this).info().setText("No ad loaded, please try again.").show()
                                            }
                                        }.show()
                            }.build().show()
                } else if (isPremium) {
                    mRewardedVideoAd?.loadAd("ca-app-pub-3940256099942544/5224354917",
                            AdRequest.Builder().build())

                    mainStatsRefreshLayout.isRefreshing = false
                    Snacky.builder().setActivity(this).setBackgroundColor(Color.parseColor("#3F51B5")).setText("You can refresh once every $updateTimeout minutes with premium. This is a PUBG API limit.").setActionClickListener {
                        if (mRewardedVideoAd?.isLoaded == true) {
                            mRewardedVideoAd?.show()
                        } else {
                            Snacky.builder().setActivity(this).info().setText("No ad loaded, please try again.").show()
                        }
                    }.setActionText("WATCH AN AD").setDuration(5000).setActionTextColor(Color.WHITE).build().show()
                }
            } else {
                mainStatsRefreshLayout?.isRefreshing = false
                Snacky.builder().setActivity(this).info().setText("Make sure you select a player, season, gamemode, and region!").show()
            }
        }
    }

    private fun reloadStats(player: PrefPlayer) {
        mainStatsRefreshLayout.isRefreshing = true

        startPlayerStatsFunction(player.playerID, player.selectedShardID, player.selectedSeason!!)?.addOnSuccessListener {
            mainStatsRefreshLayout.isRefreshing = false
        }
    }

    private var selectedRegion: Int = -1
    private var selectedGamemode: Int = -1
    private var selectedSeason: Int = -1

    private var selectPlayerDialog: MaterialDialog? = null

    private fun setupTopNav() {
        stats_player_picker.setOnClickListener {
            selectPlayerDialog = MaterialDialog(this@MainStatsActivity)
                    .title(text = "Select Player")
                    .customListAdapter(playerListAdapter)
                    .positiveButton(text = "LINK NEW") { dialog ->
                        val addPlayerBottomSheet = AddPlayerBottomSheet()
                        addPlayerBottomSheet.show(supportFragmentManager, addPlayerBottomSheet.tag)
                        if (playersMap.size < 5 || mSharedPreferences.getBoolean("premiumV1", false)) {
//                            val addPlayerBottomSheet = AddPlayerBottomSheet()
//                            addPlayerBottomSheet.show(supportFragmentManager, addPlayerBottomSheet.tag)
                        } else if (playersMap.size >= 5 && !mSharedPreferences.getBoolean("premiumV1", false)) {
                            //Max players added and no premium.
                            /*MaterialDialog.Builder(this)
                                    .title("Max Players Reached")
                                    .content(R.string.upgrade)
                                    .backgroundColorRes(R.color.md_orange_700)
                                    .titleColorRes(R.color.md_white_1000)
                                    .contentColorRes(R.color.md_white_1000)
                                    .positiveColorRes(R.color.md_white_1000)
                                    .neutralColorRes(R.color.md_white_1000)
                                    .negativeColorRes(R.color.md_white_1000)
                                    .positiveText("GO PREMIUM ($2.99)")
                                    .negativeText("CANCEL")
                                    .onPositive { dialog, which ->
                                        val buyIntentBundle = iap?.getBuyIntent(3, packageName,
                                                "plus_v1", "inapp", "")

                                        val pendingIntent = buyIntentBundle?.getParcelable<PendingIntent>("BUY_INTENT")

                                        if (pendingIntent != null) {

                                            val REQUEST_CODE = 1002

                                            startIntentSenderForResult(pendingIntent.intentSender,
                                                    REQUEST_CODE, Intent(), 0, 0,
                                                    0)
                                        }
                                    }.show()*/
                        }
                    }
            selectPlayerDialog!!.show()
        }
        stats_region_picker.setOnClickListener {
            if (currentPlayer == null) {
                Toast.makeText(this@MainStatsActivity, "Must select player first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            MaterialDialog(this)
                    .title(text = "Select Region")
                    .listItemsSingleChoice(items = regions.toMutableList(), initialSelection = selectedRegion) { _, index, text ->
                        selectedRegion = index

                        val existingPlayer = Gson().fromJson(mSharedPreferences.getString("player-${mSharedPreferences.getString("selected-player-id", "")}", null), PrefPlayer::class.java)
                        if (existingPlayer != null) {
                            existingPlayer.selectedShardID = regionList[index].toLowerCase()
                            mSharedPreferences.edit().putString("player-${mSharedPreferences.getString("selected-player-id", "")}", Gson().toJson(existingPlayer)).apply()
                        }

                        val regionText = stats_region_picker.getChildAt(1) as TextView
                        regionText.text = text

                        setPlayerSelected(existingPlayer.playerID)
                    }
                    .show()
        }
        stats_gamemode_picker.setOnClickListener {
            if (currentPlayer == null) {
                Toast.makeText(this@MainStatsActivity, "Must select player first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            MaterialDialog(this)
                    .title(text = "Select Gamemode")
                    .listItemsSingleChoice(items = listOf("Solo TPP", "Solo FPP", "Duo TPP", "Duo FPP", "Squad TPP", "Squad FPP"), initialSelection = selectedGamemode) { _, index, text ->
                        selectedGamemode = index
                        stats_gamemode_text.text = text

                        val existingPlayer = Gson().fromJson(mSharedPreferences.getString("player-${mSharedPreferences.getString("selected-player-id", "")}", null), PrefPlayer::class.java)
                        if (existingPlayer != null) {
                            existingPlayer.selectedGamemode = modesList[index]
                            mSharedPreferences.edit().putString("player-${mSharedPreferences.getString("selected-player-id", "")}", Gson().toJson(existingPlayer)).apply()
                        }

                        setPlayerSelected(existingPlayer.playerID)
                    }
                    .show()
        }
        stats_season_picker.setOnClickListener {
            if (currentPlayer == null) {
                Toast.makeText(this@MainStatsActivity, "Must select player first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val regionInt = if (regionList[selectedRegion].contains("PC")) {
                1
            } else {
                2
            }
            MaterialDialog(this)
                    .title(text = "Select Season (" + stats_region_text.text + ")")
                    .listItemsSingleChoice(items = Seasons.getInstance().getSeasonListArray(regionInt), initialSelection = selectedSeason) { _, which, text ->
                        selectedSeason = which
                        var seasonID = text.toString()
                        if (seasonID.contains("Current")) {
                            seasonID = seasonID.replace(" .*".toRegex(), "")
                        }

                        stats_season_text.text = Seasons.getInstance().getSeasonListArray(regionInt)[selectedSeason]

                        val existingPlayer = Gson().fromJson(mSharedPreferences.getString("player-${mSharedPreferences.getString("selected-player-id", "")}", null), PrefPlayer::class.java)
                        if (existingPlayer != null) {
                            existingPlayer.selectedSeason = seasonID
                            mSharedPreferences.edit().putString("player-${mSharedPreferences.getString("selected-player-id", "")}", Gson().toJson(existingPlayer)).apply()
                        }

                        setPlayerSelected(existingPlayer.playerID)
                    }
                    .show()
        }
    }

    private var players: MutableList<PrefPlayer> = ArrayList()

    private val playerListAdapter: RecyclerView.Adapter<*> = SlimAdapter.create().register<PrefPlayer>(R.layout.player_choice_dialog_item) { player, injector ->
        val iconDrawable: Int = if (player.defaultShardID.contains("pc")) {
            R.drawable.windows_color
        } else {
            R.drawable.xbox_logo
        }
        injector.image(R.id.game_version_icon, iconDrawable)
        injector.text(R.id.player_select_name, player.playerName)

        injector.clicked(R.id.constraintLayout) {
            if (selectPlayerDialog != null && selectPlayerDialog!!.isShowing) {
                setPlayerSelected(playersMap[player.playerName].toString())
                selectPlayerDialog!!.dismiss()
            } else if (deletePlayerDialog != null && deletePlayerDialog!!.isShowing) {
                deletePlayer(player.playerName)
                deletePlayerDialog!!.dismiss()
            }
        }
    }.updateData(players)

    override fun onStop() {
        super.onStop()
        if (listener != null && listenerRef != null) {
            listenerRef!!.removeEventListener(listener!!)
            listener = null
            listenerRef = null
        }

        if (iap != null) {
            try {
                applicationContext?.unbindService(serviceConnection)
            } catch (e: Exception) {
            }
        }

        if (mRewardedVideoAd != null) {
            mRewardedVideoAd!!.rewardedVideoAdListener = null
            mRewardedVideoAd!!.destroy(applicationContext)
            mRewardedVideoAd = null
        }
    }

    private var listener: ChildEventListener? = null
    private var listenerRef: DatabaseReference? = null

    private fun loadPlayers() {
        if (listener != null && listenerRef != null) {
            listenerRef!!.removeEventListener(listener!!)
            listener = null
            listenerRef = null
        }
        players.clear()
        playersMap.clear()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().reference.child("users").child(currentUser!!.uid).child("pubg_players").orderByChild("playerName")
        listenerRef = ref.ref
        listener = ref.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val player: PrefPlayer

                if (mSharedPreferences.contains("player-${p0.key}")) {
                    //Player exists in shared prefs, update any needed items and add to list.
                    val existingPlayer = Gson().fromJson(mSharedPreferences.getString("player-${p0.key}", null), PrefPlayer::class.java)
                    existingPlayer.playerID = p0.key.toString()
                    existingPlayer.playerName = p0.child("playerName").value.toString()
                    existingPlayer.defaultShardID = p0.child("shardID").value.toString()

                    if (existingPlayer.selectedSeason.isNullOrEmpty()) {
                        val seasonID: String = if (existingPlayer.defaultShardID.contains("pc")) {
                            Seasons.getInstance().pcCurrentSeason
                        } else {
                            Seasons.getInstance().xboxCurrentSeason
                        }

                        existingPlayer.selectedSeason = seasonID
                    }

                    mSharedPreferences.edit().putString("player-${p0.key}", Gson().toJson(existingPlayer)).apply()

                    player = existingPlayer
                } else {
                    //Player doesn't exist.
                    val newPlayer = PrefPlayer(
                            playerID = p0.key.toString(),
                            playerName = p0.child("playerName").value.toString(),
                            defaultShardID = p0.child("shardID").value.toString(),
                            selectedGamemode = "solo"
                    )

                    val seasonID: String
                    seasonID = if (newPlayer.defaultShardID.contains("pc")) {
                        Seasons.getInstance().pcCurrentSeason
                    } else {
                        Seasons.getInstance().xboxCurrentSeason
                    }

                    newPlayer.selectedSeason = seasonID

                    mSharedPreferences.edit().putString("player-${p0.key}", Gson().toJson(newPlayer)).apply()

                    player = newPlayer

                    setPlayerSelected(p0.key.toString())
                }

                players.add(player)
                //players = players.sortedWith(compareBy {it.playerName}).toMutableList()
                playerListAdapter.notifyDataSetChanged()

                playersMap[player.playerName] = player.playerID
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (!p0.exists()) {
                    Snacky.builder().setView(bottom_navigation).warning().setText("You haven't linked a PUBG account yet.").setAction("LINK"
                    ) {
                        val addPlayerBottomSheet = AddPlayerBottomSheet()
                        addPlayerBottomSheet.show(supportFragmentManager, addPlayerBottomSheet.tag)
                    }.setDuration(Snacky.LENGTH_INDEFINITE).show()
                }
            }

        })
    }

    public fun setPlayerSelected(id: String) {
        no_player.visibility = View.GONE

        mSharedPreferences.edit().putString("selected-player-id", id).apply()

        val player = Gson().fromJson(mSharedPreferences.getString("player-$id", null), PrefPlayer::class.java)
        currentPlayer = player

        stats_player_text.text = player.playerName
        stats_region_text.text = regions[regionList.indexOf(player.selectedShardID.toUpperCase())]
        selectedRegion = regionList.indexOf(player.selectedShardID.toUpperCase())

        try {
            if (!player.selectedSeason.isNullOrEmpty()) {
                if (player.selectedShardID.contains("pc")) {
                    selectedSeason = Seasons.getInstance().getSeasonListArrayOG(1).indexOf(player.selectedSeason)
                    stats_season_text.text = Seasons.getInstance().getSeasonListArray(1)[selectedSeason].toString()
                } else {
                    selectedSeason = Seasons.getInstance().getSeasonListArrayOG(2).indexOf(player.selectedSeason)
                    stats_season_text.text = Seasons.getInstance().getSeasonListArray(2)[selectedSeason].toString()
                }
            }
        } catch (e: Exception) {
            try {
                if (player.selectedShardID.contains("pc")) {
                    selectedSeason = Seasons.getInstance().getCurrentSeasonInt(1)
                    stats_season_text.text = Seasons.getInstance().getSeasonListArray(1)[selectedSeason].toString()
                } else {
                    selectedSeason = Seasons.getInstance().getCurrentSeasonInt(2)
                    stats_season_text.text = Seasons.getInstance().getSeasonListArray(2)[selectedSeason].toString()
                }
            } catch (e: Exception) {
                Snacky.builder().setActivity(this).error().setText("Unknown Error Occurred").show()
            }
        }

        var gamemode = "solo"
        if (!player.selectedGamemode.isNullOrEmpty()) {
            gamemode = player.selectedGamemode.toString()
            selectedGamemode = modesList.indexOf(player.selectedGamemode)
        } else {
            selectedGamemode = modesList.indexOf(gamemode)
        }
        stats_gamemode_text.text = Telemetry().gameModes.getString(gamemode)

        reloadFragments(currentPlayer!!.playerID)
    }

    private fun setupBottomNav() {
        bottom_navigation.setOnNavigationItemSelectedListener { item ->
            if (currentPlayer == null || !mSharedPreferences.contains("selected-player-id")) {
                if (supportFragmentManager.findFragmentByTag("STATS") != null) {
                    val fragment: MainStatsFragment = supportFragmentManager.findFragmentByTag("STATS") as MainStatsFragment
                    supportFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
                } else if (supportFragmentManager.findFragmentByTag("MATCHES") != null) {
                    val fragment: MatchListFragment = supportFragmentManager.findFragmentByTag("MATCHES") as MatchListFragment
                    supportFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
                }
                return@setOnNavigationItemSelectedListener false
            }
            when (item.itemId) {
                R.id.your_stats_menu -> {
                    if (supportFragmentManager.findFragmentByTag("STATS") != null) return@setOnNavigationItemSelectedListener true
                    val yourStatsFragment = MainStatsFragment()
                    val mBundle = Bundle()
                    mBundle.putSerializable("player", currentPlayer)
                    yourStatsFragment.arguments = mBundle
                    supportFragmentManager.beginTransaction().replace(R.id.home_frame, yourStatsFragment, "STATS").commitAllowingStateLoss()
                }
                R.id.matches_menu -> {
                    if (supportFragmentManager.findFragmentByTag("MATCHES") != null) return@setOnNavigationItemSelectedListener true
                    val MatchListFragment = MatchListFragment()
                    val mBundle = Bundle()
                    mBundle.putSerializable("player", currentPlayer)
                    MatchListFragment.arguments = mBundle
                    supportFragmentManager.beginTransaction().replace(R.id.home_frame, MatchListFragment, "MATCHES").commitAllowingStateLoss()
                }
            }
            true
        }
    }

    private fun reloadFragments(id: String) {
        if (currentPlayer == null || !mSharedPreferences.contains("selected-player-id")) {
            if (supportFragmentManager.findFragmentByTag("STATS") != null) {
                val fragment: MainStatsFragment = supportFragmentManager.findFragmentByTag("STATS") as MainStatsFragment
                supportFragmentManager.beginTransaction().remove(fragment).commit()
            } else if (supportFragmentManager.findFragmentByTag("MATCHES") != null) {
                val fragment: MatchListFragment = supportFragmentManager.findFragmentByTag("MATCHES") as MatchListFragment
                supportFragmentManager.beginTransaction().remove(fragment).commit()
            }
            return
        }

        val mBundle = Bundle()
        mBundle.putSerializable("player", currentPlayer)

        val yourStatsFragment = MainStatsFragment()
        when (bottom_navigation.selectedItemId) {
            R.id.your_stats_menu -> {
                if (supportFragmentManager.findFragmentByTag("STATS") != null) {
                    val fragment: MainStatsFragment = supportFragmentManager.findFragmentByTag("STATS") as MainStatsFragment
                    try {
                        mBundle.putSerializable("previousPlayer", fragment.currentPlayer)
                    } catch (e: Exception) {
                        //I tried :/
                    }
                }
                yourStatsFragment.arguments = mBundle
                supportFragmentManager.beginTransaction().replace(R.id.home_frame, yourStatsFragment, "STATS").commit()
            }
            R.id.matches_menu -> {
                val MatchListFragment = MatchListFragment()
                MatchListFragment.arguments = mBundle
                supportFragmentManager.beginTransaction().replace(R.id.home_frame, MatchListFragment, "MATCHES").commit()
            }
            else -> {
                yourStatsFragment.arguments = mBundle
                supportFragmentManager.beginTransaction().replace(R.id.home_frame, yourStatsFragment, "STATS").commit()
            }
        }
    }

    private fun deletePlayer(name: String) {
        val playerID = playersMap[name]
        playersMap.remove(name)
        for (i in players) {
            if (i is PrefPlayer) {
                if (i.playerName == name) {
                    players.remove(i)
                }
            }
        }

        if (mSharedPreferences.getString("selected-player-id", "") == playerID) {
            //Deleted player is currently selected, remove that.
            mSharedPreferences.edit().remove("selected-player-id").remove("player-$playerID").apply()

            no_player.visibility = View.VISIBLE
            stats_player_text.text = "Select Player"
            stats_region_text.text = "Select Region"
            stats_gamemode_text.text = "Select Gamemode"
            stats_season_text.text = "Select Season"

            selectedSeason = -1
            selectedGamemode = -1
            selectedRegion = -1

            currentPlayer = null

            if (supportFragmentManager.findFragmentByTag("STATS") != null) {
                val fragment: MainStatsFragment = supportFragmentManager.findFragmentByTag("STATS") as MainStatsFragment
                supportFragmentManager.beginTransaction().remove(fragment).commit()
            } else if (supportFragmentManager.findFragmentByTag("MATCHES") != null) {
                val fragment: MatchListFragment = supportFragmentManager.findFragmentByTag("MATCHES") as MatchListFragment
                supportFragmentManager.beginTransaction().remove(fragment).commit()
            }
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        FirebaseDatabase.getInstance().getReference("users").child(currentUser!!.uid).child("pubg_players").child(playerID.toString()).removeValue()

        loadPlayers()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.game_stats, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private var deletePlayerDialog: MaterialDialog? = null

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.stats_delete_player) {
            deletePlayerDialog = MaterialDialog(this@MainStatsActivity)
                    .title(text = "Select Player to Delete")
                    .customListAdapter(playerListAdapter)
                    .negativeButton(text = "CANCEL")
            deletePlayerDialog!!.show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1002) {
            val responseCode = data?.getIntExtra("RESPONSE_CODE", 0)
            val purchaseData = data?.getStringExtra("INAPP_PURCHASE_DATA")
            val dataSignature = data?.getStringExtra("INAPP_DATA_SIGNATURE")

            if (resultCode == Activity.RESULT_OK) {
                val m2SharedPreferences = this.getSharedPreferences("com.austinhodak.pubgcenter", Context.MODE_PRIVATE)

                mSharedPreferences.edit().putBoolean("premiumV1", true).apply()
                m2SharedPreferences.edit().putBoolean("removeAds", true).apply()
                Snacky.builder().setActivity(this).info().setText("Thanks! Enjoy the new stuff!").show()
            }
        }
    }

    private fun startPlayerStatsFunction(playerID: String, shardID: String, seasonID: String): Task<Map<String, Any>>? {
        val data = java.util.HashMap<String, Any>()
        data["playerID"] = playerID
        data["shardID"] = shardID
        data["seasonID"] = seasonID

        return FirebaseFunctions.getInstance().getHttpsCallable("loadPlayerStats")?.call(data)?.continueWith { task ->
            val result = task.result.data as Map<String, Any>
            Log.d("REQUEST", result.toString())
            result
        }
    }

    fun getTimeSinceLastUpdated(): Long {
        if (lastUpdated == null) return 0
        return Math.abs(lastUpdated?.minus((System.currentTimeMillis() / 1000))!!)
    }

    override fun onRewardedVideoAdClosed() {
        if (hasReward) {
            Snacky.builder().setActivity(this).success().setText("Refresh Rewarded. Thank you!").show()
            reloadStats(currentPlayer!!)
            hasReward = false
        }
    }

    override fun onRewardedVideoAdLeftApplication() {
    }

    override fun onRewardedVideoAdLoaded() {
    }

    override fun onRewardedVideoAdOpened() {
    }

    override fun onRewardedVideoCompleted() {

    }

    private var hasReward: Boolean = false

    override fun onRewarded(p0: RewardItem?) {
        hasReward = true
        val bundle = Bundle()
        bundle.putString("refresh_type", "ad_reward")
        FirebaseAnalytics.getInstance(this).logEvent("stat_refresh", bundle)
    }

    override fun onRewardedVideoStarted() {
    }

    override fun onRewardedVideoAdFailedToLoad(p0: Int) {
        Log.d("ADERROR", p0.toString())
    }

    private var serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            iap = null
        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            iap = IInAppBillingService.Stub.asInterface(p1)
        }
    }
}