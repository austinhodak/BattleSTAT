package com.respondingio.battlegroundsbuddy.stats.main


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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.ads.AdView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.functions.FirebaseFunctions
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.models.PlayerStats
import com.respondingio.battlegroundsbuddy.models.PrefPlayer
import com.respondingio.battlegroundsbuddy.utils.Ads
import com.respondingio.battlegroundsbuddy.utils.Premium
import com.respondingio.battlegroundsbuddy.utils.Ranks
import com.respondingio.battlegroundsbuddy.utils.Ranks.getRankIcon
import com.respondingio.battlegroundsbuddy.viewmodels.PlayerStatsViewModel
import com.respondingio.battlegroundsbuddy.viewmodels.models.PlayerModel
import kotlinx.android.synthetic.main.player_stats_main_new.*
import java.text.DecimalFormat

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

    private var updateTimeout: Long = 15

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mainView = inflater.inflate(R.layout.player_stats_main_new, container, false)
        return mainView
    }

    private var mSharedPreferences: SharedPreferences? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("TESTING", "ONVIEWCREATED CALLED")
        mDatabase = FirebaseDatabase.getInstance().reference
        mFunctions = FirebaseFunctions.getInstance()
        mSharedPreferences = activity!!.getSharedPreferences("com.respondingio.battlegroundsbuddy", Context.MODE_PRIVATE)

        if (mSharedPreferences!!.getBoolean("premiumV1", false)) {
            updateTimeout = 2
        }

        val player: PrefPlayer = arguments!!.getSerializable("selectedPlayer") as PrefPlayer

        viewModel.playerData.observe(this, Observer<PlayerModel> {
            handler.removeCallbacksAndMessages(null)
            updateStats(it.getStatsByGamemode(player.selectedGamemode!!)!!)
            statsKills.text = it.getStatsByGamemode(player.selectedGamemode!!)?.kills.toString()

            if (it.lastUpdated != null && it.lastUpdated != 0.toLong()) {
                val relTime = DateUtils
                        .getRelativeTimeSpanString(it.lastUpdated!! * 1000L,
                                System.currentTimeMillis(),
                                DateUtils.MINUTE_IN_MILLIS).toString().toUpperCase()


                statsLastUpdatedTop?.text = "LAST UPDATED: $relTime"

                Log.d("STATS", "MINUTES SINCE LAST UPDATE ${it.getMinutesSinceLastUpdated()}")

                if (it.getMinutesSinceLastUpdated() >= 15) {
                    stats_corner_alert?.setOutdated()
                } else {
                    stats_corner_alert?.hide()
                }
            } else {
                statsLastUpdatedTop?.text = "LAST UPDATED: NEVER, PULL TO REFRESH"
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

        if (!Premium.isAdFreeUser()) {
            val statsBanner = AdView(requireContext())
            statsBanner.adSize = com.google.android.gms.ads.AdSize.BANNER
            statsBanner.adUnitId = "ca-app-pub-1946691221734928/5335722082"
            statsBanner.loadAd(Ads.getAdBuilder())
            statsFragList?.addView(statsBanner)
        }
    }

    private fun updateStats(stats: PlayerStats) {
        statsKills.text = stats.kills.toString()

        statsRankIcon?.setImageResource(getRankIcon(stats.rankPoints))
        statsPlayerName?.text = Ranks.getRankTitle(stats.rankPoints)

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
        statsKD?.text = String.format("%.2f", stats.kills.toDouble() / stats.losses.toDouble())
        statsGamesPlayed?.text = stats.roundsPlayed.toString()
        statsAssists?.text = stats.assists.toString()
        statsLosses?.text = "${String.format("%.2f", (stats.wins.toDouble() / stats.roundsPlayed.toDouble()) * 100)}%"
        statsdBNOs?.text = stats.dBNOs.toString()
        statsMostKills?.text = stats.roundMostKills.toString()
        statsAvgDamage?.text = String.format("%.0f", Math.ceil(stats.damageDealt / stats.roundsPlayed.toDouble()))
        statsHeadshotPct?.text = "${String.format("%.2f", (stats.headshotKills.toDouble() / stats.kills.toDouble()) * 100)}%"

        statsRideDist?.text = "${String.format("%.0f", Math.ceil(stats.rideDistance))}m"
        statsSwimDist?.text = "${String.format("%.0f", Math.ceil(stats.swimDistance))}m"
        statsWalkDist?.text = "${String.format("%.0f", Math.ceil(stats.walkDistance))}m"

        statsLongestKill?.text = "${String.format("%.0f", Math.ceil(stats.longestKill))}m"
        statsRoadKills?.text = stats.roadKills.toString()
        statsTeamKills?.text = stats.teamKills.toString()

        statsBoosts?.text = stats.boosts.toString()
        statsHeals?.text = stats.heals.toString()
        statsRevives?.text = stats.revives.toString()
    }
}
