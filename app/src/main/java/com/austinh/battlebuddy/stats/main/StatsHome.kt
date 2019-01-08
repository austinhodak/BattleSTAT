package com.austinh.battlebuddy.stats.main

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinh.battlebuddy.R
import com.austinh.battlebuddy.models.Gamemode
import com.austinh.battlebuddy.models.PlayerListModel
import com.austinh.battlebuddy.premium.UpgradeActivity
import com.austinh.battlebuddy.settings.SettingsActivity
import com.austinh.battlebuddy.snacky.Snacky
import com.austinh.battlebuddy.utils.*
import com.austinh.battlebuddy.viewmodels.PlayerStatsViewModel
import com.austinh.battlebuddy.viewmodels.models.PlayerModel
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_stats_home.*
import org.jetbrains.anko.startActivity

class StatsHome : AppCompatActivity(), RewardedVideoAdListener {

    private var player: PlayerListModel? = null
    private var playerModel: PlayerModel? = null

    private var selectedSeason = 0
    private var selectedShard = 0

    private var mRewardedVideoAd: RewardedVideoAd? = null

    private var menu: Menu? = null

    private val viewModel: PlayerStatsViewModel by lazy {
        ViewModelProviders.of(this).get(PlayerStatsViewModel::class.java)
    }

    private var mSharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats_home)
        setSupportActionBar(toolbar)

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        toolbar.setNavigationOnClickListener { onBackPressed() }

        stats_bottom_nav?.itemTextAppearanceActive = R.style.TabSelectedText
        stats_bottom_nav?.itemTextAppearanceInactive = R.style.TabUnselectedText

        statsRefreshLayout?.setColorSchemeColors(resources.getColor(R.color.timelineOrange))
        statsRefreshLayout?.setProgressBackgroundColorSchemeColor(resources.getColor(R.color.md_grey_900))

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(applicationContext)
        mRewardedVideoAd?.rewardedVideoAdListener = this

        if (!Premium.isUserLevel3() || !Premium.isUserLevel2()) {
            mRewardedVideoAd?.loadAd("ca-app-pub-2237535196399997/3853662528",
                    Ads.getAdBuilder())
        }

        receiveIntent()
    }

    private fun receiveIntent() {
        if (intent != null && intent.hasExtra("selectedPlayer")) {
            val selectedPlayer = intent.getSerializableExtra("selectedPlayer") as PlayerListModel
            player = selectedPlayer

            selectedSeason = 0
            selectedShard = Regions.getShortRegionIDs(player!!.platform).indexOf("na")

            toolbar_title?.text = selectedPlayer.playerName

            val bundle = Bundle()
            val fragmentNew = MainStatsFragmentNew()
            selectedPlayer.selectedGamemode = Gamemode.SOLO
            bundle.putSerializable("selectedPlayer", selectedPlayer)
            fragmentNew.arguments = bundle
            supportFragmentManager.beginTransaction().replace(R.id.stats_home_frame, fragmentNew).commit()

            when (selectedPlayer.platform) {
                Platform.KAKAO,
                Platform.STEAM -> {
                    toolbar_image?.setImageResource(R.drawable.windows_white)
                }
                Platform.XBOX -> {
                    toolbar_image?.setImageResource(R.drawable.xbox_white)
                }
                Platform.PS4 -> {
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

            viewModel.getPlayerStats(player!!)
            viewModel.playerData.observe(this, Observer<PlayerModel> {
                playerModel = it
                if (it.error != null && it.error == 1) {
                    player!!.runGetStats()
                }
            })

            setupBottomNav(selectedPlayer)

            //Log.d("PLAYER", "${player?.isSeasonNewFormat(player?.defaultShardID!!)} --")

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

    private fun loadPlayerStats(selectedPlayer: PlayerListModel) {
        statsRefreshLayout?.isRefreshing = true
        /*var shardID = ""
        if (Regions.getNewRegion(selectedPlayer.selectedShardID) == Regions.Region.XBOX) {
            if (selectedPlayer.isSeasonNewFormat(selectedPlayer.selectedShardID)) {
                shardID = "xbox"
            } else {
                shardID = selectedPlayer.selectedShardID
            }
        } else if (Regions.getNewRegion(selectedPlayer.selectedShardID) == Regions.Region.PS4) {
            if (selectedPlayer.isSeasonNewFormat(selectedPlayer.selectedShardID)) {
                shardID = "psn"
            } else if (selectedPlayer.selectedShardID == "psn") {
                shardID = "psn-na"
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
        }*/

        //Log.d("PLAYER LOAD", shardID)

        selectedPlayer.runGetStats().addOnSuccessListener {
            statsRefreshLayout?.isRefreshing = false
        }.addOnFailureListener {
            statsRefreshLayout?.isRefreshing = false
        }

        selectedPlayer.runGetMatches()
    }

    private fun tabSelected(gameMode: String, player: PlayerListModel, itemId: Int) {
        when (gameMode) {
            "Solo" -> {
                val fragment = if (itemId == R.id.your_stats_menu) {
                    MainStatsFragmentNew()
                } else if (itemId == R.id.stats_leaderboards) {
                    LeaderboardFragment()
                } else {
                    MatchListFragmentNew()
                }

                player.selectedGamemode = Gamemode.SOLO
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

                player.selectedGamemode = Gamemode.SOLOFPP
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

                player.selectedGamemode = Gamemode.DUO
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

                player.selectedGamemode = Gamemode.DUOFPP
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

                player.selectedGamemode = Gamemode.SQUAD
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

                player.selectedGamemode = Gamemode.SQUADFPP
                val bundle = Bundle()
                bundle.putSerializable("selectedPlayer", player)

                fragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(R.id.stats_home_frame, fragment).commit()
            }
        }
    }

    private fun setupBottomNav(player: PlayerListModel) {
        if (player.isConsolePlayer()) {
            stats_bottom_nav?.menu?.removeItem(R.id.stats_leaderboards)
        }
        stats_bottom_nav?.setOnNavigationItemSelectedListener {
            if (it.itemId == R.id.matches_menu) {
                tabSelected(stats_home_tabs?.getTabAt(stats_home_tabs?.selectedTabPosition!!)?.text.toString(), player, it.itemId)
                //stats_home_tabs?.visibility = View.GONE
                //playerListToolbarWaterfall?.elevation = 0f
            } else {
                tabSelected(stats_home_tabs?.getTabAt(stats_home_tabs?.selectedTabPosition!!)?.text.toString(), player, it.itemId)
                stats_home_tabs?.visibility = View.VISIBLE
                //playerListToolbarWaterfall?.elevation = 15f
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        if (mSharedPreferences != null) {
            when (Gamemode.valueOf(mSharedPreferences!!.getString("default_gamemode", "SOLO")!!)) {
                Gamemode.SOLO -> tabSelected("Solo", player!!, stats_bottom_nav?.selectedItemId!!)
                Gamemode.SOLOFPP -> tabSelected("Solo FPP", player!!, stats_bottom_nav?.selectedItemId!!)
                Gamemode.DUO -> tabSelected("Duo", player!!, stats_bottom_nav?.selectedItemId!!)
                Gamemode.DUOFPP -> tabSelected("Duo FPP", player!!, stats_bottom_nav?.selectedItemId!!)
                Gamemode.SQUAD -> tabSelected("Squad", player!!, stats_bottom_nav?.selectedItemId!!)
                Gamemode.SQUADFPP -> tabSelected("Squad FPP", player!!, stats_bottom_nav?.selectedItemId!!)
            }
            stats_home_tabs?.getTabAt(Gamemode.valueOf(mSharedPreferences!!.getString("default_gamemode", "SOLO")!!).ordinal)?.select()
        }
        else
            tabSelected(stats_home_tabs?.getTabAt(stats_home_tabs?.selectedTabPosition!!)?.text.toString(), player!!, stats_bottom_nav?.selectedItemId!!)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.new_stats_home, menu)
        menu?.findItem(R.id.change_region)?.isVisible = !Seasons.isSeasonNewFormat(player!!.platform)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.change_region -> {
                MaterialDialog(this@StatsHome)
                        .listItemsSingleChoice(items = Regions.getRegionNames(player!!.platform), initialSelection = selectedShard) { _, index, text ->
                            selectedShard = index
                            //player?.selectedRegion = Regions.getRegionIDs(text, null)[Regions.getRegionNames(text, null).indexOf(text)].toLowerCase()
                            player?.selectedRegion = Regions.getShortRegionIDs(player!!.platform)[(Regions.getRegionNames(player!!.platform).indexOf(text))].toLowerCase()

                            viewModel.getPlayerStats(player!!)

                            if (stats_bottom_nav?.selectedItemId == R.id.matches_menu) {
                                tabSelected(stats_home_tabs?.getTabAt(stats_home_tabs?.selectedTabPosition!!)?.text.toString(), player!!, stats_bottom_nav?.selectedItemId!!)
                            }
                        }
                        .title(text = "Change Region")
                        .show()
            }
            R.id.change_season -> {
                MaterialDialog(this@StatsHome)
                        .listItemsSingleChoice(items = Seasons.getSeasonStringList(player!!.platform, true), initialSelection = selectedSeason) { _, index, text ->
                            selectedSeason = index
                            player?.selectedSeason = Seasons.getSeasonsForPlatform(player!!.platform).find { it.name == text }!!

                            viewModel.getPlayerStats(player!!)

                            if (stats_bottom_nav?.selectedItemId == R.id.matches_menu) {
                                tabSelected(stats_home_tabs?.getTabAt(stats_home_tabs?.selectedTabPosition!!)?.text.toString(), player!!, stats_bottom_nav?.selectedItemId!!)
                            }

                            menu?.findItem(R.id.change_region)?.isVisible = !Seasons.isSeasonNewFormat(player!!.platform, player!!.selectedSeason)
                        }
                        .title(text = "Change Season")
                        .show()
            }
            R.id.settings -> {
                startActivity<SettingsActivity>()
            }
        }
        return true
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(R.anim.nav_default_pop_enter_anim, R.anim.nav_default_pop_exit_anim)
        // overridePendingTransition(R.anim.instabug_fadein, R.anim.instabug_fadeout)
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

        mRewardedVideoAd?.loadAd("ca-app-pub-2237535196399997/3853662528",
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

    fun setTabVisibility(visible: Int) {
        stats_home_tabs?.visibility = visible
    }
}