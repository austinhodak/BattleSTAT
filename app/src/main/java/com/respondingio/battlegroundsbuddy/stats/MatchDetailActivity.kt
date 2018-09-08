package com.respondingio.battlegroundsbuddy.stats

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badge
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.divider
import com.afollestad.materialdialogs.MaterialDialog
import com.android.volley.*
import com.android.volley.toolbox.*
import com.google.gson.Gson
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.Telemetry
import com.respondingio.battlegroundsbuddy.models.LogPlayerKill
import com.respondingio.battlegroundsbuddy.models.MatchParticipant
import de.mateware.snacky.Snacky
import kotlinx.android.synthetic.main.activity_match_detail.*
import kotlinx.android.synthetic.main.fragment_stats_kill_feed.*
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.SlimInjector
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class MatchDetailActivity : AppCompatActivity() {

    private lateinit var mDrawer: Drawer

    private var headerGamemodeTV: TextView? = null
    private var headerDurationTV: TextView? = null
    private var headerTimeTV: TextView? = null
    private var headerRegionTV: TextView? = null
    private var headerMapIV: ImageView? = null
    private var currentPlayerID: String? = null

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

        val matchID = intent.getStringExtra("matchID")
        val regionID = intent.getStringExtra("regionID")
        currentPlayerID =  "account.${intent.getStringExtra("playerID")}"

        mDrawer.addStickyFooterItem(SecondaryDrawerItem().withName(matchID).withEnabled(false).withSelectable(false))
        Log.d("MATCHID", matchID)

        val dialog = MaterialDialog.Builder(this)
                .content("Getting Match Data")
                .progress(false, 100, false)
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .show()

        loadMatchData(matchID, regionID, dialog)
    }

    private fun loadMatchData(matchID: String?, regionID: String?, dialog: MaterialDialog?) {
        val url = "https://api.pubg.com/shards/$regionID/matches/$matchID"

        val jsonArrayRequest = object : JsonObjectRequest(Request.Method.GET, url, null, Response.Listener {
            if (dialog?.currentProgress == 0) {
                dialog.setContent("Parsing Match Data")
                dialog.setProgress(25)
            }

            parseMatchData(it, dialog)
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
                if (volleyError.networkResponse.statusCode == 404) {
                    runOnUiThread {
                        dialog?.dismiss()
                        MaterialDialog.Builder(this@MatchDetailActivity)
                                .title("Match Data Not Available")
                                .canceledOnTouchOutside(false)
                                .cancelable(false)
                                .content("Most likely the match you are trying to view is older than 15 days old, we cannot pull data for matches older than 15 days.")
                                .positiveText("OKAY")
                                .onPositive { _, _ ->
                                    finish()
                                }
                                .show()
                    }
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

    private var participantList = ArrayList<MatchParticipant>()

    private var matchData: JSONObject? = null

    private fun parseMatchData(json: JSONObject?, dialog: MaterialDialog?) {

        participantList.clear()
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
                loadTelemetryData(includedObject.getJSONObject("attributes").getString("URL"), dialog)
            } else if (includedObject.getString("type") == "participant") {
                participantList.add(Gson().fromJson(includedObject.toString(), MatchParticipant::class.java))
            }
        }

        mDrawer.updateBadge(10, StringHolder("${participantList.size}"))
        mDrawer.updateBadge(11, StringHolder("${json.getJSONObject("data").getJSONObject("relationships").getJSONObject("rosters").getJSONArray("data").length()}"))

        dialog?.setContent("Getting Telemetry Data")
        dialog?.setProgress(50)
    }

    private fun loadTelemetryData(url: String?, dialog: MaterialDialog?) {
        Log.d("TELEMETRY", url)

        val jsonArrayRequest = object : JsonArrayRequest(Request.Method.GET, url, null, Response.Listener {
            if (dialog?.currentProgress == 50) {
                dialog.setContent("Parsing Telemetry Data")
                dialog.setProgress(75)
            }

            parseTelemetry(it, dialog)

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

            primaryItem("Your Match Stats") {
                icon = R.drawable.icons8_person_male
                onClick { _, _, _ ->
                    supportFragmentManager.beginTransaction().replace(R.id.match_frame, EmptyFragment())
                            .commit()
                    false
                }
            }
            primaryItem("Your Team's Stats") {
                icon = R.drawable.icons8_group
                identifier = 12
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
            }
            divider {}
            primaryItem("Kill Feed") {
                icon = R.drawable.icons8_horror_96
                onClick { view, position, drawerItem ->
                    supportFragmentManager.beginTransaction().replace(R.id.match_frame, KillFeedFragment())
                            .commit()
                    false
                }
            }
        }

        headerGamemodeTV = mDrawer.header.findViewById(R.id.header_gamemode)
        headerDurationTV = mDrawer.header.findViewById(R.id.header_duration)
        headerTimeTV = mDrawer.header.findViewById(R.id.header_time)
        headerRegionTV = mDrawer.header.findViewById(R.id.header_region)
        headerMapIV = mDrawer.header.findViewById(R.id.header_icon)
    }



    private fun setupToolbar() {
        setSupportActionBar(match_detail_toolbar)
        title = "Match Details"
    }

    class EmptyFragment: Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.match_roster_card, container, false)
        }
    }

    private fun parseTelemetry(json: JSONArray, dialog: MaterialDialog?) {
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

        dialog?.dismiss()
    }

    private var killFeedList = ArrayList<LogPlayerKill>()

    class KillFeedFragment: Fragment() {

        private var killFeedList: ArrayList<LogPlayerKill>? = null

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.fragment_stats_kill_feed, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            var activity: MatchDetailActivity = activity as MatchDetailActivity
            killFeedList = activity.killFeedList

            kill_feed_rv.layoutManager = LinearLayoutManager(activity)

            var adapter: SlimAdapter = SlimAdapter.create().attachTo(kill_feed_rv).register(R.layout.stats_kill_feed_item, SlimInjector<LogPlayerKill> { data, injector ->
                injector.text(R.id.kill_feed_killer, "")
                injector.text(R.id.kill_feed_victim, "")

                if (data.killer.name.isEmpty()) {
                    data.killer.name = Telemetry().damageTypeCategory[data.damageTypeCategory].toString()
                    injector.typeface(R.id.kill_feed_killer, injector.findViewById<TextView>(R.id.kill_feed_killer).typeface, Typeface.ITALIC)
                }

                injector.text(R.id.kill_feed_killer, data.killer.name.trim())
                injector.text(R.id.kill_feed_victim, data.victim.name.trim())

                if (Telemetry().damageCauserName[data.damageCauserName].toString() == "Player") {
                    injector.text(R.id.kill_feed_cause, Telemetry().damageTypeCategory[data.damageTypeCategory].toString())
                } else {
                    injector.text(R.id.kill_feed_cause, Telemetry().damageCauserName[data.damageCauserName].toString())
                }

                injector.text(R.id.textView9, (killFeedList!!.size - killFeedList!!.indexOf(data)).toString())

                Log.d("MATCH", "${activity.currentPlayerID} - ${data.killer.accountId}")

                if (data.killer.accountId == activity.currentPlayerID) {
                    injector.background(R.id.textView9, R.drawable.chip_green_outline)
                } else if (data.victim.accountId == activity.currentPlayerID) {
                    injector.background(R.id.textView9, R.drawable.chip_red_outline)
                } else {
                    injector.background(R.id.textView9, R.drawable.chip_grey_outline)
                }

                //Do date.
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
                val sdf2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                val matchStartDate = sdf.parse(activity.matchData?.getJSONObject("data")?.getJSONObject("attributes")?.getString("createdAt"))
                val killTime = sdf2.parse(data._D)

                var difference = killTime.time - matchStartDate.time

                val secondsInMilli: Long = 1000
                val minutesInMilli = secondsInMilli * 60
                val hoursInMilli = minutesInMilli * 60
                val daysInMilli = hoursInMilli * 24

                val elapsedDays = difference / daysInMilli
                difference = difference % daysInMilli

                val elapsedHours = difference / hoursInMilli
                difference = difference % hoursInMilli

                val elapsedMinutes = difference / minutesInMilli
                difference = difference % minutesInMilli

                val elapsedSeconds = difference / secondsInMilli

                injector.text(R.id.kill_feed_time, String.format("%02d:%02d", elapsedMinutes, elapsedSeconds))

            }).updateData(killFeedList)
        }
    }

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

    fun getFormattedCreatedAt(timeString: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
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
