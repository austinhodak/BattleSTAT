package com.respondingio.battlegroundsbuddy.viewmodels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.database.*
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.respondingio.battlegroundsbuddy.models.PlayerStats
import com.respondingio.battlegroundsbuddy.utils.Regions
import com.respondingio.battlegroundsbuddy.viewmodels.models.LeaderboardModel
import com.respondingio.battlegroundsbuddy.viewmodels.models.LeaderboardPlayer
import com.respondingio.battlegroundsbuddy.viewmodels.models.PlayerModel
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.util.*

class PlayerStatsViewModel : ViewModel() {
    val playerData = MutableLiveData<PlayerModel>()
    val leaderboardData = MutableLiveData<LeaderboardModel>()

    var playerListener: ValueEventListener? = null
    var playerRef: DatabaseReference? = null

    fun getPlayerStats(playerID: String, regionID: String, seasonID: String) {
        playerRef = FirebaseDatabase.getInstance().getReference("user_stats/$playerID/season_data/${regionID.toLowerCase()}/${seasonID.toLowerCase()}")
        playerListener = playerRef?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val playerStats = PlayerModel()
                if (!snapshot.exists()) {
                    playerStats.error = 1
                    playerData.value = playerStats
                    return
                }

                playerStats.lastUpdated = snapshot.child("lastUpdated").value as Long

                playerStats.soloStats = snapshot.child("stats/solo").getValue(PlayerStats::class.java)
                playerStats.soloFPPStats = snapshot.child("stats/solo-fpp").getValue(PlayerStats::class.java)
                playerStats.duoStats = snapshot.child("stats/duo").getValue(PlayerStats::class.java)
                playerStats.duoFPPStats = snapshot.child("stats/duo-fpp").getValue(PlayerStats::class.java)
                playerStats.squadStats = snapshot.child("stats/squad").getValue(PlayerStats::class.java)
                playerStats.squadFPPStats = snapshot.child("stats/squad-fpp").getValue(PlayerStats::class.java)

                playerData.value = playerStats
            }
        })
    }

    fun getLeaderboards(regionID: String, gameMode: String, application: Context) {
        val mVolleyQueue = Volley.newRequestQueue(application)
        val url = "https://api.pubg.com/shards/${Regions.getNewRegionID(regionID)}/leaderboards/${gameMode.toLowerCase()}"

        val leaderboardModel = LeaderboardModel(gameMode = gameMode)

        val objectRequest = object : JsonObjectRequest(Request.Method.GET, url, null, Response.Listener {
            for (i in 0 until it.getJSONArray("included").length()) {
                leaderboardModel.playerList.add(Gson().fromJson(it.getJSONArray("included")[i].toString(), LeaderboardPlayer::class.java))
            }

            leaderboardModel.playerList = leaderboardModel.playerList.sortedWith(compareBy { it.attributes.rank }).toMutableList()

            leaderboardData.postValue(leaderboardModel)
        }, Response.ErrorListener {

        }) {
            override fun parseNetworkResponse(response: NetworkResponse): Response<JSONObject> {
                try {
                    var cacheEntry: Cache.Entry? = HttpHeaderParser.parseCacheHeaders(response)
                    if (cacheEntry == null) {
                        cacheEntry = Cache.Entry()
                    }
                    val cacheHitButRefreshed = (15 * 60 * 1000).toLong() // in 3 minutes cache will be hit, but also refreshed on background
                    val cacheExpired = (15 * 60 * 1000).toLong() // in 24 hours this cache entry expires completely
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
                    //matchModel.error = volleyError.message
                    try {
                        //   mMatchData.postValue(matchModel)
                    } catch (e: Exception) {
                    }
                }
                return super.parseNetworkError(volleyError)
            }

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Accept"] = "application/vnd.api+json"
                params["Authorization"] = "Bearer ${FirebaseRemoteConfig.getInstance().getString("pubg_api_key")}"
                return params
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

        mVolleyQueue.add(objectRequest)
    }

    override fun onCleared() {
        super.onCleared()
        if (playerListener != null && playerRef != null)
            playerRef?.removeEventListener(playerListener!!)
    }
}