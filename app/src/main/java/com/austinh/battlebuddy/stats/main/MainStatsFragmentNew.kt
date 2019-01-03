package com.austinh.battlebuddy.stats.main


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.text.format.DateUtils
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.austinh.battlebuddy.R
import com.austinh.battlebuddy.models.PlayerListModel
import com.austinh.battlebuddy.models.PlayerStats
import com.austinh.battlebuddy.utils.Rank
import com.austinh.battlebuddy.utils.Ranks.getRankIcon
import com.austinh.battlebuddy.viewmodels.PlayerStatsViewModel
import com.austinh.battlebuddy.viewmodels.models.PlayerModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.android.synthetic.main.player_stats_main_new.*
import java.text.DecimalFormat
import kotlin.math.roundToInt

class MainStatsFragmentNew : Fragment() {

    private val viewModel: PlayerStatsViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(PlayerStatsViewModel::class.java)
    }

    var handler = Handler()

    private var mainView: View? = null

    val formatter = DecimalFormat("#,###")

    private var mDatabase: DatabaseReference? = null

    private var mFunctions: FirebaseFunctions? = null

    private var TAG = MainStatsFragmentNew::class.java.simpleName

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mainView = inflater.inflate(R.layout.player_stats_main_new, container, false)
        return mainView
    }

    private var mSharedPreferences: SharedPreferences? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDatabase = FirebaseDatabase.getInstance().reference
        mFunctions = FirebaseFunctions.getInstance()
        mSharedPreferences = activity!!.getSharedPreferences("com.austinh.battlebuddy", Context.MODE_PRIVATE)

        val player: PlayerListModel = arguments!!.getSerializable("selectedPlayer") as PlayerListModel

        viewModel.playerData.observe(this, Observer<PlayerModel> {
            handler.removeCallbacksAndMessages(null)
            updateStats(it.getStatsByGamemode(player.selectedGamemode)!!)
            statsKills.text = it.getStatsByGamemode(player.selectedGamemode)?.kills.toString()

            if (it.lastUpdated != null && it.lastUpdated != 0.toLong()) {
                val relTime = DateUtils
                        .getRelativeTimeSpanString(it.lastUpdated!! * 1000L,
                                System.currentTimeMillis(),
                                DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL).toString().toUpperCase().replace(".", "")


                statsLastUpdatedTop?.text = "LAST UPDATED: ${relTime}"

                last_updated_tv?.text = relTime

                Log.d("STATS", "MINUTES SINCE LAST UPDATE ${it.getMinutesSinceLastUpdated()}")

                if (it.getMinutesSinceLastUpdated() >= 60) {
                    (last_updated_tv?.parent as ConstraintLayout).setBackgroundResource(R.drawable.top_right_corner_box_red)
                    //stats_corner_alert?.setOutdated()
                } else {
                    (last_updated_tv?.parent as ConstraintLayout).setBackgroundResource(R.drawable.top_right_corner_box)
                    //stats_corner_alert?.hide()
                }
            } else {
                statsLastUpdatedTop?.text = "LAST UPDATED: NEVER, PULL TO REFRESH"

                last_updated_tv?.text = "NEVER"
                (last_updated_tv?.parent as ConstraintLayout).setBackgroundResource(R.drawable.top_right_corner_box_red)
            }
        })

        statsRankPoints?.setFactory {
            TextView(ContextThemeWrapper(requireActivity(), R.style.PointsText), null, 0)
        }

        val inAnim = AnimationUtils.loadAnimation(requireContext(),
                android.R.anim.fade_in)
        val outAnim = AnimationUtils.loadAnimation(requireContext(),
                android.R.anim.fade_out)
        inAnim.duration = 200
        outAnim.duration = 200

        statsRankPoints?.inAnimation = inAnim
        statsRankPoints?.outAnimation = outAnim

        timeline_top_card?.setOnClickListener {
            if (top_div?.visibility == View.VISIBLE) {
                top_div?.visibility = View.GONE
                top_div2?.visibility = View.GONE

                statsTopExtras1?.visibility = View.GONE
                statsTopExtras2?.visibility = View.GONE

                statsTopDropDown?.setImageResource(R.drawable.ic_arrow_drop_down_24dp)
            } else {
                top_div?.visibility = View.VISIBLE
                top_div2?.visibility = View.VISIBLE

                statsTopExtras1?.visibility = View.VISIBLE
                statsTopExtras2?.visibility = View.VISIBLE

                statsTopDropDown?.setImageResource(R.drawable.ic_arrow_drop_up_24dp)
            }
        }

        /*if (!Premium.isAdFreeUser()) {
            val statsBanner = AdView(requireContext())
            statsBanner.adSize = com.google.android.gms.ads.AdSize.BANNER
            statsBanner.adUnitId = "ca-app-pub-2237535196399997/3286029778"
            statsBanner.loadAd(Ads.getAdBuilder())
            statsFragList?.addView(statsBanner)
        }*/
    }

    private fun updateStats(stats: PlayerStats) {
        statsKills.text = stats.kills.toString()

        statsRankIcon?.setImageResource(getRankIcon(stats.getRank()))
        //statsPlayerName?.text = Ranks.getRankTitle(stats.rankPoints)
        if (stats.getRank() != Rank.UNKNOWN)
            statsPlayerName?.text = stats.getRank().title.toUpperCase() + " " + stats.getRankLevel()
        else
            statsPlayerName?.text = "UNRANKED"

        statsRankPoints?.setCurrentText("POINTS: ${formatter.format(Math.floor(stats.rankPoints).toInt())}")


        val runnableCode = object : Runnable {
            override fun run() {
                statsRankPoints?.setText("BEST POINTS: ${formatter.format(Math.floor(stats.bestRankPoint).toInt())}")

                val secondRun = Runnable { statsRankPoints?.setText("POINTS: ${formatter.format(Math.floor(stats.rankPoints).toInt())}") }

                handler.postDelayed(secondRun, 5000)

                handler.postDelayed(this, 10000)
            }
        }

        handler.postDelayed(runnableCode, 5000)

        statsWins?.text = stats.wins.toString()
        statsTopWins?.text = (stats.top10s - stats.wins).toString()
        statsHeadshots?.text = stats.headshotKills.toString()
        if (stats.kills != 0 && stats.losses != 0) statsKD?.text = String.format("%.2f", stats.kills.toDouble() / stats.losses.toDouble()) else statsKD?.text = "0.00"
        statsGamesPlayed?.text = stats.roundsPlayed.toString()
        statsAssists?.text = stats.assists.toString()
        if (stats.wins != 0 && stats.roundsPlayed != 0) statsLosses?.text = "${String.format("%.2f", (stats.wins.toDouble() / stats.roundsPlayed.toDouble()) * 100)}%" else statsLosses?.text = "0%"
        statsdBNOs?.text = stats.dBNOs.toString()
        statsMostKills?.text = stats.roundMostKills.toString()
        if (stats.damageDealt != 0.0 && stats.roundsPlayed != 0) statsAvgDamage?.text = String.format("%.0f", Math.ceil(stats.damageDealt / stats.roundsPlayed.toDouble())) else  statsAvgDamage?.text = "0"
        if (stats.headshotKills != 0 && stats.kills != 0) statsHeadshotPct?.text = "${String.format("%.2f", (stats.headshotKills.toDouble() / stats.kills.toDouble()) * 100)}%" else statsHeadshotPct?.text = "0%"

        statsRideDist?.text = "${String.format("%.0f", Math.ceil(stats.rideDistance))}m"
        statsSwimDist?.text = "${String.format("%.0f", Math.ceil(stats.swimDistance))}m"
        statsWalkDist?.text = "${String.format("%.0f", Math.ceil(stats.walkDistance))}m"

        statsLongestKill?.text = "${String.format("%.0f", Math.ceil(stats.longestKill))}m"
        statsRoadKills?.text = stats.roadKills.toString()
        statsTeamKills?.text = stats.teamKills.toString()

        statsBoosts?.text = stats.boosts.toString()
        statsHeals?.text = stats.heals.toString()
        statsRevives?.text = stats.revives.toString()

        statsTotalDamageDealt?.text = stats.damageDealt.roundToInt().toString()
        statsVehicleDestroys?.text = stats.vehicleDestroys.toString()

        statsTotalTime?.text = stats.getTotalTimeSurvived()
        statsLongestTime?.text = stats.getLongTimeSurvived()
    }
}
