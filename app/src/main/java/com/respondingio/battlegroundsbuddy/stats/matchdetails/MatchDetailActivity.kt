package com.respondingio.battlegroundsbuddy.stats.matchdetails

import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.builders.footer
import co.zsmb.materialdrawerkt.draweritems.badge
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.badgeable.secondaryItem
import co.zsmb.materialdrawerkt.draweritems.divider
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.Volley
import com.google.android.material.appbar.AppBarLayout
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.respondingio.battlegroundsbuddy.BuildConfig
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.Telemetry
import com.respondingio.battlegroundsbuddy.models.LogPlayerKill
import com.respondingio.battlegroundsbuddy.snacky.Snacky
import com.respondingio.battlegroundsbuddy.viewmodels.MatchDetailViewModel
import com.respondingio.battlegroundsbuddy.viewmodels.models.MatchModel
import kotlinx.android.synthetic.main.activity_match_detail.app_bar
import kotlinx.android.synthetic.main.activity_match_detail.match_detail_toolbar
import kotlinx.android.synthetic.main.activity_match_detail.match_loading_lottie
import kotlinx.android.synthetic.main.activity_match_detail.toolbar_title
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList


class MatchDetailActivity : AppCompatActivity() {

    private val viewModel: MatchDetailViewModel by lazy {
        ViewModelProviders.of(this).get(MatchDetailViewModel::class.java)
    }

    private val matchDataObserver = Observer<MatchModel> {
        value -> value?.let {
            matchDataLoaded(it)
        }
    }

    private lateinit var mDrawer: Drawer

    private var headerGamemodeTV: TextView? = null
    private var headerDurationTV: TextView? = null
    private var headerTimeTV: TextView? = null
    private var headerRegionTV: TextView? = null
    private var headerMapIV: ImageView? = null
    var currentPlayerID: String? = null
    var currentPlayerAccountID: String? = null

    var requestQueue: RequestQueue? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_detail)
        setupToolbar()
        setupDrawer()
        requestQueue = Volley.newRequestQueue(this)
        val cache = DiskBasedCache(cacheDir, 100 * 1024 * 1024)
        requestQueue = RequestQueue(cache, BasicNetwork(HurlStack()))
        requestQueue!!.start()

        if (!intent.hasExtra("matchID")) {
            Snacky.builder().setActivity(this).error().setText("No Match ID").show()
            return
        }

        toolbar_title.text = "Your Match Stats"

        val matchID = intent.getStringExtra("matchID")
        val regionID = intent.getStringExtra("regionID")
        currentPlayerID =  "account.${intent.getStringExtra("playerID")}"

        mDrawer.addStickyFooterItem(SecondaryDrawerItem().withName(matchID).withEnabled(false).withSelectable(false))
        Log.d("MATCHID", matchID)

        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        match_loading_lottie?.visibility = View.VISIBLE
        match_loading_lottie?.playAnimation()

        viewModel.mMatchData.observe(this, matchDataObserver)
        viewModel.getMatchData(application, regionID, matchID, currentPlayerID!!)
    }

    private fun matchDataLoaded(matchModel: MatchModel) {
        match_loading_lottie?.pauseAnimation()
        match_loading_lottie?.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        headerMapIV?.setImageDrawable(resources.getDrawable(matchModel.getMapIcon()))
        headerGamemodeTV?.text = Telemetry().gameModes[matchModel.attributes?.gameMode].toString()
        headerTimeTV?.text = matchModel.getFormattedCreatedAt()
        headerDurationTV?.text = DateUtils.formatElapsedTime(matchModel.attributes?.duration!!)
        headerRegionTV?.text = Telemetry().region[matchModel.attributes?.shardId].toString()

        if (matchModel.attributes?.gameMode!!.contains("solo", true)) mDrawer.removeItems(11, 12)

        mDrawer.updateBadge(10, StringHolder("${matchModel.participantList.size}"))
        mDrawer.updateBadge(11, StringHolder("${matchModel.rosterList.size}"))

        mDrawer.setSelection(1)
    }

    private fun setupDrawer() {
        mDrawer = drawer {
            toolbar = match_detail_toolbar
            headerViewRes = R.layout.drawer_header
            headerDivider = false
            stickyFooterShadow = false
            selectedItem = -1
            primaryItem("Your Match Stats") {
                icon = R.drawable.icons8_person_male
                identifier = 1
                onClick { _, _, _ ->
                    isNavigatedDown = false
                    val matchesPlayerStatsFragment = MatchPlayerStatsFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.match_frame, matchesPlayerStatsFragment)
                            .commit()

                    updateToolbarElevation(0f)
                    updateToolbarFlags(false)

                    toolbar_title.text = "Your Match Stats"
                    false
                }
            }
            primaryItem("Your Team's Stats") {
                icon = R.drawable.icons8_group
                identifier = 12
                onClick { view, position, drawerItem ->
                    isNavigatedDown = false
                    val matchesPlayerStatsFragment = MatchYourTeamsStatsFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.match_frame, matchesPlayerStatsFragment)
                            .commit()

                    toolbar_title.text = "Your Team's Stats"
                    updateToolbarElevation(0f)

                    var params = match_detail_toolbar.layoutParams as AppBarLayout.LayoutParams
                    params.scrollFlags = 0
                    app_bar.requestLayout()
                    false
                }
            }
            primaryItem("Teams") {
                icon = R.drawable.icons8_parallel_tasks
                badge("28") {
                    cornersDp = 20
                    paddingHorizontalDp = 6
                    textColorRes = R.color.md_white_1000
                    colorRes = R.color.md_orange_600
                }
                identifier = 11
                onClick { _, _, _ ->
                    isNavigatedDown = false
                    supportFragmentManager.beginTransaction().replace(R.id.match_frame, MatchTeamsFragment())
                            .commit()
                    toolbar_title.text = "Teams"
                    updateToolbarElevation(15f)
                    updateToolbarFlags(true)
                    false
                }
            }
            primaryItem("Players") {
                icon = R.drawable.icons8_player_male
                badge("97") {
                    cornersDp = 20
                    paddingHorizontalDp = 6
                    textColorRes = R.color.md_white_1000
                    colorRes = R.color.md_orange_600
                }
                identifier = 10
                onClick { view, position, drawerItem ->
                    isNavigatedDown = false
                    supportFragmentManager.beginTransaction().replace(R.id.match_frame, MatchPlayersFragment()).commit()
                    toolbar_title.text = "Players"
                    updateToolbarElevation(15f)
                    updateToolbarFlags(true)
                    false
                }
            }
            divider {}
            primaryItem("Kill Feed") {
                icon = R.drawable.icons8_horror_96
                onClick { view, position, drawerItem ->
                    isNavigatedDown = false
                    supportFragmentManager.beginTransaction().replace(R.id.match_frame, KillFeedFragment())
                            .commit()
                    toolbar_title.text = "Kills"
                    updateToolbarElevation(0f)
                    updateToolbarFlags(false)
                    false
                }
            }
            if (BuildConfig.DEBUG) {
                primaryItem("Care Packages") {
                    icon = R.drawable.carepackage_open
                }
                primaryItem("Weapon Stats") {
                    icon = R.drawable.icons8_rifle
                    onClick { view, position, drawerItem ->
                        isNavigatedDown = false
                        supportFragmentManager.beginTransaction().replace(R.id.match_frame, MatchWeaponStatsFragment())
                                .commit()
                        toolbar_title.text = "Weapon Stats"
                        updateToolbarElevation(0f)
                        updateToolbarFlags(false)
                        false
                    }
                }
                footer {
                    secondaryItem ("All Match Events") {
                        icon = R.drawable.icons8_timeline
                    }
                }
            }
        }

        headerGamemodeTV = mDrawer.header.findViewById(R.id.header_gamemode)
        headerDurationTV = mDrawer.header.findViewById(R.id.header_duration)
        headerTimeTV = mDrawer.header.findViewById(R.id.header_time)
        headerRegionTV = mDrawer.header.findViewById(R.id.header_region)
        headerMapIV = mDrawer.header.findViewById(R.id.header_icon)

        /*if (BuildConfig.DEBUG) {
            drawer{
                selectedItem = -1
                gravity = Gravity.END
                closeOnClick = false
                toolbar = match_detail_toolbar
                sectionHeader("Show In Map")
                switchItem("Care Packages") {
                    selectable = false
                    icon = R.drawable.carepackage_open
                }
            }
        }*/
    }

    private fun setupToolbar() {
        setSupportActionBar(match_detail_toolbar)
        toolbar_title.text = "Your Match Stats"
    }

    fun updateToolbarElevation(int: Float) {
        ViewCompat.setElevation(app_bar, int)
    }

    fun updateToolbarFlags(canShowOnScroll: Boolean) {
        var params = match_detail_toolbar.layoutParams as AppBarLayout.LayoutParams
        if (canShowOnScroll) {
            params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        } else {
            params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
        }
        app_bar.requestLayout()
    }

    override fun onBackPressed() {
        if (isNavigatedDown) {
            isNavigatedDown = false

            if (mDrawer.currentSelection == 11.toLong()) {
                mDrawer.setSelection(11)
            } else if (mDrawer.currentSelection == 10.toLong()) {
                mDrawer.setSelection(10)
            }
        } else {
            super.onBackPressed()
        }
    }

    private var isNavigatedDown = false

    fun showPlayerStatsFragment(bundle: Bundle) {
        isNavigatedDown = true

        val playerStats = MatchPlayerStatsFragment()
        playerStats.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.match_frame, playerStats)
                .commit()
    }

    var telemetryJson: JSONArray? = null


    /*private fun parseTelemetry(json: JSONArray) {
        telemetryJson = json
        killFeedList.clear()
        for (i in 0 until json.length()) {
            var item = json.getJSONObject(i)

            when (item["_T"]) {
                "LogPlayerKill" -> {
                    killFeedList.add(Gson().fromJson(item.toString(), LogPlayerKill::class.java))
                }
            }
        }

        Log.d("LogPlayerKill", "SIZE: ${killFeedList[2].killer.name}")

        killFeedList.sortedWith(compareBy { it._D })

        match_pg?.visibility = View.GONE
        match_loading_lottie?.pauseAnimation()
        match_loading_lottie?.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        //mDrawer.openDrawer()

        val bundle = Bundle()
        bundle.putString("playerID", currentPlayerID)
        bundle.putSerializable("player", participantHash[currentPlayerAccountID])
        bundle.putSerializable("participantList", participantList)
        bundle.putSerializable("rosterList", rosterList)
        bundle.putSerializable("killList", killFeedList)
        bundle.putString("matchCreatedAt", matchData?.getJSONObject("data")?.getJSONObject("attributes")?.getString("createdAt"))
        val matchesPlayerStatsFragment = MatchPlayerStatsFragment()
        matchesPlayerStatsFragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.match_frame, matchesPlayerStatsFragment)
                .commitAllowingStateLoss()

        updateToolbarElevation(0f)
        updateToolbarFlags(false)

        toolbar_title.text = "Your Match Stats"
    }*/

    var killFeedList = ArrayList<LogPlayerKill>()

    var matchData: JSONObject? = null

}