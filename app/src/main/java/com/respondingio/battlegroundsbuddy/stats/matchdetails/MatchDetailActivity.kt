package com.respondingio.battlegroundsbuddy.stats.matchdetails

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.Menu
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
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.respondingio.battlegroundsbuddy.BuildConfig
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.Telemetry
import com.respondingio.battlegroundsbuddy.utils.Ads
import com.respondingio.battlegroundsbuddy.utils.Premium
import com.respondingio.battlegroundsbuddy.viewmodels.MatchDetailViewModel
import com.respondingio.battlegroundsbuddy.viewmodels.models.MatchModel
import kotlinx.android.synthetic.main.activity_match_detail.*
import nouri.`in`.goodprefslib.GoodPrefs
import org.jetbrains.anko.toast
import org.json.JSONException
import java.net.URLEncoder
import java.util.regex.Pattern


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
    var currentPlayerID: String = ""

    var requestQueue: RequestQueue? = null

    var mInterstitialAd = InterstitialAd(this)

    private var matchID: String? = null
    private var regionID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_detail)
        setupToolbar()
        setupDrawer()
        requestQueue = Volley.newRequestQueue(this)
        val cache = DiskBasedCache(cacheDir, 100 * 1024 * 1024)
        requestQueue = RequestQueue(cache, BasicNetwork(HurlStack()))
        requestQueue!!.start()

        toolbar_title.text = "Your Match Stats"

        //matchID = intent.getStringExtra("matchID")
        //regionID = intent.getStringExtra("regionID")

        FirebaseDynamicLinks.getInstance().getDynamicLink(intent).addOnSuccessListener {
            if (it == null) {
                matchID = intent.getStringExtra("matchID")
                regionID = intent.getStringExtra("regionID")
            } else {
                matchID = it.link.getQueryParameter("matchId")
                regionID = it.link.getQueryParameter("platform")
                Log.d("MATCH", "${it.link} - $matchID - $regionID")
            }

            viewModel.mMatchData.observe(this, matchDataObserver)
            viewModel.getMatchData(application, regionID!!, matchID!!, currentPlayerID)

            //if (currentPlayerID.isEmpty())
            //mDrawer.setSelection(10)
        }.addOnFailureListener {
            matchID = intent.getStringExtra("matchID")
            regionID = intent.getStringExtra("regionID")

            viewModel.mMatchData.observe(this, matchDataObserver)
            viewModel.getMatchData(application, regionID!!, matchID!!, currentPlayerID)
        }

        currentPlayerID = "account.${intent.getStringExtra("playerID") ?: ""}"

        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        match_loading_lottie?.visibility = View.VISIBLE
        match_loading_lottie?.playAnimation()

        GoodPrefs.getInstance().saveInt("matchDetailLaunchCount", (GoodPrefs.getInstance().getInt("matchDetailLaunchCount", 0) + 1))

        mInterstitialAd.adUnitId = "ca-app-pub-1946691221734928/7664747817"
        if (!Premium.isAdFreeUser()) {
            mInterstitialAd.loadAd(Ads.getAdBuilder())
        }

        if (!Premium.isAdFreeUser()) {
            val statsBanner = AdView(this)
            statsBanner.adSize = com.google.android.gms.ads.AdSize.BANNER
            statsBanner.adUnitId = "ca-app-pub-1946691221734928/7236919124"
            statsBanner.loadAd(Ads.getAdBuilder())
            statsBanner.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    matchDetailBottomLL?.removeAllViews()
                    matchDetailBottomLL?.addView(statsBanner)
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
        }
    }

    private fun matchDataLoaded(matchModel: MatchModel) {
        match_loading_lottie?.pauseAnimation()
        match_loading_lottie?.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        headerMapIV?.setImageDrawable(resources.getDrawable(matchModel.getMapIcon()))

        try {
            headerGamemodeTV?.text = Telemetry().gameModes[matchModel.attributes?.gameMode].toString()
        } catch (e: JSONException) {
            headerGamemodeTV?.visibility = View.INVISIBLE
        }

        headerTimeTV?.text = matchModel.getFormattedCreatedAt()
        headerDurationTV?.text = DateUtils.formatElapsedTime(matchModel.attributes?.duration!!)
        headerRegionTV?.text = Telemetry().region[matchModel.attributes?.shardId].toString()

        if (matchModel.attributes?.gameMode!!.contains("solo", true)) mDrawer.removeItems(11, 12)

        mDrawer.updateBadge(10, StringHolder("${matchModel.participantList.size}"))
        mDrawer.updateBadge(11, StringHolder("${matchModel.rosterList.size}"))

        if (!currentPlayerID.isEmpty() && currentPlayerID != "account.") {
            mDrawer.setSelection(1)
        } else {
            mDrawer.removeItem(1)
            mDrawer.removeItem(12)
            mDrawer.setSelection(10)
        }

        mDrawer.addStickyFooterItem(SecondaryDrawerItem().withName("Match Ping: ${capitalize(matchModel.matchDefinition!!.PingQuality)} Quality").withEnabled(false).withSelectable(false))
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

                    updateToolbarElevation(15f)
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
                    updateToolbarElevation(15f)
                    updateToolbarFlags(false)
                    false
                }
            }
            primaryItem("Kill Tree") {
                icon = R.drawable.flow_chart
                onClick { view, position, drawerItem ->
                    isNavigatedDown = false
                    supportFragmentManager.beginTransaction().replace(R.id.match_frame, KillTreeFragment())
                            .commit()
                    toolbar_title.text = "Kill Tree"
                    updateToolbarElevation(15f)
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
                        updateToolbarElevation(15f)
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

        headerGamemodeTV = mDrawer.header.findViewById(R.id.header_name)
        headerDurationTV = mDrawer.header.findViewById(R.id.header_upgrade)
        headerTimeTV = mDrawer.header.findViewById(R.id.header_ingame_name)
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
        toolbar_title.text = "Match Stats"
    }

    fun updateToolbarElevation(int: Float) {
        ViewCompat.setElevation(app_bar, int)
    }

    fun updateToolbarFlags(canShowOnScroll: Boolean) {
        var params = match_detail_toolbar.layoutParams as AppBarLayout.LayoutParams
        if (canShowOnScroll) {
            //params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        } else {
            //params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
        }
        //app_bar.requestLayout()
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
            if (this::mDrawer.isInitialized && mDrawer.isDrawerOpen) {
                mDrawer.closeDrawer()
            } else if (!Premium.isAdFreeUser()) {
                Log.d("LAUNCH", GoodPrefs.getInstance().getInt("matchDetailLaunchCount", 0).toString())
                if (GoodPrefs.getInstance().getInt("matchDetailLaunchCount", 0) >= 3) {
                    GoodPrefs.getInstance().saveInt("matchDetailLaunchCount", 0)

                    if (mInterstitialAd.isLoaded)
                    mInterstitialAd.show()
                }
            }
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.match_detail_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        Log.d("OPTION", "CLCIKED: ${item?.itemId}")
        when (item?.itemId) {
            R.id.match_share -> {
                shareMatch()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun shareMatch() {
        Log.d("MATCH URI", createShareUri(matchID!!, regionID!!).toString())
        createDynamicUri(createShareUri(matchID!!, regionID!!))
    }

    private fun createShareUri(matchId: String, shardId: String): Uri {
        val platformString = when {
            shardId.contains("kakao", true) -> "kakao"
            shardId.contains("pc", true) -> "steam"
            shardId.contains("xbox", true) -> "xbox"
            shardId.contains("ps4", true) -> "ps4"
            else -> "steam"
        }

        val firstURL = URLEncoder.encode("http://www.pubgbuddy.gg/match?matchId=$matchId&platform=$platformString", "UTF-8")

        val url = "https://pubgbuddy.page.link/?link=$firstURL&apn=com.respondingio.battlegroundsbuddy&amv=10401000"
        return Uri.parse(url)
    }

    private fun createDynamicUri(myUri: Uri) {
        val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(myUri)
                .buildShortDynamicLink(ShortDynamicLink.Suffix.SHORT)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val shortLink = task.result?.shortLink
                        val msg = "Check out my @PUBG Match on Battle Buddy (@PUBGBuddy): $shortLink"
                        val sendIntent = Intent()
                        sendIntent.action = Intent.ACTION_SEND
                        sendIntent.putExtra(Intent.EXTRA_TEXT, msg)
                        sendIntent.type = "text/plain"
                        startActivity(sendIntent)
                    } else {
                        toast(task.exception?.message.toString())
                    }
                }
    }

    private fun capitalize(capString: String): String {
        val capBuffer = StringBuffer()
        val capMatcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString)
        while (capMatcher.find()) {
            capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase())
        }

        return capMatcher.appendTail(capBuffer).toString()
    }
}
