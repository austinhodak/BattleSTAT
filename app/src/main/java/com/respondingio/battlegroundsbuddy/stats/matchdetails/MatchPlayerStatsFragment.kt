package com.respondingio.battlegroundsbuddy.stats.matchdetails

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.Telemetry
import com.respondingio.battlegroundsbuddy.models.LogPlayerKill
import com.respondingio.battlegroundsbuddy.models.Stats
import com.respondingio.battlegroundsbuddy.viewmodels.MatchDetailViewModel
import com.respondingio.battlegroundsbuddy.viewmodels.models.MatchModel
import kotlinx.android.synthetic.main.fragment_matches_player_stats.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.textColor
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.TimeZone
import kotlin.math.roundToLong

class MatchPlayerStatsFragment : Fragment() {

    private val viewModel: MatchDetailViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(MatchDetailViewModel::class.java)
    }

    private val matchDataObserver = Observer<MatchModel> {
        value -> value?.let {
            matchDataLoaded(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_matches_player_stats, container, false)
    }

    private var playerID: String? = null

    private var matchSingle: MatchModel? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments != null && arguments!!.containsKey("playerID")) {
            playerID = arguments!!.getString("playerID")
            if (arguments!!.containsKey("isTabs")) {
                top.backgroundColor = Color.parseColor("#FAFAFA")
            } else {
                val bottomUp = AnimationUtils.loadAnimation(context, R.anim.bottom_up)
                stats_scroll_view.startAnimation(bottomUp)

            }

            if (!arguments!!.containsKey("match")) {
                viewModel.mMatchData.observe(this, matchDataObserver)
            } else {
                matchDataLoaded(arguments!!.getSerializable("match") as MatchModel)
            }
        } else {
            viewModel.mMatchData.observe(this, matchDataObserver)
            val bottomUp = AnimationUtils.loadAnimation(context, R.anim.bottom_up)
            stats_scroll_view.startAnimation(bottomUp)
        }

        stats_scroll_view.visibility = View.VISIBLE
    }

    private fun matchDataLoaded(it: MatchModel) {
        Log.d("MATCH", it.attributes?.gameMode)
        fillStats(it)
        setupAndFillKills(it)
    }

    private fun setupAndFillKills(it: MatchModel) {
        var match = it

        val killsList = ArrayList<LogPlayerKill>()

        for (kill in match.killFeedList) {
            if (kill.killer.accountId == match.currentPlayerID) {
                killsList.add(kill)
            } else if (kill.victim.accountId == match.currentPlayerID) {
                //This PlayerKill (death) is the current user.

            }
        }

        if (killsList.isEmpty()) {
            div2.visibility = View.GONE
        }

        stats_killsRV.layoutManager = LinearLayoutManager(requireActivity())
        stats_killsRV.isNestedScrollingEnabled = false
        SlimAdapter.create().attachTo(stats_killsRV).updateData(killsList).register(R.layout.stats_kill_feed_item2) { data: LogPlayerKill, injector ->
            if (killsList.indexOf(data) == (killsList.size -1)) {
                //Last item so remove div.
                injector.gone(R.id.div)
            }

            injector.text(R.id.kill_feed_victim, data.victim.name)
            injector.text(R.id.textView9, ordinal(data.victim.ranking))

            if (Telemetry().damageCauserName[data.damageCauserName].toString() == "Player") {
                injector.text(R.id.kill_feed_cause, Telemetry().damageTypeCategory[data.damageTypeCategory].toString())
            } else {
                injector.text(R.id.kill_feed_cause, Telemetry().damageCauserName[data.damageCauserName].toString())
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            sdf.timeZone = TimeZone.getTimeZone("GMT")
            val sdf2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            sdf2.timeZone = TimeZone.getTimeZone("GMT")
            val matchStartDate = sdf.parse(match.attributes?.createdAt)
            val killTime = sdf2.parse(data._D)

            var difference = killTime.time - matchStartDate.time

            val secondsInMilli: Long = 1000
            val minutesInMilli = secondsInMilli * 60
            val hoursInMilli = minutesInMilli * 60
            val daysInMilli = hoursInMilli * 24

            difference %= daysInMilli

            difference %= hoursInMilli

            val elapsedMinutes = difference / minutesInMilli
            difference %= minutesInMilli

            val elapsedSeconds = difference / secondsInMilli

            injector.text(R.id.kill_feed_time, String.format("%02d:%02d", elapsedMinutes, elapsedSeconds))

            injector.text(R.id.kill_feed_distance, "${String.format("%.0f", Math.rint(data.distance/100))}m")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fillStats(matchModel: MatchModel) {
        var match = matchModel

        Log.d("MATCH", match.getFormattedCreatedAt())

        val stats: Stats
        if (playerID != null && !playerID!!.contains("account.", true)) {
            stats = match.participantHash[playerID!!]?.attributes?.stats!!
        } else if (playerID != null &&playerID!!.contains("account.", false)) {
            stats = match.getPlayerByAccountID(playerID!!)?.attributes?.stats!!
        } else {
            stats = match.currentPlayer?.attributes!!.stats
        }

        stats_rideDist.text = "${Math.rint(stats.rideDistance).toLong()}m"
        stats_walkDist.text = "${Math.rint(stats.walkDistance).toLong()}m"
        stats_swimDist.text = "${Math.rint(stats.swimDistance).toLong()}m"

        val winPointsText: String

        if (stats.winPointsDelta >= 0) {
            winPointsText = "${stats.winPoints} (+${String.format("%.0f", stats.winPointsDelta)})"
            //stats_winPoints.textColor = resources.getColor(R.color.md_green_A700)
        } else {
            winPointsText = "${stats.winPoints} (${String.format("%.0f", stats.winPointsDelta)})"
            //stats_winPoints.textColor = resources.getColor(R.color.md_red_A700)
        }

        val killPointsText: String

        if (stats.killPointsDelta >= 0) {
            killPointsText = "${stats.killPoints} (+${String.format("%.0f", stats.killPointsDelta)})"
            //stats_killPoints.textColor = resources.getColor(R.color.md_green_A700)
        } else {
            killPointsText = "${stats.killPoints} (${String.format("%.0f", stats.killPointsDelta)})"
            //stats_killPoints.textColor = resources.getColor(R.color.md_red_A700)
        }

        stats_winPoints.text = winPointsText
        stats_killPoints.text = killPointsText
        stats_weaponsAqd.text = stats.weaponsAcquired.toString()
        stats_boosts.text = stats.boosts.toString()
        stats_killStreaks.text = stats.killStreaks.toString()
        stats_timeSurv.text = "${(stats.timeSurvived/60).roundToLong()} Min"
        stats_vehicleDestroy.text = stats.vehicleDestroys.toString()
        stats_mostDamageDealt.text = stats.mostDamage.roundToLong().toString()

        stats_kills.text = stats.kills.toString()
        stats_headshots.text = stats.headshotKills.toString()
        stats_assists.text = stats.assists.toString()
        stats_roadKills.text = stats.roadKills.toString()
        stats_dbnos.text = stats.DBNOs.toString()
        stats_longestKill.text = "${stats.longestKill.roundToLong()}m"
        stats_teamKills.text = stats.teamKills.toString()
        stats_heals.text = stats.heals.toString()
        stats_damageDealt.text = stats.damageDealt.roundToLong().toString()
        stats_revives.text = stats.revives.toString()

        if (stats.killPoints == 0.0 && stats.winPoints == 0.0 && stats.rankPoints > 0.0) {
            rank_card?.visibility = View.VISIBLE
            rank_title?.text = getRankTitle(stats.rankPoints)
            rank_subtitle?.text = "POINTS: ${Math.floor(stats.rankPoints).toInt()}"

            Glide.with(this).load(getRankIcon(stats.rankPoints)).into(rank_icon)

            old_points_layout?.visibility = View.GONE
            divider13?.visibility = View.INVISIBLE
        } else {
            rank_card?.visibility = View.GONE

            old_points_layout?.visibility = View.VISIBLE

            stats_killPoints?.text = String.format("%.0f", Math.rint(stats.killPoints))
            stats_winPoints?.text = String.format("%.0f", Math.rint(stats.winPoints))
        }

        if (stats.killPoints == 0.0 && stats.winPoints == 0.0 && stats.rankPoints > 0.0) {
            winPointsTitle?.text = "RANK POINTS *NEW"
            stats_winPoints?.text = String.format("%.0f", Math.rint(stats.rankPoints))

            killPoints_view?.visibility = View.GONE
        } else {
            stats_killPoints?.text = String.format("%.0f", Math.rint(stats.killPoints))
            stats_winPoints?.text = String.format("%.0f", Math.rint(stats.winPoints))
        }
    }

    fun ordinal(i: Int): String {
        val sufixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
        return when (i % 100) {
            11, 12, 13 -> i.toString() + "th"
            else -> i.toString() + sufixes[i % 10]
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