package com.respondingio.battlegroundsbuddy.stats

import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.builders.footer
import co.zsmb.materialdrawerkt.draweritems.badge
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.badgeable.secondaryItem
import co.zsmb.materialdrawerkt.draweritems.divider
import co.zsmb.materialdrawerkt.draweritems.sectionHeader
import co.zsmb.materialdrawerkt.draweritems.switchable.switchItem
import com.afollestad.materialdialogs.MaterialDialog
import com.android.volley.Cache
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.respondingio.battlegroundsbuddy.BuildConfig
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.Telemetry
import com.respondingio.battlegroundsbuddy.models.LogPlayerKill
import com.respondingio.battlegroundsbuddy.models.MatchParticipant
import com.respondingio.battlegroundsbuddy.models.MatchRoster
import com.respondingio.battlegroundsbuddy.snacky.Snacky
import kotlinx.android.synthetic.main.activity_match_detail.app_bar
import kotlinx.android.synthetic.main.activity_match_detail.match_detail_toolbar
import kotlinx.android.synthetic.main.activity_match_detail.match_loading_lottie
import kotlinx.android.synthetic.main.activity_match_detail.match_pg
import kotlinx.android.synthetic.main.activity_match_detail.toolbar_title
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.HashMap
import java.util.TimeZone


class MatchDetailActivity : AppCompatActivity() {

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

        //Crashlytics.setString("matchID", matchID)
        //Crashlytics.setString("shardID", regionID)

        mDrawer.addStickyFooterItem(SecondaryDrawerItem().withName(matchID).withEnabled(false).withSelectable(false))
        Log.d("MATCHID", matchID)

        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        //match_pg.visibility = View.VISIBLE
        match_loading_lottie?.visibility = View.VISIBLE
        match_loading_lottie?.playAnimation()

        loadMatchData(matchID, regionID)
    }

    private fun loadMatchData(matchID: String?, regionID: String?) {
        val url = "https://api.pubg.com/shards/$regionID/matches/$matchID"

        val jsonArrayRequest = object : JsonObjectRequest(Request.Method.GET, url, null, Response.Listener {

            parseMatchData(it)
        }, Response.ErrorListener {
            Log.d("MATCHREQ", it.toString())
        }) {
            override fun parseNetworkResponse(response: NetworkResponse): Response<JSONObject> {
                try {
                    var cacheEntry: Cache.Entry? = HttpHeaderParser.parseCacheHeaders(response)
                    if (cacheEntry == null) {
                        cacheEntry = Cache.Entry()
                    }
                    val cacheHitButRefreshed = (73 * 60 * 1000).toLong() // in 3 minutes cache will be hit, but also refreshed on background
                    val cacheExpired = (72 * 60 * 60 * 1000).toLong() // in 24 hours this cache entry expires completely
                    val now = System.currentTimeMillis()
                    val softExpire = now + cacheHitButRefreshed
                    val ttl = now + cacheExpired
                    cacheEntry.data = response.data
                    cacheEntry.softTtl = softExpire
                    cacheEntry.ttl = ttl
                    var headerValue: String? = response.headers["Date"]
                    if (headerValue != null) {
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue)
                    }
                    headerValue = response.headers["Last-Modified"]
                    if (headerValue != null) {
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue)
                    }
                    cacheEntry.responseHeaders = response.headers
                    val jsonString = String(response.data) +
                            HttpHeaderParser.parseCharset(response.headers)
                    return Response.success(JSONObject(jsonString), cacheEntry)
                } catch (e: UnsupportedEncodingException) {
                    return Response.error(ParseError(e))
                } catch (e: JSONException) {
                    return Response.error(ParseError(e))
                }

            }

            override fun deliverError(error: VolleyError) {
                super.deliverError(error)
            }

            override fun parseNetworkError(volleyError: VolleyError): VolleyError {
                if (volleyError.networkResponse == null || volleyError.networkResponse.statusCode == 404) {
                    runOnUiThread {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        match_pg?.visibility = View.GONE
                        MaterialDialog(this@MatchDetailActivity)
                                .title(text = "Error Getting Match Data")
                                .cancelOnTouchOutside(false)
                                .cancelable(false)
                                .message(text = "Either you are offline or the match you are trying to view is older than 15 days, we cannot pull data for matches older than 15 days.")
                                .positiveButton(text = "OKAY")
                                .show()
                    }
                } else {
                    finish()
                }
                return super.parseNetworkError(volleyError)
            }

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Accept"] = "application/vnd.api+json"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }


        requestQueue?.add(jsonArrayRequest)
    }

    var participantList = ArrayList<MatchParticipant>()
    var participantHash = HashMap<String, MatchParticipant>()
    var rosterList = ArrayList<MatchRoster>()

    var currentPlayerRoster: MatchRoster? = null

    var matchData: JSONObject? = null

    private fun parseMatchData(json: JSONObject?) {

        participantList.clear()
        rosterList.clear()

        matchData = json

        headerMapIV?.setImageDrawable(resources.getDrawable(getMapIcon(json?.getJSONObject("data")?.getJSONObject("attributes")?.getString("mapName").toString())))
        headerGamemodeTV?.text = Telemetry().gameModes[json?.getJSONObject("data")?.getJSONObject("attributes")?.getString("gameMode")].toString()
        headerTimeTV?.text = getFormattedCreatedAt(json?.getJSONObject("data")?.getJSONObject("attributes")?.getString("createdAt").toString())
        headerDurationTV?.text = DateUtils.formatElapsedTime(json?.getJSONObject("data")?.getJSONObject("attributes")?.getLong("duration")!!)
        headerRegionTV?.text = Telemetry().region[json.getJSONObject("data")?.getJSONObject("attributes")?.getString("shardId")].toString()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            headerMapIV?.tooltipText = getMapName(json.getJSONObject("data")?.getJSONObject("attributes")?.getString("mapName").toString())
        }

        if (json.getJSONObject("data")?.getJSONObject("attributes")?.getString("gameMode")?.contains("solo", true)!!) {
            //Game is a solo match, remove teams drawer item
            mDrawer.removeItems(11, 12)
        }

        for (i in 0 until json.getJSONArray("included")?.length()!!) {
            val includedObject = json.getJSONArray("included").getJSONObject(i)

            if (includedObject.getString("type") == "asset") {
                loadTelemetryData(includedObject.getJSONObject("attributes").getString("URL"))
            } else if (includedObject.getString("type") == "participant") {
                val participant = Gson().fromJson(includedObject.toString(), MatchParticipant::class.java)
                participantList.add(participant)
                participantHash.put(includedObject.getString("id"), participant)

                if (participant.attributes.stats.playerId == currentPlayerID) {
                    currentPlayerAccountID = participant.id
                }
            } else if (includedObject.getString("type") == "roster") {
                val roster: MatchRoster = Gson().fromJson(includedObject.toString(), MatchRoster::class.java)
                rosterList.add(roster)
            }
        }

        for (roster in rosterList) {
            for (rosterItem in roster.relationships.participants.data) {
                if (rosterItem.id == currentPlayerAccountID) {
                    currentPlayerRoster = roster
                    Log.d("ROSTER", rosterItem.id)
                }
            }
        }

        mDrawer.updateBadge(10, StringHolder("${participantList.size}"))
        mDrawer.updateBadge(11, StringHolder("${json.getJSONObject("data").getJSONObject("relationships").getJSONObject("rosters").getJSONArray("data").length()}"))
    }

    private fun loadTelemetryData(url: String?) {
        Log.d("TELEMETRY", url)

        val jsonArrayRequest = object : JsonArrayRequest(Request.Method.GET, url, null, Response.Listener {
            parseTelemetry(it)
        }, Response.ErrorListener {
            Log.d("MATCHREQ", it.toString())
        }) {
            override fun parseNetworkResponse(response: NetworkResponse): Response<JSONArray> {
                try {
                    var cacheEntry: Cache.Entry? = HttpHeaderParser.parseCacheHeaders(response)
                    if (cacheEntry == null) {
                        cacheEntry = Cache.Entry()
                    }
                    val cacheHitButRefreshed = (73 * 60 * 1000).toLong() // in 3 minutes cache will be hit, but also refreshed on background
                    val cacheExpired = (72 * 60 * 60 * 1000).toLong() // in 24 hours this cache entry expires completely
                    val now = System.currentTimeMillis()
                    val softExpire = now + cacheHitButRefreshed
                    val ttl = now + cacheExpired
                    cacheEntry.data = response.data
                    cacheEntry.softTtl = softExpire
                    cacheEntry.ttl = ttl
                    var headerValue: String? = response.headers["Date"]
                    if (headerValue != null) {
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue)
                    }
                    headerValue = response.headers["Last-Modified"]
                    if (headerValue != null) {
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue)
                    }
                    cacheEntry.responseHeaders = response.headers
                    val jsonString = String(response.data) +
                            HttpHeaderParser.parseCharset(response.headers)
                    return Response.success(JSONArray(jsonString), cacheEntry)
                } catch (e: UnsupportedEncodingException) {
                    return Response.error(ParseError(e))
                } catch (e: JSONException) {
                    return Response.error(ParseError(e))
                }

            }

            override fun deliverError(error: VolleyError) {
                super.deliverError(error)
            }

            override fun parseNetworkError(volleyError: VolleyError): VolleyError {
                runOnUiThread {
                    match_pg?.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    MaterialDialog(this@MatchDetailActivity)
                            .title(text = "Error Getting Match Data")
                            .cancelOnTouchOutside(false)
                            .cancelable(false)
                            .message(text = "Either you are offline or the match you are trying to view is older than 15 days, we cannot pull data for matches older than 15 days.")
                            .positiveButton(text = "OKAY")
                            .show()
                }
                return super.parseNetworkError(volleyError)
            }

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Accept"] = "application/vnd.api+json"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }
        requestQueue?.add(jsonArrayRequest)
    }

    private fun setupDrawer() {
        mDrawer = drawer {
            toolbar = match_detail_toolbar
            headerViewRes = R.layout.drawer_header
            headerDivider = false
            stickyFooterShadow = false
            selectedItem = 0
            primaryItem("Your Match Stats") {
                icon = R.drawable.icons8_person_male
                onClick { _, _, _ ->
                    val bundle = Bundle()
                    bundle.putString("playerID", currentPlayerID)
                    bundle.putSerializable("player", participantHash[currentPlayerAccountID])
                    bundle.putSerializable("killList", killFeedList)
                    bundle.putString("matchCreatedAt", matchData?.getJSONObject("data")?.getJSONObject("attributes")?.getString("createdAt"))
                    val matchesPlayerStatsFragment = MatchPlayerStatsFragment()
                    matchesPlayerStatsFragment.arguments = bundle
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
                    val bundle = Bundle()
                    bundle.putString("playerID", currentPlayerID)
                    bundle.putSerializable("player", participantHash[currentPlayerAccountID])
                    bundle.putSerializable("killList", killFeedList)
                    bundle.putString("matchCreatedAt", matchData?.getJSONObject("data")?.getJSONObject("attributes")?.getString("createdAt"))
                    val matchesPlayerStatsFragment = MatchYourTeamsStatsFragment()
                    matchesPlayerStatsFragment.arguments = bundle
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

        if (BuildConfig.DEBUG) {
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
        }
    }



    private fun setupToolbar() {
        setSupportActionBar(match_detail_toolbar)
        toolbar_title.text = "Your Match Stats"
    }

    private fun updateToolbarElevation(int: Float) {
        ViewCompat.setElevation(app_bar, int)
    }

    private fun updateToolbarFlags(canShowOnScroll: Boolean) {
        var params = match_detail_toolbar.layoutParams as AppBarLayout.LayoutParams
        if (canShowOnScroll) {
            params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        } else {
            params.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
        }
        app_bar.requestLayout()
    }

    class EmptyFragment: Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.match_roster_card, container, false)
        }
    }

    var telemetryJson: JSONArray? = null

    private fun parseTelemetry(json: JSONArray) {
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
    }

    var killFeedList = ArrayList<LogPlayerKill>()

    fun getMapIcon(mapName: String): Int {
        if (mapName == null) return -1
        when (mapName) {
            "Savage_Main" -> return R.drawable.sanhok_icon
            "Erangel_Main" -> return R.drawable.erangel_icon
            "Desert_Main" -> return R.drawable.cactu
            else -> return R.drawable.erangel_icon
        }
    }

    fun getMapName(mapName: String): String {
        if (mapName == null) return ""
        when (mapName) {
            "Savage_Main" -> return "Sanhok"
            "Erangel_Main" -> return "Erangel"
            "Desert_Main" -> return "Miramar"
            else -> return ""
        }
    }

    fun getMapAsset(): String {
        val mapName: String = matchData?.getJSONObject("data")?.getJSONObject("attributes")?.getString("mapName").toString()
        return when (mapName) {
            "Savage_Main" -> "sanhok/Savage_Main_Low_Res.jpg"
            "Erangel_Main" -> "erangel/Erangel_Main.jpg"
            "Desert_Main" -> "miramar/Miramar_Main_High_Res.jpg"
            else -> ""
        }
    }

    fun getFormattedCreatedAt(timeString: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        var time: Long = 0
        try {
            time = sdf.parse(timeString).time
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val offset = TimeZone.getDefault().rawOffset + TimeZone.getDefault().dstSavings
        val now = System.currentTimeMillis()

        val ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS)

        return ago as String
    }
}
