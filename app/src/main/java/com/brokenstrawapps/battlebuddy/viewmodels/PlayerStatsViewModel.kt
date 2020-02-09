package com.brokenstrawapps.battlebuddy.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.brokenstrawapps.battlebuddy.models.MatchTop
import com.brokenstrawapps.battlebuddy.models.PlayerListModel
import com.brokenstrawapps.battlebuddy.models.PlayerStats
import com.brokenstrawapps.battlebuddy.viewmodels.models.LeaderboardModel
import com.brokenstrawapps.battlebuddy.viewmodels.models.LeaderboardPlayer
import com.brokenstrawapps.battlebuddy.viewmodels.models.MasteryModel
import com.brokenstrawapps.battlebuddy.viewmodels.models.PlayerModel
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.util.*

class PlayerStatsViewModel : ViewModel() {
    val masteryData = MutableLiveData<MasteryModel>()

    val playerData = MutableLiveData<PlayerModel>()
    val leaderboardData = MutableLiveData<LeaderboardModel>()
    val matchesList = MutableLiveData<MutableList<MatchTop>>()

    var playerListener: ValueEventListener? = null
    var playerRef: DatabaseReference? = null

    var matchDocListeners = ArrayList<ListenerRegistration>()

    var masteryListener: ListenerRegistration? = null

    fun getPlayerStats(player: PlayerListModel) {
        //Remove old listeners so no duplicating
        if (playerListener != null && playerRef != null) playerRef?.removeEventListener(playerListener!!)

        Log.d("GETTING PLAYER", "user_stats/${player.playerID}/season_data/${player.getDatabaseSearchURL().toLowerCase()}/${player.selectedSeason.codeString.toLowerCase()}")
        playerRef = if (player.isLifetimeSelected) {
            FirebaseDatabase.getInstance().getReference("user_stats/${player.playerID}/season_data/${player.getDatabaseSearchURL().toLowerCase()}/lifetime")
        } else {
            FirebaseDatabase.getInstance().getReference("user_stats/${player.playerID}/season_data/${player.getDatabaseSearchURL().toLowerCase()}/${player.selectedSeason.codeString.toLowerCase()}")
        }
        playerListener = playerRef?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val playerStats = PlayerModel()
                if (!snapshot.exists()) {
                    playerStats.error = 1
                    playerStats.lastUpdated = 0.toLong()
                    playerData.value = playerStats
                    return
                }

                playerStats.lastUpdated = snapshot.child("lastUpdated").value.toString().toLongOrNull() ?: 0

                playerStats.soloStats = snapshot.child("stats/solo").getValue(PlayerStats::class.java) ?: PlayerStats()
                playerStats.soloFPPStats = snapshot.child("stats/solo-fpp").getValue(PlayerStats::class.java)?: PlayerStats()
                playerStats.duoStats = snapshot.child("stats/duo").getValue(PlayerStats::class.java)?: PlayerStats()
                playerStats.duoFPPStats = snapshot.child("stats/duo-fpp").getValue(PlayerStats::class.java)?: PlayerStats()
                playerStats.squadStats = snapshot.child("stats/squad").getValue(PlayerStats::class.java)?: PlayerStats()
                playerStats.squadFPPStats = snapshot.child("stats/squad-fpp").getValue(PlayerStats::class.java)?: PlayerStats()

                playerData.value = playerStats
            }
        })
    }

    fun getLeaderboards(player: PlayerListModel, application: Context) {
        val mVolleyQueue = Volley.newRequestQueue(application)
        val url = "https://api.pubg.com/shards/${player.getDatabaseSearchURL()}/leaderboards/division.bro.official.${player.selectedSeason.codeString}/${player.selectedGamemode.id}"
        Log.d("URL", url)

        val leaderboardModel = LeaderboardModel(gameMode = player.selectedGamemode)

        val objectRequest = object : JsonObjectRequest(Request.Method.GET, url, null, Response.Listener {
            if (!it.has("included")) {
                leaderboardData.postValue(leaderboardModel)
                return@Listener
            }
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

    fun getPlayerWeaponMastery(player: PlayerListModel) {
        masteryListener?.remove()

        val docRef = FirebaseFirestore.getInstance().collection("players").document(player.playerID)
        masteryListener = docRef.addSnapshotListener { snapshot, e ->
            var mastery = MasteryModel()
            if (e != null) {
                mastery.weaponMaster?.error = 2
                masteryData.value = mastery
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                mastery = snapshot.toObject(MasteryModel::class.java)!!
                Log.d("MASTERY", mastery.toString())
                Log.d("MASTERY", snapshot.toString())
                masteryData.value = mastery
            } else {
                //Call to get data.
                val data = HashMap<String, Any>()
                data["playerID"] = player.playerID
                data["platformID"] = player.platform.id

                FirebaseFunctions.getInstance().getHttpsCallable("getPlayerWeaponMasteryData").call(data).continueWith { task ->

                }.addOnSuccessListener {

                }
            }
        }
    }
}