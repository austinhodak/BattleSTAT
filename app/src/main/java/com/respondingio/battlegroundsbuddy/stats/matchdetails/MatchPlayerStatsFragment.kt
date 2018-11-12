package com.respondingio.battlegroundsbuddy.stats.matchdetails

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.viewmodels.MatchDetailViewModel
import com.respondingio.battlegroundsbuddy.viewmodels.models.MatchModel
import kotlinx.android.synthetic.main.fragment_matches_player_stats.*
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
                //top.backgroundColor = Color.parseColor("#FAFAFA")
            } else {
                val bottomUp = AnimationUtils.loadAnimation(context, R.anim.bottom_up)
                //stats_scroll_view.startAnimation(bottomUp)

            }

            if (!arguments!!.containsKey("match")) {
                viewModel.mMatchData.observe(this, matchDataObserver)
            } else {
                matchDataLoaded(arguments!!.getSerializable("match") as MatchModel)
            }
        } else {
            viewModel.mMatchData.observe(this, matchDataObserver)
            val bottomUp = AnimationUtils.loadAnimation(context, R.anim.bottom_up)
            //stats_scroll_view.startAnimation(bottomUp)
        }

        //stats_scroll_view.visibility = View.VISIBLE
    }

    private fun matchDataLoaded(it: MatchModel) {
        fillStats(it)
        //setupAndFillKills(it)
    }

    /*private fun setupAndFillKills(it: MatchModel) {
        var match = it

        val killsList = ArrayList<LogPlayerKill>()

        for (kill in match.killFeedList) {
            Log.d("MATCH", "ID: $playerID - ${kill.killer.accountId}")
            if (playerID == null) {
                if (kill.killer.accountId == match.currentPlayerID) {
                    killsList.add(kill)
                }
            } else {
                if (kill.killer.accountId == match.participantHash[playerID!!]!!.attributes.stats.playerId) {
                    killsList.add(kill)
                }
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
    }*/

    @SuppressLint("SetTextI18n")
    private fun fillStats(matchModel: MatchModel) {
        val match = matchModel

        val stats = if (playerID != null && !playerID!!.contains("account.", true)) {
            match.participantHash[playerID!!]?.attributes?.stats!!
        } else if (playerID != null &&playerID!!.contains("account.", false)) {
            match.getPlayerByAccountID(playerID!!)?.attributes?.stats!!
        } else {
            match.currentPlayer?.attributes!!.stats
        }

        statsRideDist?.text = "${String.format("%.0f", Math.ceil(stats.rideDistance))}m"
        statsSwimDist?.text = "${String.format("%.0f", Math.ceil(stats.swimDistance))}m"
        statsWalkDist?.text = "${String.format("%.0f", Math.ceil(stats.walkDistance))}m"

        statsLongestKill?.text = "${String.format("%.0f", Math.ceil(stats.longestKill))}m"
        statsRoadKills?.text = stats.roadKills.toString()
        statsTeamKills?.text = stats.teamKills.toString()

        statsBoosts?.text = stats.boosts.toString()
        statsHeals?.text = stats.heals.toString()
        statsRevives?.text = stats.revives.toString()

        statsKills?.text = stats.kills.toString()

        statsWinPlace?.text = "${ordinal(stats.winPlace)} Place"
        statsPlayerName?.text = stats.name

        statsWeaponsAq?.text = stats.weaponsAcquired.toString()

        if (stats.kills != 0 && stats.headshotKills != 0)
            statsHeadshotPct?.text = "${String.format("%.2f", (stats.headshotKills.toDouble() / stats.kills.toDouble()) * 100)}%"

        statsKillsPlace?.text = stats.killPlace.toString()
        statsAssists?.text = stats.assists.toString()
        statsdBNOs?.text = stats.DBNOs.toString()
        statsKillStreaks?.text = stats.killStreaks.toString()
        statsMostDamage?.text = stats.mostDamage.roundToLong().toString()
        statsDamageDealt?.text = stats.damageDealt.roundToLong().toString()
        statsTimeSurv?.text = "${Math.ceil(stats.timeSurvived / 60).toInt()} Min"
        statsHeadshots?.text = stats.headshotKills.toString()
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