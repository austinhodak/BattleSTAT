package com.austinh.battlebuddy.viewmodels

import android.app.Application
import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.*
import com.android.volley.toolbox.*
import com.austinh.battlebuddy.models.*
import com.austinh.battlebuddy.utils.Regions
import com.austinh.battlebuddy.viewmodels.models.MatchData
import com.austinh.battlebuddy.viewmodels.models.MatchModel
import com.google.gson.Gson
import org.jetbrains.anko.doAsync
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.GZIPInputStream

class MatchDetailViewModel : ViewModel() {
    val mMatchData = MutableLiveData<MatchModel>()

    fun getMatchData(application: Application, shardID: String, matchID: String, playerID: String) {
        val mVolleyQueue = Volley.newRequestQueue(application)
        val url = "https://api.pubg.com/shards/${Regions.getNewRegionID(shardID)}/matches/$matchID"

        val matchModel = MatchModel(currentPlayerID = playerID)

        val objectRequest = object : JsonObjectRequest(Request.Method.GET, url, null, Response.Listener {
            parseMatchData(it, matchModel, mVolleyQueue)
        }, Response.ErrorListener {

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

            override fun parseNetworkError(volleyError: VolleyError): VolleyError {
                if (volleyError.networkResponse == null || volleyError.networkResponse.statusCode == 404) {
                    matchModel.error = volleyError.message
                    try {
                        mMatchData.postValue(matchModel)
                    } catch (e: Exception) {
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

        mVolleyQueue.add(objectRequest)
    }

    private fun parseMatchData(json: JSONObject, matchModel: MatchModel, mVolleyQueue: RequestQueue) {
        matchModel.attributes = Gson().fromJson(json.getJSONObject("data")?.getJSONObject("attributes").toString(), MatchData::class.java)

        var assetURL = ""

        for (i in 0 until json.getJSONArray("included")?.length()!!) {
            val includedObject = json.getJSONArray("included").getJSONObject(i)

            if (includedObject.getString("type") == "asset") {
                assetURL = includedObject.getJSONObject("attributes").getString("URL")
            } else if (includedObject.getString("type") == "participant") {
                val participant = Gson().fromJson(includedObject.toString(), MatchParticipant::class.java)
                matchModel.participantList.add(participant)
                matchModel.participantHash[includedObject.getString("id")] = participant

                if (participant.attributes.stats.playerId == matchModel.currentPlayerID) {
                    matchModel.currentPlayer = participant
                    matchModel.currentPlayerMatchID = participant.id
                }
            } else if (includedObject.getString("type") == "roster") {
                val roster: MatchRoster = Gson().fromJson(includedObject.toString(), MatchRoster::class.java)
                matchModel.rosterList.add(roster)
            }
        }

        for (roster in matchModel.rosterList) {
            for (rosterItem in roster.relationships.participants.data) {
                if (rosterItem.id == matchModel.currentPlayerMatchID) {
                    matchModel.currentPlayerRoster = roster
                }
            }
        }

        matchModel.randomizeTeamColors()

        getTelemetryDataGZIP(mVolleyQueue, matchModel, assetURL)
    }

    private fun getTelemetryDataGZIP(mVolleyQueue: RequestQueue, matchModel: MatchModel, assetURL: String) {
        val objectRequest = object : StringRequest(Request.Method.GET, assetURL, Response.Listener<String> {
            parseTelemetryData(JSONArray(it), matchModel)
        }, Response.ErrorListener {

        }) {
            override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                doAsync {

                }
                val output = StringBuilder()
                try {
                    val gStream = GZIPInputStream(ByteArrayInputStream(response.data))
                    val reader = InputStreamReader(gStream)
                    val inp = BufferedReader(reader, 16384)

                    reader.forEachLine {
                        output.append(it).append("\n")
                    }

                    reader.close()
                    inp.close()
                    gStream.close()
                } catch (e: IOException) {
                    return Response.error(ParseError())
                }

                return Response.success(output.toString(), HttpHeaderParser.parseCacheHeaders(response))
            }

            override fun parseNetworkError(volleyError: VolleyError): VolleyError {
                if (volleyError.networkResponse == null || volleyError.networkResponse.statusCode == 404) {
                    matchModel.error = volleyError.message
                    mMatchData.value = matchModel
                }

                return super.parseNetworkError(volleyError)
            }

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Accept-Encoding"] = "gzip"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

        mVolleyQueue.add(objectRequest)

    }

    private fun getTelemetryData(mVolleyQueue: RequestQueue, matchModel: MatchModel, assetURL: String) {
        val objectRequest = object : JsonArrayRequest(Request.Method.GET, assetURL, null, Response.Listener {
            parseTelemetryData(it, matchModel)
        }, Response.ErrorListener {

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

            override fun parseNetworkError(volleyError: VolleyError): VolleyError {
                if (volleyError.networkResponse == null || volleyError.networkResponse.statusCode == 404) {
                    matchModel.error = volleyError.message
                    mMatchData.value = matchModel
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

        mVolleyQueue.add(objectRequest)
    }

    private fun parseTelemetryData(json: JSONArray, matchModel: MatchModel) {
        doAsync {
            for (i in 0 until json.length()) {
                var item = json.getJSONObject(i)

                when (item["_T"]) {
                    "LogPlayerKill" -> {
                        matchModel.killFeedList.add(Gson().fromJson(item.toString(), LogPlayerKill::class.java))
                    }
                    "LogItemPickup" -> {
                        matchModel.logItemPickup.add(Gson().fromJson(item.toString(), LogItemPickup::class.java))
                    }
                    "LogPlayerTakeDamage" -> {
                        var item2 = Gson().fromJson(item.toString(), LogPlayerTakeDamage::class.java)
                        if (item.isNull("attacker")) {
                            //Log.d("TELEMETRY", "ATTACK NULL")
                            item2.attacker = LogCharacter()
                        }
                        matchModel.logPlayerTakeDamage.add(item2)
                    }
                    "LogPlayerAttack" -> {
                        matchModel.logPlayerAttack.add(Gson().fromJson(item.toString(), LogPlayerAttack::class.java))
                    }
                    "LogMatchDefinition" -> {
                        matchModel.matchDefinition = Gson().fromJson(item.toString(), LogMatchDefinition::class.java)
                    }
                    "LogPlayerCreate" -> {
                        matchModel.logPlayerCreate.add(Gson().fromJson(item.toString(), LogPlayerCreate::class.java).character)
                    }
                    "LogCarePackageSpawn" -> {
                        //matchModel.carePackageList.add(Gson().fromJson(item.toString(), LogCarePackageSpawn::class.java))
                    }
                    "LogCarePackageLand" -> {
                        matchModel.carePackageList.add(Gson().fromJson(item.toString(), LogCarePackageLand::class.java).doElapsedTime(matchModel.attributes!!.createdAt))
                    }
                    "LogPlayerPosition" -> {
                        val logPosition = Gson().fromJson(item.toString(), LogPlayerPosition::class.java)
                        matchModel.logPlayerPositions.add(logPosition)
                    }
                    "LogGameStatePeriodic" -> {
                        val periodic = Gson().fromJson(item.toString(), LogGamestatePeriodic::class.java)
                        periodic.matchTime = matchModel.attributes!!.createdAt

                        //val gameState = periodic.gameState

                        //Do date.
                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                        sdf.timeZone = TimeZone.getTimeZone("GMT")

                        val sdf2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                        sdf2.timeZone = TimeZone.getTimeZone("GMT")

                        val matchStartDate = sdf.parse(matchModel.attributes?.createdAt)
                        val killTime = sdf2.parse(periodic._D)

                        val difference = killTime.time - matchStartDate.time

                        /*val circle = SafeZoneCircle (
                                position = gameState.poisonGasWarningPosition,
                                radius = gameState.poisonGasWarningRadius,
                                timeInMatch = difference / 1000
                        )

                        if (!matchModel.safeZoneList.contains(circle) && circle.position.isValidCirclePosition()) {
                            matchModel.safeZoneList.add(circle)
                            Log.d("CIRCLE", circle.toString())
                        }

                        if (gameState.redZonePosition.isValidCirclePosition()) {
                            val redZone = SafeZoneCircle(
                                    position = gameState.redZonePosition,
                                    radius = gameState.redZoneRadius,
                                    timeInMatch = difference / 1000
                            )

                            if (!matchModel.redZoneList.contains(redZone)) {
                                matchModel.redZoneList.add(redZone)
                            }
                        }*/

                       // periodic.gameState.elapsedTime = difference

                        matchModel.gameStates.add(periodic)
                    }
                }
            }

            matchModel.killFeedList.sortedWith(compareBy { it._D })

            mMatchData.postValue(matchModel)
        }
    }

    private fun getRandomColor(): Int {
        val rnd = Random()
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }
}