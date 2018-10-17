package com.respondingio.battlegroundsbuddy.stats


import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.FirebaseFunctionsException.Code
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.models.PlayerStats
import com.respondingio.battlegroundsbuddy.models.PrefPlayer
import com.respondingio.battlegroundsbuddy.snacky.Snacky
import kotlinx.android.synthetic.main.activity_stats_main_new.mainStatsRefreshLayout
import kotlinx.android.synthetic.main.fragment_your_stats.*
import org.jetbrains.anko.textColor
import java.util.HashMap

class MainStatsFragment : Fragment() {


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

    private var TAG = MainStatsFragment::class.java.simpleName

    private var lastUpdated: Long? = null

    private var updateTimeout: Long = 15

    private var isPremium: Boolean = false

    lateinit var previousPlayer: PlayerStats

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mainView = inflater.inflate(R.layout.fragment_your_stats, container, false)
        //mainView?.visibility = View.INVISIBLE

        return mainView
    }

    private var mSharedPreferences: SharedPreferences? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDatabase = FirebaseDatabase.getInstance().reference
        mFunctions = FirebaseFunctions.getInstance()
        mSharedPreferences = activity!!.getSharedPreferences("com.respondingio.battlegroundsbuddy", Context.MODE_PRIVATE)

        isPremium = mSharedPreferences!!.getBoolean("premiumV1", false)

        if (mSharedPreferences!!.getBoolean("premiumV1", false)) {
            updateTimeout = 2
        }

        var player: PrefPlayer = arguments!!.getSerializable("player") as PrefPlayer

        playerName = player.playerName
        playerID = player.playerID
        shardID = player.selectedShardID
        gameMode = player.selectedGamemode
        seasonID = player.selectedSeason

        if (arguments!!.containsKey("previousPlayer")) {
            previousPlayer = arguments!!.getSerializable("previousPlayer") as PlayerStats
            //setPreviousStats(previousPlayer)
        }

        if (playerID != null && shardID != null && seasonID != null)
            loadPlayerStats()

        Log.d("STATS", "$playerID - $shardID - $seasonID")
    }

    override fun onStop() {
        super.onStop()
        if (statListener != null && statRef != null) {
            statRef!!.removeEventListener(statListener!!)
        }
    }

    private var statListener: ValueEventListener? = null

    private var statRef: DatabaseReference? = null

    private fun loadPlayerStats() {
        statRef = mDatabase?.child("user_stats/$playerID/season_data/$shardID/$seasonID")?.ref
        statListener = mDatabase?.child("user_stats/$playerID/season_data/$shardID/$seasonID")?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(data: DataSnapshot) {
                if (data.exists()) {
                    if (data.hasChild("stats/$gameMode")) {
                        updateStats(data.child("stats/$gameMode").getValue(PlayerStats::class.java)!!)
                    }

                    lastUpdated = data.child("lastUpdated").value as Long?

                    if (activity != null && activity!! is MainStatsActivity) {
                        try {
                            val topActivity = activity as MainStatsActivity
                            topActivity.lastUpdated = lastUpdated
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    val relTime = DateUtils
                            .getRelativeTimeSpanString(lastUpdated!! * 1000L,
                                    System.currentTimeMillis(),
                                    DateUtils.MINUTE_IN_MILLIS)

                    stats_updated?.text = "Updated: $relTime"

                    val activity = activity as MainStatsActivity?
                    activity?.mainStatsRefreshLayout?.isRefreshing = false

                    Log.d("LASTUPDATED", getTimeSinceLastUpdated().toString())

                    if (getTimeSinceLastUpdated() / 60 > updateTimeout) {
                        //activity?.showOutdated()
                    } else {
                        //activity?.hideOutdated(
                    }
                } else {
                    Log.w(TAG, "Stats not available in database, run function. - $playerID")
                    val activity = activity as MainStatsActivity?
                    activity?.mainStatsRefreshLayout?.isRefreshing = true

                    pullNewPlayerStats()
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    lateinit var currentPlayer: PlayerStats

    @SuppressLint("SetTextI18n")
    private fun updateStats(playerStats: PlayerStats) {
        currentPlayer = playerStats
        if (this::previousPlayer.isInitialized) {
            val animator = ValueAnimator.ofInt(previousPlayer.kills, playerStats.kills)
            animator.duration = 500
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.addUpdateListener {
                stats_kills?.text = it.animatedValue.toString()
            }
            animator.start()
        } else {
            stats_kills?.text = playerStats.kills.toString()
        }

        if (this::previousPlayer.isInitialized) {
            val animator = ValueAnimator.ofInt(previousPlayer.damageDealt.toInt(), playerStats.damageDealt.toInt())
            animator.duration = 500
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.addUpdateListener {
                stats_damageDealt?.text = it.animatedValue.toString()
            }
            animator.start()
        } else {
            stats_damageDealt?.text = playerStats.damageDealt.toInt().toString()
        }

        stats_assists?.text = playerStats.assists.toString()
        stats_boosts?.text = playerStats.boosts.toString()
        stats_dbnos?.text = playerStats.dBNOs.toString()
        stats_damageDealt?.text = Math.round(playerStats.damageDealt).toString()
        stats_headshots?.text = playerStats.headshotKills.toString()
        stats_heals?.text = playerStats.heals.toString()
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

        if (playerStats.killPoints == 0.0 && playerStats.winPoints == 0.0 && playerStats.rankPoints >= 0.0) {
            rank_card?.visibility = View.VISIBLE
            rank_title?.text = getRankTitle(playerStats.rankPoints)
            rank_subtitle?.text = "POINTS: ${Math.floor(playerStats.rankPoints).toInt()}"

            try {
                Glide.with(this).load(getRankIcon(playerStats.rankPoints)).into(rank_icon)
            } catch (e: Exception) {
            }

            old_points_layout?.visibility = View.GONE
            divider13?.visibility = View.INVISIBLE
            //stats_winPoints?.text = String.format("%.0f", Math.rint(playerStats.rankPoints))

        } else {
            rank_card?.visibility = View.GONE

            old_points_layout?.visibility = View.VISIBLE

            stats_killPoints?.text = String.format("%.0f", Math.rint(playerStats.killPoints))
            stats_winPoints?.text = String.format("%.0f", Math.rint(playerStats.winPoints))
        }

        stats_wins?.text = playerStats.wins.toString()
        stats_deaths?.text = playerStats.losses.toString()

        if (playerStats.kills > 0 && playerStats.losses > 0) {
            stats_kd?.text = String.format("%.2f", playerStats.kills.toDouble() / playerStats.losses.toDouble())
        } else {
            stats_kd?.text = "0"
        }

        //FIX WINS/ROUNDS PLAYED FOR W/L RATIO
        if (playerStats.wins > 0 && playerStats.roundsPlayed > 0) {
            stats_winloss?.text = String.format("%.2f", playerStats.wins.toDouble() / playerStats.roundsPlayed.toDouble())
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

    fun setPreviousStats(playerStats: PlayerStats) {
        stats_assists?.text = playerStats.assists.toString()
        stats_boosts?.text = playerStats.boosts.toString()
        stats_dbnos?.text = playerStats.dBNOs.toString()
        stats_damageDealt?.text = Math.round(playerStats.damageDealt).toString()
        stats_headshots?.text = playerStats.headshotKills.toString()
        stats_heals?.text = playerStats.heals.toString()
        stats_killPoints?.text = String.format("%.0f", Math.rint(playerStats.killPoints))
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
    }

    fun getTimeSinceLastUpdated(): Long {
        if (lastUpdated == null) return 0
        return Math.abs(lastUpdated?.minus((System.currentTimeMillis() / 1000))!!)
    }

    private fun pullNewPlayerStats() {
        startPlayerStatsFunction(playerID.toString(), shardID.toString(), seasonID.toString())?.addOnCompleteListener {
            if (!it.isSuccessful) {
                val e = it.exception
                if (e is FirebaseFunctionsException) {
                    val code = e.code

                    Log.e("PullNewStats", "onComplete: " + e.message)

                    if (activity != null) {
                        when (code) {
                            Code.NOT_FOUND -> Snacky.builder().setActivity(activity).info().setText(e.message.toString()).setDuration(
                                    Snacky.LENGTH_LONG).show()
                            Code.RESOURCE_EXHAUSTED -> Snacky.builder().setActivity(activity).error().setText("API limit reached, try again in a minute.").setDuration(
                                    Snacky.LENGTH_LONG).show()
                            else -> Snacky.builder().setActivity(activity).error().setText("Unknown error.").setDuration(
                                    Snacky.LENGTH_LONG).show()
                        }
                    }
                }
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
            val result = task.result?.data as Map<String, Any>
            Log.d("REQUEST", result.toString())
            result
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1002) {
            val responseCode = data?.getIntExtra("RESPONSE_CODE", 0)
            val purchaseData = data?.getStringExtra("INAPP_PURCHASE_DATA")
            val dataSignature = data?.getStringExtra("INAPP_DATA_SIGNATURE")

            if (resultCode == Activity.RESULT_OK) {
                val m2SharedPreferences = requireActivity().getSharedPreferences("com.austinhodak.pubgcenter", Context.MODE_PRIVATE)

                mSharedPreferences?.edit()?.putBoolean("premiumV1", true)?.apply()
                m2SharedPreferences.edit().putBoolean("removeAds", true).apply()
                Snacky.builder().setActivity(requireActivity()).info().setText("Thanks! Enjoy the new stuff!").show()
            }
        }
    }

    fun getRankTitle(rank: Double): String {
        var rankPoints = Math.floor(rank).toInt()

        return when {
            rankPoints == 0 -> "UNRANKED"
            rankPoints in 1..1399 -> "BRONZE"
            rankPoints in 1400..1499 -> "SILVER"
            rankPoints in 1500..1599 -> "GOLD"
            rankPoints in 1600..1699 -> "PLATINUM"
            rankPoints in 1700..1799 -> "DIAMOND"
            rankPoints in 1800..1899 -> "ELITE"
            rankPoints in 1900..1999 -> "MASTER"
            rankPoints >= 2000 -> "GRANDMASTER"
            else -> "RANK ERROR"
        }
    }

    fun getRankIcon(rank: Double): Int {
        var rankPoints = Math.floor(rank).toInt()

        return when {
            rankPoints == 0 -> R.drawable.rank_icon_unranked
            rankPoints in 1..1399 -> R.drawable.rank_icon_bronze
            rankPoints in 1400..1499 -> R.drawable.rank_icon_silver
            rankPoints in 1500..1599 -> R.drawable.rank_icon_gold
            rankPoints in 1600..1699 -> R.drawable.rank_icon_platinum
            rankPoints in 1700..1799 -> R.drawable.rank_icon_diamond
            rankPoints in 1800..1899 -> R.drawable.rank_icon_elite
            rankPoints in 1900..1999 -> R.drawable.rank_icon_master
            rankPoints >= 2000 -> R.drawable.rank_icon_grandmaster
            else -> R.drawable.rank_icon_unranked
        }
    }
}
