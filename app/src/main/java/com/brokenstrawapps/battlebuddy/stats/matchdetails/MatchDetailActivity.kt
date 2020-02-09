package com.brokenstrawapps.battlebuddy.stats.matchdetails

import android.content.Intent
import android.graphics.Color
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
import com.brokenstrawapps.battlebuddy.BuildConfig
import com.brokenstrawapps.battlebuddy.R
import com.brokenstrawapps.battlebuddy.Telemetry
import com.brokenstrawapps.battlebuddy.models.MatchTop
import com.brokenstrawapps.battlebuddy.snacky.Snacky
import com.brokenstrawapps.battlebuddy.stats.matchdetails.replay.MatchGod
import com.brokenstrawapps.battlebuddy.stats.matchdetails.replay.ReplayActivity
import com.brokenstrawapps.battlebuddy.utils.Auth.getUser
import com.brokenstrawapps.battlebuddy.utils.Premium
import com.brokenstrawapps.battlebuddy.utils.Regions
import com.brokenstrawapps.battlebuddy.viewmodels.MatchDetailViewModel
import com.brokenstrawapps.battlebuddy.viewmodels.models.MatchModel
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import kotlinx.android.synthetic.main.activity_match_detail.*
import org.jetbrains.anko.startActivity
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

    lateinit var mDrawer: Drawer

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
    private var match: MatchTop? = null

    private var mLoadingSnack: Snackbar? = null

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

        mLoadingSnack = Snacky.builder().setDuration(Snacky.LENGTH_INDEFINITE).setActivity(this).setText("Loading telemetry...").build()

        //matchID = intent.getStringExtra("matchID")
        //regionID = intent.getStringExtra("regionID")

        currentPlayerID = "account.${intent.getStringExtra("playerID") ?: ""}"

        FirebaseDynamicLinks.getInstance().getDynamicLink(intent).addOnSuccessListener {
            if (it == null) {
                matchID = intent.getStringExtra("matchID")
                regionID = intent.getStringExtra("regionID")

                //processIntent()
            } else {
                matchID = it.link?.getQueryParameter("matchId")
                regionID = it.link?.getQueryParameter("platform")

                match_detail_toolbar?.menu?.findItem(R.id.match_favorite)?.isVisible = false
            }

            viewModel.mMatchData.observe(this, matchDataObserver)
            viewModel.getMatchData(application, regionID!!, matchID!!, currentPlayerID)
        }.addOnFailureListener {
            matchID = intent.getStringExtra("matchID")
            regionID = intent.getStringExtra("regionID")

            //processIntent()

            viewModel.mMatchData.observe(this, matchDataObserver)
            viewModel.getMatchData(application, regionID!!, matchID!!, currentPlayerID)
        }

        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        match_loading_lottie?.visibility = View.VISIBLE
        match_loading_lottie?.playAnimation()

        mLoadingSnack?.show()

        //TODO UPDATE
        //GoodPrefs.getInstance().saveInt("matchDetailLaunchCount", (GoodPrefs.getInstance().getInt("matchDetailLaunchCount", 0) + 1))

        mInterstitialAd.adUnitId = "ca-app-pub-2981302488834327/1026987854"
        if (!Premium.isAdFreeUser()) {
            //mInterstitialAd.loadAd(Ads.getAdBuilder())
        }

        if (!Premium.isAdFreeUser()) {
            val statsBanner = AdView(this)
            statsBanner.adSize = com.google.android.gms.ads.AdSize.BANNER
            statsBanner.adUnitId = "ca-app-pub-2981302488834327/6726392316"
            //statsBanner.loadAd(Ads.getAdBuilder())
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
        MatchGod.match = matchModel

        mLoadingSnack?.dismiss()

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

        mDrawer.addStickyFooterItem(SecondaryDrawerItem().withIdentifier(10001).withName("Match Ping: ${capitalize(matchModel.eventList!!.getMatchDefinition().PingQuality)} Quality").withEnabled(false).withSelectable(false))

        match_loading_lottie?.pauseAnimation()
        match_loading_lottie?.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
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
                    toolbar_title.text = "Kill Feed"
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
            primaryItem("Care Packages") {
                icon = R.drawable.carepackage_open
                onClick { view, position, drawerItem ->
                    isNavigatedDown = false
                    supportFragmentManager.beginTransaction().replace(R.id.match_frame, CarePackageListFragment())
                            .commit()
                    toolbar_title.text = "Care Packages"
                    updateToolbarElevation(15f)
                    updateToolbarFlags(false)
                    false
                }
            }
            if (BuildConfig.DEBUG) {

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
                    secondaryItem ("2D Replay") {
                        icon = R.drawable.icons8_timeline
                        selectable = false
                        onClick { view, position, drawerItem ->
                            startActivity<ReplayActivity>()
                            false
                        }
                    }
                }
            }
        }

        headerGamemodeTV = mDrawer.header.findViewById(R.id.header_name)
        headerDurationTV = mDrawer.header.findViewById(R.id.header_upgrade)
        headerTimeTV = mDrawer.header.findViewById(R.id.header_ingame_name)
        headerRegionTV = mDrawer.header.findViewById(R.id.header_region)
        headerMapIV = mDrawer.header.findViewById(R.id.header_icon)
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
                return
            } else if (!Premium.isAdFreeUser()) {
                //Log.d("LAUNCH", GoodPrefs.getInstance().getInt("matchDetailLaunchCount", 0).toString())
                //if (GoodPrefs.getInstance().getInt("matchDetailLaunchCount", 0) >= 3) {
                //    GoodPrefs.getInstance().saveInt("matchDetailLaunchCount", 0)

                //    if (mInterstitialAd.isLoaded)
                //    mInterstitialAd.show()
                //}
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

        if (intent != null && intent.hasExtra("match")) {
            match = intent.getSerializableExtra("match") as MatchTop
            if (match?.isFavorite == true) {
                menu?.findItem(R.id.match_favorite)?.setIcon(R.drawable.ic_favorite_24dp)
            } else {
                menu?.findItem(R.id.match_favorite)?.setIcon(R.drawable.ic_favorite_border_24dp)
            }
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        Log.d("OPTION", "CLCIKED: ${item?.itemId}")
        when (item?.itemId) {
            R.id.match_share -> {
                shareMatch()
                return true
            }
            R.id.match_favorite -> {
                Log.d("FAVORITE", "CLICKED ${match?.isFavorite}")

                if (intent != null && intent.hasExtra("match") && match != null) {
                    if (match?.isFavorite == true) {
                        item.setIcon(R.drawable.ic_favorite_border_24dp)
                        match?.isFavorite = false

                        FirebaseDatabase.getInstance().getReference("user_stats/${intent.getStringExtra("playerID")
                                ?: return true}/allMatches/${Regions.getNewRegionID(regionID!!)}/${match?.season}/matches/$matchID/favorites/${getUser().uid}").removeValue()
                    } else {
                        item.setIcon(R.drawable.ic_favorite_24dp)
                        match?.isFavorite = true

                        Log.d("FAVORITE", "user_stats/${intent.getStringExtra("playerID")
                                ?: return true}/allMatches/${Regions.getNewRegionID(regionID!!)}/${match?.season}/matches/$matchID/favorites/${getUser().uid}")

                        FirebaseDatabase.getInstance().getReference("user_stats/${intent.getStringExtra("playerID")
                                ?: return true}/allMatches/${Regions.getNewRegionID(regionID!!)}/${match?.season}/matches/$matchID/favorites/${getUser().uid}").setValue(true)

                        Snacky.builder().setActivity(this).setBackgroundColor(Color.WHITE).setIcon(R.drawable.ic_favorite_24dp).setText("Match Favorited!").setTextColor(Color.parseColor("#FF026A")).build().show()
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun shareMatch() {
        Log.d("MATCH URI", createShareUri(matchID!!, regionID!!).toString())
        toast("This feature will be back soon, standby!")
        //createDynamicUri(createShareUri(matchID!!, regionID!!))
    }

    private fun createShareUri(matchId: String, shardId: String): Uri {
        val platformString = when {
            shardId.contains("kakao", true) -> "kakao"
            shardId.contains("pc", true) -> "steam"
            shardId.contains("xbox", true) -> "xbox"
            shardId.contains("psn", true) -> "psn"
            else -> "steam"
        }

        val firstURL = URLEncoder.encode("http://www.pubgbuddy.gg/match?matchId=$matchId&platform=$platformString", "UTF-8")

        val url = "https://pubgbuddy.page.link/?link=$firstURL&apn=com.brokenstrawapps.battlebuddy&amv=10401000"
        return Uri.parse(url)
    }

    private fun createDynamicUri(myUri: Uri) {
        val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(myUri)
                .buildShortDynamicLink(ShortDynamicLink.Suffix.SHORT)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val shortLink = task.result?.shortLink
                        val msg = "Check out my @PUBG Match on BattleSTAT (@PUBGBuddy): $shortLink"
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
