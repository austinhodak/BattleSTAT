package com.ahcjapps.battlebuddy.stats.main

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.builders.footer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.badgeable.secondaryItem
import co.zsmb.materialdrawerkt.draweritems.divider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.ahcjapps.battlebuddy.R
import com.ahcjapps.battlebuddy.models.Gamemode
import com.ahcjapps.battlebuddy.models.PlayerListModel
import com.ahcjapps.battlebuddy.premium.UpgradeActivity
import com.ahcjapps.battlebuddy.settings.SettingsActivity
import com.ahcjapps.battlebuddy.snacky.Snacky
import com.ahcjapps.battlebuddy.stats.PlayerListDialog
import com.ahcjapps.battlebuddy.stats.compare.ComparePlayerModel
import com.ahcjapps.battlebuddy.stats.compare.ComparePlayersActivity
import com.ahcjapps.battlebuddy.utils.*
import com.ahcjapps.battlebuddy.viewmodels.PlayerStatsViewModel
import com.ahcjapps.battlebuddy.viewmodels.models.PlayerModel
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.functions.FirebaseFunctionsException
import com.mikepenz.materialdrawer.Drawer
import kotlinx.android.synthetic.main.activity_new_home.*
import kotlinx.android.synthetic.main.activity_stats_home.*
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity

class StatsHome : AppCompatActivity(), RewardedVideoAdListener {

    private var player: PlayerListModel? = null
    private var playerModel: PlayerModel? = null
    private var mDrawer: Drawer? = null

    private var selectedSeason = 0
    private var selectedShard = 0

    private var mRewardedVideoAd: RewardedVideoAd? = null

    private var menu: Menu? = null

    private val viewModel: PlayerStatsViewModel by lazy {
        ViewModelProviders.of(this).get(PlayerStatsViewModel::class.java)
    }

    private var selectedTab: String? = null

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
            mRewardedVideoAd?.loadAd("ca-app-pub-1646739421365093/9274148507",
                    Ads.getAdBuilder())
        }

        receiveIntent()

       /* mDrawer = drawer {
            headerDivider = false
            headerViewRes = R.layout.main_drawer_header
            toolbar = find(R.id.toolbar)
            primaryItem(R.string.drawer_title_stats_enw) {
                icon = R.drawable.chart_color
                onClick { _ ->
                    startActivity<PlayerListDialog>()
                    true
                }
            }
            primaryItem("Lifetime Stats") {
                icon = R.drawable.icons8_time_span_96
                onClick { _ ->
                    true
                }
            }
            primaryItem("Matches") {
                icon = R.drawable.icons8_firing_gun_96
                onClick { _ ->
                    true
                }
            }
            primaryItem("Leaderboards") {
                icon = R.drawable.icons8_leaderboard_96
                onClick { _ ->
                    true
                }
            }
            divider()
            primaryItem("Weapon Progression") {
                icon = R.drawable.icons8_assault_rifle_96
                onClick { _ ->
                    true
                }
            }
            footer {
                secondaryItem ("2019 Season 1") {
                    icon = R.drawable.icons8_calendar_96
                    //description = "2019 Season 1"
                    onClick { _ ->
                        true
                    }
                }
                secondaryItem ("Xbox North America") {
                    icon = R.drawable.icons8_globe_96
                    //description = "Xbox North America"
                    onClick { _ ->
                        true
                    }
                }
            }
        }

        val headerText = mDrawer?.header?.findViewById<TextView>(R.id.header_name)
        headerText?.text = player?.playerName

        val levelText = mDrawer?.header?.findViewById<TextView>(R.id.header_upgrade)*/
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
                    stats_bottom_nav?.menu?.getItem(1)?.isEnabled = p0?.text != "Overall"

                    Log.d("TAB", p0?.text.toString())
                    tabSelected(p0?.text.toString(), selectedPlayer, stats_bottom_nav?.selectedItemId!!)
                }
            })

            viewModel.getPlayerStats(player!!)
            viewModel.playerData.observe(this, Observer<PlayerModel> {
                playerModel = it
                if (it.error != null && it.error == 1) {
                    loadPlayerStats(selectedPlayer)
                }
            })

            setupBottomNav(selectedPlayer)

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
                    mRewardedVideoAd?.loadAd("ca-app-pub-1646739421365093/9274148507",
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

        selectedPlayer.runGetStats().addOnSuccessListener {
            statsRefreshLayout?.isRefreshing = false
        }.addOnFailureListener { exception ->
            statsRefreshLayout?.isRefreshing = false

            if (exception is FirebaseFunctionsException) {
                val code = exception.code
                val details = exception.details

                when (code) {
                    FirebaseFunctionsException.Code.NOT_FOUND -> Snacky.builder().setActivity(this@StatsHome).error().setText("No stats available for selected season.").show()
                    FirebaseFunctionsException.Code.RESOURCE_EXHAUSTED -> Snacky.builder().setActivity(this@StatsHome).error().setText("API Limit Reached. Try again in a few seconds.").show()
                    else -> Snacky.builder().setActivity(this@StatsHome).error().setText("Error: ${exception.message}").show()
                }
            }
        }

        if (!selectedPlayer.isLifetimeSelected)
        selectedPlayer.runGetMatches()
    }

    private fun tabSelected(gameMode: String, player: PlayerListModel, itemId: Int) {
        selectedTab = gameMode
        player.isOverallStatsSelected = gameMode == "Overall"

        val mode = when (gameMode) {
            "Solo" -> Gamemode.SOLO
            "Solo FPP" -> Gamemode.SOLOFPP
            "Duo" -> Gamemode.DUO
            "Duo FPP" -> Gamemode.DUOFPP
            "Squad" -> Gamemode.SQUAD
            "Squad FPP" -> Gamemode.SQUADFPP
            else -> Gamemode.SOLO
        }

        val fragment = if (itemId == R.id.your_stats_menu || itemId == R.id.lifetime_stats) {
            MainStatsFragmentNew()
        } else if (itemId == R.id.stats_leaderboards) {
            LeaderboardFragment()
        } else {
            MatchListFragment()
        }

        player.selectedGamemode = mode
        val bundle = Bundle()
        bundle.putSerializable("selectedPlayer", player)

        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.stats_home_frame, fragment).commit()
    }

    private fun setupBottomNav(player: PlayerListModel) {
        if (player.isConsolePlayer()) {
            stats_bottom_nav?.menu?.removeItem(R.id.stats_leaderboards)
            //stats_bottom_nav?.menu?.removeItem(R.id.lifetime_stats)
        }
        stats_bottom_nav?.setOnNavigationItemSelectedListener {
            player.isLifetimeSelected = it.itemId == R.id.lifetime_stats
            viewModel.getPlayerStats(player)
            (stats_home_tabs?.getChildAt(0) as ViewGroup).getChildAt(6).isEnabled = it.itemId != R.id.matches_menu
            if (it.itemId == R.id.matches_menu) {
                tabSelected(stats_home_tabs?.getTabAt(stats_home_tabs?.selectedTabPosition!!)?.text.toString(), player, it.itemId)

                (stats_home_tabs?.getChildAt(0) as ViewGroup).getChildAt(6).alpha = 0.3f
            } else {
                tabSelected(stats_home_tabs?.getTabAt(stats_home_tabs?.selectedTabPosition!!)?.text.toString(), player, it.itemId)
                stats_home_tabs?.visibility = View.VISIBLE

                (stats_home_tabs?.getChildAt(0) as ViewGroup).getChildAt(6).alpha = 1f
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        if (mSharedPreferences != null && selectedTab == null) {
            when (Gamemode.valueOf(mSharedPreferences!!.getString("default_gamemode", "SOLO")!!)) {
                Gamemode.SOLO -> tabSelected("Solo", player!!, stats_bottom_nav?.selectedItemId!!)
                Gamemode.SOLOFPP -> tabSelected("Solo FPP", player!!, stats_bottom_nav?.selectedItemId!!)
                Gamemode.DUO -> tabSelected("Duo", player!!, stats_bottom_nav?.selectedItemId!!)
                Gamemode.DUOFPP -> tabSelected("Duo FPP", player!!, stats_bottom_nav?.selectedItemId!!)
                Gamemode.SQUAD -> tabSelected("Squad", player!!, stats_bottom_nav?.selectedItemId!!)
                Gamemode.SQUADFPP -> tabSelected("Squad FPP", player!!, stats_bottom_nav?.selectedItemId!!)
            }
            stats_home_tabs?.getTabAt(Gamemode.valueOf(mSharedPreferences!!.getString("default_gamemode", "SOLO")!!).ordinal)?.select()
        } else if (selectedTab == null) {
            tabSelected(stats_home_tabs?.getTabAt(stats_home_tabs?.selectedTabPosition!!)?.text.toString(), player!!, stats_bottom_nav?.selectedItemId!!)
        } else {
            tabSelected(selectedTab!!, player!!, stats_bottom_nav?.selectedItemId!!)
        }
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
                /*val dialog = MaterialDialog(this)
                        .customView(R.layout.dialog_quick_stats, noVerticalPadding = true)

                val view = dialog.getCustomView()

                val flexBox = view!!.findViewById<FlexboxLayout>(R.id.statsBox)
                flexBox.addView(com.austinh.battlebuddy.views.StatItem(this, statName = "Wins", statValue = "14"))
                flexBox.addView(com.austinh.battlebuddy.views.StatItem(this, statName = "Top 10s", statValue = "26"))
                flexBox.addView(com.austinh.battlebuddy.views.StatItem(this, statName = "Kills", statValue = "800"))
                flexBox.addView(com.austinh.battlebuddy.views.StatItem(this, statName = "Headshots", statValue = "254"))
                flexBox.addView(com.austinh.battlebuddy.views.StatItem(this, statName = "K/D", statValue = "5.30"))
                dialog.show()*/
            }
            R.id.compare_players -> {
                val intent = Intent(this@StatsHome, PlayerListDialog::class.java)
                intent.action = "pick"
                startActivityForResult(intent, 1)
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                startActivity<ComparePlayersActivity>("firstPlayer" to ComparePlayerModel(player!!, playerModel!!), "secondPlayer" to data?.getSerializableExtra("selectedPlayer"), "gamemode" to player!!.selectedGamemode)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(R.anim.nav_default_pop_enter_anim, R.anim.nav_default_pop_exit_anim)
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

        mRewardedVideoAd?.loadAd("ca-app-pub-1646739421365093/9274148507",
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