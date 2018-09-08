package com.respondingio.battlegroundsbuddy.stats


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.FirebaseFunctionsException.Code
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.models.PlayerStats
import de.mateware.snacky.Snacky
import kotlinx.android.synthetic.main.fragment_your_stats.*
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.textColor
import java.util.*

class NewStatsFragment : Fragment() {

    private var playerName: String? = null
    private var playerID: String? = null
    private var shardID: String? = null
    private var gameMode: String? = null
    private var seasonID: String? = null

    private var mainView: View? = null

    internal var mBundle = arguments

    private var mDatabase: DatabaseReference? = null

    private var mFunctions: FirebaseFunctions? = null

    private var isOutdated = false

    private var TAG = NewStatsFragment::class.java.simpleName

    private var lastUpdated: Long? = null

    private var updateTimeout: Long = 15

    private var isPremium: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mainView = inflater.inflate(R.layout.fragment_your_stats, container, false)
        //mainView?.visibility = View.INVISIBLE

        mDatabase = FirebaseDatabase.getInstance().reference
        mFunctions = FirebaseFunctions.getInstance()
        val mSharedPreferences = activity!!.getSharedPreferences("com.austinhodak.pubgcenter", Context.MODE_PRIVATE)

        isPremium = mSharedPreferences.getBoolean("isPremium", false)

        if (mSharedPreferences.getBoolean("isPremium", false)) {
            updateTimeout = 5
        }

        playerName = arguments!!.getString("player_name")
        playerID = arguments!!.getString("player_id")
        shardID = AddPlayerBottomSheet.regionList[arguments!!.getInt("region")].toLowerCase()
        gameMode = AddPlayerBottomSheet.modesList[arguments!!.getInt("gamemode")]
        seasonID = arguments!!.getString("season_id")


        if (playerID != null && shardID != null && seasonID != null)
            loadPlayerStats()

        Log.d("STATS", "$playerID - $shardID - $seasonID")

        val refreshLayout = mainView?.findViewById<SwipeRefreshLayout>(R.id.stats_refresh)

        refreshLayout?.setColorSchemeResources(R.color.md_orange_500, R.color.md_pink_500)
        refreshLayout?.onRefresh {
            if (playerID != null && shardID != null && gameMode != null && seasonID != null) {
                if (getTimeSinceLastUpdated() / 60 > updateTimeout) {
                    refreshLayout.isRefreshing = true
                    pullNewPlayerStats()
                } else {
                    refreshLayout.isRefreshing = false
                    Snacky.builder().setActivity(activity!!).setBackgroundColor(Color.parseColor("#3F51B5")).setText("You can refresh once every $updateTimeout minutes.").setActionClickListener {
                        pullNewPlayerStats()
                    }.setActionText("UPGRADE").setDuration(5000).setActionTextColor(Color.WHITE).build().show()
                }
            } else {
                Snacky.builder().setActivity(activity!!).warning().setText("Must Select Gamemode and Season before refreshing.").show()
            }
        }

        return mainView
    }

    private fun loadPlayerStats() {
        mDatabase?.child("user_stats/$playerID/season_data/$shardID/$seasonID")?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(data: DataSnapshot) {
                if (data.exists()) {
                    if (data.hasChild("stats/$gameMode")) {
                        updateStats(data.child("stats/$gameMode").getValue(PlayerStats::class.java)!!)

                        stats_refresh?.isRefreshing = false
                    }

                    lastUpdated = data.child("lastUpdated").value as Long?
                    val activity = activity as GameStatsActivity?

                    Log.d("LASTUPDATED", getTimeSinceLastUpdated().toString())

                    if (getTimeSinceLastUpdated() / 60 > updateTimeout) {
                        activity?.showOutdated()
                    } else {
                        activity?.hideOutdated()
                    }
                } else {
                    Log.w(TAG, "Stats not available in database, run function. - $playerID")
                    stats_refresh?.isRefreshing = true

                    pullNewPlayerStats()
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    @SuppressLint("SetTextI18n")
    private fun updateStats(playerStats: PlayerStats) {

        stats_assists?.text = playerStats.assists.toString()
        stats_boosts?.text = playerStats.boosts.toString()
        stats_dbnos?.text = playerStats.dBNOs.toString()
        stats_damageDealt?.text = Math.round(playerStats.damageDealt).toString()
        stats_headshots?.text = playerStats.headshotKills.toString()
        stats_heals?.text = playerStats.heals.toString()
        stats_killPoints?.text = String.format("%.0f", Math.rint(playerStats.killPoints))
        stats_kills?.text = playerStats.kills.toString()
        stats_longestKill?.text = "${String.format("%.0f", Math.rint(playerStats.longestKill))}m"
        stats_longestSurv?.text = "${String.format("%.0f", Math.ceil(playerStats.longestTimeSurvived / 60))} Min"
        stats_losses?.text = playerStats.losses.toString()
        stats_killStreak?.text = playerStats.maxKillStreaks.toString()
        stats_revives?.text = playerStats.revives.toString()
        stats_rideDist?.text = "${String.format("%.0f", Math.ceil(playerStats.rideDistance))}m"
        stats_roadKills?.text = playerStats.roadKills.toString()
        stats_mostkills?.text = playerStats.roundMostKills.toString()
        stats_roundsplayed?.text = playerStats.roundsPlayed.toString()
        stats_suicides?.text = playerStats.suicides.toString()
        stats_teamKills?.text = playerStats.teamKills.toString()
        stats_time?.text = "${String.format("%.0f", Math.ceil(playerStats.timeSurvived / 60))} Min"
        stats_top10s?.text = playerStats.top10s.toString()
        stats_vehicleDestroy?.text = playerStats.vehicleDestroys.toString()
        stats_walkDist?.text = "${String.format("%.0f", Math.ceil(playerStats.walkDistance))}m"
        stats_weaponsAqd?.text = playerStats.weaponsAcquired.toString()
        stats_weeklyPoints?.text = playerStats.weeklyKills.toString()
        stats_winPoints?.text = String.format("%.0f", Math.rint(playerStats.winPoints))
        stats_wins?.text = playerStats.wins.toString()
        stats_deaths?.text = playerStats.losses.toString()

        if (playerStats.kills > 0 && playerStats.losses > 0) {
            stats_kd?.text = String.format("%.2f", playerStats.kills.toDouble() / playerStats.losses.toDouble())
        } else {
            stats_kd?.text = "0"
        }

        if (playerStats.wins > 0 && playerStats.losses > 0) {
            stats_winloss?.text = String.format("%.2f", playerStats.wins.toDouble() / playerStats.losses.toDouble())
        } else {
            stats_winloss?.text = "0"
        }

        when {
            playerStats.kills.toDouble() / playerStats.losses.toDouble() >= 2.00 -> stats_kd?.textColor = resources.getColor(R.color.md_green_A700)
            playerStats.kills.toDouble() / playerStats.losses.toDouble() >= 1.00 -> stats_kd?.textColor = resources.getColor(R.color.md_orange_A400)
            stats_kd?.text != "0" -> stats_kd?.textColor = resources.getColor(R.color.md_red_A400)
        }

        mainView?.visibility = View.VISIBLE

    }

    fun getTimeSinceLastUpdated(): Long {
        return Math.abs(lastUpdated?.minus((System.currentTimeMillis() / 1000))!!)
    }

    private fun pullNewPlayerStats() {
        startPlayerStatsFunction(playerID.toString(), shardID.toString(), seasonID.toString())?.addOnCompleteListener {
            if (!it.isSuccessful) {
                val e = it.exception
                if (e is FirebaseFunctionsException) {
                    val code = e.code

                    Log.e("PullNewStats", "onComplete: " + code.toString())

                    if (code == Code.NOT_FOUND) {
                        Snacky.builder().setActivity(activity).info().setText("Player not found, try again.").setDuration(
                                Snacky.LENGTH_LONG).show()
                    } else if (code == Code.RESOURCE_EXHAUSTED) {
                        Snacky.builder().setActivity(activity).error().setText("API limit reached, try again in a minute.").setDuration(
                                Snacky.LENGTH_LONG).show()
                    } else {
                        Snacky.builder().setActivity(activity).error().setText("Unknown error.").setDuration(
                                Snacky.LENGTH_LONG).show()
                    }
                }

                stats_refresh.isRefreshing = false
                return@addOnCompleteListener
            }

            Log.d(TAG, "Successfully refreshed player stats.")
        }
    }

    private fun startPlayerStatsFunction(playerID: String, shardID: String, seasonID: String): Task<Map<String, Any>>? {
        val data = HashMap<String, Any>()
        data["playerID"] = playerID
        data["shardID"] = shardID
        data["seasonID"] = seasonID

        return mFunctions?.getHttpsCallable("loadPlayerStats")?.call(data)?.continueWith { task ->
            val result = task.result.data as Map<String, Any>
            Log.d("REQUEST", result.toString())
            result
        }
    }
}
