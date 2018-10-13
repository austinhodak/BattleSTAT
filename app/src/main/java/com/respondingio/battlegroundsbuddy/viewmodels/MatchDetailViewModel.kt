package com.respondingio.battlegroundsbuddy.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Cache
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.respondingio.battlegroundsbuddy.models.LogItemPickup
import com.respondingio.battlegroundsbuddy.models.LogPlayerKill
import com.respondingio.battlegroundsbuddy.models.MatchParticipant
import com.respondingio.battlegroundsbuddy.models.MatchRoster
import com.respondingio.battlegroundsbuddy.viewmodels.models.MatchData
import com.respondingio.battlegroundsbuddy.viewmodels.models.MatchModel
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.util.HashMap

class MatchDetailViewModel : ViewModel() {
    val mMatchData = MutableLiveData<MatchModel>()

    fun getMatchData(application: Application, shardID: String, matchID: String, playerID: String) {
        val mVolleyQueue = Volley.newRequestQueue(application)
        val url = "https://api.pubg.com/shards/$shardID/matches/$matchID"

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

            override fun deliverError(error: VolleyError) {
                super.deliverError(error)
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

        getTelemetryData(mVolleyQueue, matchModel, assetURL)
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

            override fun deliverError(error: VolleyError) {
                super.deliverError(error)
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
        for (i in 0 until json.length()) {
            var item = json.getJSONObject(i)

            when (item["_T"]) {
                "LogPlayerKill" -> {
                    matchModel.killFeedList.add(Gson().fromJson(item.toString(), LogPlayerKill::class.java))
                }
                "LogItemPickup" -> {
                    matchModel.logItemPickup.add(Gson().fromJson(item.toString(), LogItemPickup::class.java))
                }
            }
        }

        matchModel.killFeedList.sortedWith(compareBy { it._D })

        mMatchData.postValue(matchModel)
    }
}