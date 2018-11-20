package com.respondingio.battlegroundsbuddy.stats.main

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.android.gms.tasks.Task
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.functions.FirebaseFunctions
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.models.PrefPlayer
import com.respondingio.battlegroundsbuddy.premium.UpgradeActivity
import com.respondingio.battlegroundsbuddy.snacky.Snacky
import com.respondingio.battlegroundsbuddy.utils.Ads
import com.respondingio.battlegroundsbuddy.utils.Premium
import com.respondingio.battlegroundsbuddy.utils.Regions
import com.respondingio.battlegroundsbuddy.utils.Seasons
import com.respondingio.battlegroundsbuddy.viewmodels.PlayerStatsViewModel
import com.respondingio.battlegroundsbuddy.viewmodels.models.PlayerModel
import kotlinx.android.synthetic.main.activity_stats_home.*
import org.jetbrains.anko.startActivity

class StatsHome : AppCompatActivity(), RewardedVideoAdListener {

    private var player: PrefPlayer? = null
    private var playerModel: PlayerModel? = null

    private var selectedSeason = 0
    private var selectedShard = 0

    private var mRewardedVideoAd: RewardedVideoAd? = null

    private var menu: Menu? = null

    private val viewModel: PlayerStatsViewModel by lazy {
        ViewModelProviders.of(this).get(PlayerStatsViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats_home)
        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener { onBackPressed() }

        stats_bottom_nav?.itemTextAppearanceActive = R.style.TabSelectedText
        stats_bottom_nav?.itemTextAppearanceInactive = R.style.TabUnselectedText

        statsRefreshLayout?.setColorSchemeColors(resources.getColor(R.color.timelineOrange))
        statsRefreshLayout?.setProgressBackgroundColorSchemeColor(resources.getColor(R.color.md_grey_850))

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(applicationContext)
        mRewardedVideoAd?.rewardedVideoAdListener = this

        if (!Premium.isUserLevel3() || !Premium.isUserLevel2()) {
            mRewardedVideoAd?.loadAd("ca-app-pub-1946691221734928/1941699809",
                    Ads.getAdBuilder())
        }

        receiveIntent()
    }

    private fun receiveIntent() {
        if (intent != null && intent.hasExtra("selectedPlayer")) {
            val selectedPlayer = intent.getSerializableExtra("selectedPlayer") as PrefPlayer
            player = selectedPlayer

            Log.d("SEASON", Regions.getRegionNames(selectedPlayer.selectedShardID, null).indexOf(selectedPlayer.selectedShardID).toString())
            selectedSeason = Seasons.getCurrentSeasonInt(selectedPlayer.selectedShardID)
            selectedShard = Regions.getRegionIDs(selectedPlayer.selectedShardID, null).indexOf(selectedPlayer.selectedShardID)

            toolbar_title?.text = selectedPlayer.playerName

            val bundle = Bundle()
            val fragmentNew = MainStatsFragmentNew()
            selectedPlayer.selectedGamemode = "solo"
            bundle.putSerializable("selectedPlayer", selectedPlayer)
            fragmentNew.arguments = bundle
            supportFragmentManager.beginTransaction().replace(R.id.stats_home_frame, fragmentNew).commit()

            when (Regions.getNewRegion(selectedPlayer.selectedShardID)) {
                Regions.Region.KAKAO,
                Regions.Region.STEAM -> {
                    toolbar_image?.setImageResource(R.drawable.windows_white)
                }
                Regions.Region.XBOX -> {
                    toolbar_image?.setImageResource(R.drawable.xbox_white)
                }
                Regions.Region.PS4 -> {
                    toolbar_image?.setImageResource(R.drawable.ic_icons8_playstation)
                }
            }

            stats_home_tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(p0: TabLayout.Tab?) {
                }

                override fun onTabUnselected(p0: TabLayout.Tab?) {
                }

                override fun onTabSelected(p0: TabLayout.Tab?) {
                    Log.d("TAB", p0?.text.toString())
                    tabSelected(p0?.text.toString(), selectedPlayer, stats_bottom_nav?.selectedItemId!!)
                }
            })

            viewModel.getPlayerStats(selectedPlayer.playerID, selectedPlayer.defaultShardID, selectedPlayer.selectedSeason!!, player!!)
            viewModel.playerData.observe(this, Observer<PlayerModel> {
                playerModel = it
            })

            setupBottomNav(selectedPlayer)

            Log.d("PLAYER", "${player?.isSeasonNewFormat(player?.defaultShardID!!)} --")

            statsRefreshLayout?.setOnRefreshListener {
                var updateTimeout = 15
                if (Premium.isUserLevel3()) {
                    updateTimeout = 2
                } else if (Premium.isUserLevel2()) {
                    updateTimeout = 5
                }

                if (playerModel != null && playerModel?.getMinutesSinceLastUpdated()!! > updateTimeout) {
                    loadPlayerStats(selectedPlayer)
                    return@setOnRefreshListener
                } else if (!Premium.isUserLevel2() || !Premium.isUserLevel3()) {
                    statsRefreshLayout?.isRefreshing = false
                    Snacky.builder().setActivity(this).setBackgroundColor(resources.getColor(R.color.timelineOrange)).setText("You can refresh once every $updateTimeout minutes.")
                            .setActionText("UPGRADE OR\nWATCH AN AD").setDuration(5000).setActionTextColor(Color.WHITE).setActionClickListener {
                                MaterialDialog(this)
                                        .title(text = "Upgrade or Watch An Ad")
                                        .message(text = "You can either watch a quick ad to refresh the stats or upgrade to a level that allows quicker refreshing!")
                                        .positiveButton(text = "UPGRADE") {
                                            startActivity<UpgradeActivity>()
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
                } else {
                    mRewardedVideoAd?.loadAd("ca-app-pub-3940256099942544/5224354917",
                            Ads.getAdBuilder())

                    statsRefreshLayout?.isRefreshing = false
                    Snacky.builder().setActivity(this).setBackgroundColor(resources.getColor(R.color.timelineOrange)).setText("You can refresh once every $updateTimeout minutes with premium. This is a PUBG API limit.").setActionClickListener {
                        if (mRewardedVideoAd?.isLoaded == true) {
                            mRewardedVideoAd?.show()
                        } else {
                            Snacky.builder().setActivity(this).info().setText("No ad loaded, please try again.").show()
                        }
                    }.setActionText("WATCH AN AD").setDuration(5000).setActionTextColor(Color.WHITE).build().show()
                }
            }
        }
    }

    private fun loadPlayerStats(selectedPlayer: PrefPlayer) {
        statsRefreshLayout?.isRefreshing = true
        var shardID = ""
        if (Regions.getNewRegion(selectedPlayer.selectedShardID) == Regions.Region.XBOX) {
            if (selectedPlayer.isSeasonNewFormat(selectedPlayer.selectedShardID)) {
                shardID = "xbox"
            } else {
                shardID = selectedPlayer.selectedShardID
            }
        } else {
            //PC
            if (selectedPlayer.isSeasonNewFormat(selectedPlayer.selectedShardID)) {
                if (Regions.getNewRegion(selectedPlayer.selectedShardID) == Regions.Region.STEAM) {
                    shardID = "steam"
                } else {
                    shardID = "kakao"
                }
            } else {
                if (selectedPlayer.selectedShardID == "steam") {
                    shardID = "pc-na"
                } else if (selectedPlayer.selectedShardID == "kakao") {
                    shardID = "pc-kakao"
                } else {
                    shardID = selectedPlayer.selectedShardID
                }
            }
        }
        startPlayerStatsFunction(selectedPlayer.playerID, shardID, player!!.selectedSeason!!)?.addOnSuccessListener {
            statsRefreshLayout?.isRefreshing = false
        }?.addOnFailureListener {
            statsRefreshLayout?.isRefreshing = false
        }
    }

    private fun tabSelected(gameMode: String, player: PrefPlayer, itemId: Int) {
        when (gameMode) {
            "Solo" -> {
                val fragment = if (itemId == R.id.your_stats_menu) {
                    MainStatsFragmentNew()
                } else if (itemId == R.id.stats_leaderboards) {
                    LeaderboardFragment()
                } else {
                    MatchListFragmentNew()
                }

                player.selectedGamemode = "solo"
                val bundle = Bundle()
                bundle.putSerializable("selectedPlayer", player)

                fragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.stats_home_frame, fragment).commit()
            }
            "Solo FPP" -> {
                val fragment = if (itemId == R.id.your_stats_menu) {
                    MainStatsFragmentNew()
                } else if (itemId == R.id.stats_leaderboards) {
                    LeaderboardFragment()
                } else {
                    MatchListFragmentNew()
                }

                player.selectedGamemode = "solo-fpp"
                val bundle = Bundle()
                bundle.putSerializable("selectedPlayer", player)

                fragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.stats_home_frame, fragment).commit()
            }
            "Duo" -> {
                val fragment = if (itemId == R.id.your_stats_menu) {
                    MainStatsFragmentNew()
                } else if (itemId == R.id.stats_leaderboards) {
                    LeaderboardFragment()
                } else {
                    MatchListFragmentNew()
                }

                player.selectedGamemode = "duo"
                val bundle = Bundle()
                bundle.putSerializable("selectedPlayer", player)

                fragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.stats_home_frame, fragment).commit()
            }
            "Duo FPP" -> {
                val fragment = if (itemId == R.id.your_stats_menu) {
                    MainStatsFragmentNew()
                } else if (itemId == R.id.stats_leaderboards) {
                    LeaderboardFragment()
                } else {
                    MatchListFragmentNew()
                }

                player.selectedGamemode = "duo-fpp"
                val bundle = Bundle()
                bundle.putSerializable("selectedPlayer", player)

                fragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.stats_home_frame, fragment).commit()
            }
            "Squad" -> {
                val fragment = if (itemId == R.id.your_stats_menu) {
                    MainStatsFragmentNew()
                } else if (itemId == R.id.stats_leaderboards) {
                    LeaderboardFragment()
                } else {
                    MatchListFragmentNew()
                }

                player.selectedGamemode = "squad"
                val bundle = Bundle()
                bundle.putSerializable("selectedPlayer", player)

                fragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.stats_home_frame, fragment).commit()
            }
            "Squad FPP" -> {
                val fragment = if (itemId == R.id.your_stats_menu) {
                    MainStatsFragmentNew()
                } else if (itemId == R.id.stats_leaderboards) {
                    LeaderboardFragment()
                } else {
                    MatchListFragmentNew()
                }

                player.selectedGamemode = "squad-fpp"
                val bundle = Bundle()
                bundle.putSerializable("selectedPlayer", player)

                fragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.stats_home_frame, fragment).commit()
            }
        }
    }

    private fun setupBottomNav(player: PrefPlayer) {
        if (player.selectedShardID.contains("xbox", true)) {
            stats_bottom_nav?.menu?.removeItem(R.id.stats_leaderboards)
        }
        stats_bottom_nav?.setOnNavigationItemSelectedListener {
            if (it.itemId == R.id.stats_war_mode) {
                //Load War Mode section
                stats_home_tabs?.visibility = View.GONE
            } else {
                tabSelected(stats_home_tabs?.getTabAt(stats_home_tabs?.selectedTabPosition!!)?.text.toString(), player, it.itemId)
                stats_home_tabs?.visibility = View.VISIBLE
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        tabSelected(stats_home_tabs?.getTabAt(stats_home_tabs?.selectedTabPosition!!)?.text.toString(), player!!, stats_bottom_nav?.selectedItemId!!)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.new_stats_home, menu)
        menu?.findItem(R.id.change_region)?.isVisible = !player?.isSeasonNewFormat(player?.defaultShardID!!)!!
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.change_region -> {
                if (player!!.defaultShardID == "xbox") {
                    if (player!!.isSeasonNewFormat("xbox")) {
                        selectedShard = -1
                    } else {
                        if (player!!.oldXboxShard != null) {
                            if (player!!.selectedShardID.toLowerCase() != player!!.oldXboxShard) {
                                selectedShard = Regions.getRegionIDs(player!!.selectedShardID, null).indexOf(player!!.selectedShardID.toUpperCase())
                            } else {
                                selectedShard = Regions.getRegionIDs(player!!.oldXboxShard
                                        ?: "xbox-na", null).indexOf(player!!.oldXboxShard!!.toUpperCase())
                            }
                        } else {
                            selectedShard = Regions.getRegionIDs(player!!.selectedShardID, null).indexOf(player!!.selectedShardID.toUpperCase())
                        }
                    }
                } else {
                    if (player!!.selectedShardID == "steam") {
                        selectedShard = 2
                    } else if (player!!.selectedShardID == "kakao") {
                        selectedShard = 6
                    } else {
                        selectedShard = Regions.getRegionIDs(player!!.selectedShardID, null).indexOf(player!!.selectedShardID.toUpperCase())
                    }
                }
                MaterialDialog(this@StatsHome)
                        .listItemsSingleChoice(items = Regions.getRegionNames(player!!.selectedShardID, player!!.selectedSeason!!).toMutableList(), initialSelection = selectedShard) { _, index, text ->
                            selectedShard = index
                            player?.selectedShardID = Regions.getRegionIDs(text, null)[Regions.getRegionNames(text, null).indexOf(text)].toLowerCase()

                            viewModel.getPlayerStats(player!!.playerID, player!!.selectedShardID, player!!.selectedSeason!!, player!!)

                            if (stats_bottom_nav?.selectedItemId == R.id.matches_menu) {
                                tabSelected(stats_home_tabs?.getTabAt(stats_home_tabs?.selectedTabPosition!!)?.text.toString(), player!!, stats_bottom_nav?.selectedItemId!!)
                            }
                        }
                        .title(text = "Change Region")
                        .show()
            }
            R.id.change_season -> {
                MaterialDialog(this@StatsHome)
                        .listItemsSingleChoice(items = Seasons.getSeasonsListForShard(player!!.selectedShardID, true), initialSelection = selectedSeason) { _, index, text ->
                            selectedSeason = index
                            player?.selectedSeason = Seasons.getSeasonsListForShard(player!!.selectedShardID, false)[index]

                            viewModel.getPlayerStats(player!!.playerID, player!!.selectedShardID, player!!.selectedSeason!!, player!!)

                            if (stats_bottom_nav?.selectedItemId == R.id.matches_menu) {
                                tabSelected(stats_home_tabs?.getTabAt(stats_home_tabs?.selectedTabPosition!!)?.text.toString(), player!!, stats_bottom_nav?.selectedItemId!!)
                            }

                            menu?.findItem(R.id.change_region)?.isVisible = !player?.isSeasonNewFormat(player?.defaultShardID!!)!!
                        }
                        .title(text = "Change Season")
                        .show()
            }
        }
        return true
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(R.anim.nav_default_pop_enter_anim, R.anim.nav_default_pop_exit_anim)
        // overridePendingTransition(R.anim.instabug_fadein, R.anim.instabug_fadeout)
    }

    private fun startPlayerStatsFunction(playerID: String, shardID: String, seasonID: String): Task<Map<String, Any>>? {
        val data = java.util.HashMap<String, Any>()
        data["playerID"] = playerID
        data["shardID"] = shardID.toLowerCase()
        data["seasonID"] = seasonID.toLowerCase()

        return FirebaseFunctions.getInstance().getHttpsCallable("loadPlayerStats")?.call(data)?.continueWith { task ->
            val result = task.result?.data as Map<String, Any>
            Log.d("REQUEST", result.toString())
            result
        }
    }

    fun setRefreshing(isRefreshing: Boolean) {
        statsRefreshLayout?.isRefreshing = isRefreshing
    }

    override fun onRewardedVideoAdClosed() {
        if (hasReward) {
            Snacky.builder().setActivity(this).success().setText("Refresh Rewarded. Thank you!").show()
            loadPlayerStats(player!!)
            hasReward = false
        }

        mRewardedVideoAd?.loadAd("ca-app-pub-1946691221734928/1941699809",
                Ads.getAdBuilder())
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
}