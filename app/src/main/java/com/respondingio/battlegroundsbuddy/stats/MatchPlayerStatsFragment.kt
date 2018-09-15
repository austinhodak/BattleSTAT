package com.respondingio.battlegroundsbuddy.stats

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.Telemetry
import com.respondingio.battlegroundsbuddy.models.LogPlayerKill
import com.respondingio.battlegroundsbuddy.models.MatchParticipant
import kotlinx.android.synthetic.main.fragment_matches_player_stats.div2
import kotlinx.android.synthetic.main.fragment_matches_player_stats.stats_assists
import kotlinx.android.synthetic.main.fragment_matches_player_stats.stats_boosts
import kotlinx.android.synthetic.main.fragment_matches_player_stats.stats_damageDealt
import kotlinx.android.synthetic.main.fragment_matches_player_stats.stats_dbnos
import kotlinx.android.synthetic.main.fragment_matches_player_stats.stats_headshots
import kotlinx.android.synthetic.main.fragment_matches_player_stats.stats_heals
import kotlinx.android.synthetic.main.fragment_matches_player_stats.stats_killPoints
import kotlinx.android.synthetic.main.fragment_matches_player_stats.stats_killStreaks
import kotlinx.android.synthetic.main.fragment_matches_player_stats.stats_kills
import kotlinx.android.synthetic.main.fragment_matches_player_stats.stats_killsRV
import kotlinx.android.synthetic.main.fragment_matches_player_stats.stats_longestKill
import kotlinx.android.synthetic.main.fragment_matches_player_stats.stats_mostDamageDealt
import kotlinx.android.synthetic.main.fragment_matches_player_stats.stats_revives
import kotlinx.android.synthetic.main.fragment_matches_player_stats.stats_rideDist
import kotlinx.android.synthetic.main.fragment_matches_player_stats.stats_roadKills
import kotlinx.android.synthetic.main.fragment_matches_player_stats.stats_swimDist
import kotlinx.android.synthetic.main.fragment_matches_player_stats.stats_teamKills
import kotlinx.android.synthetic.main.fragment_matches_player_stats.stats_timeSurv
import kotlinx.android.synthetic.main.fragment_matches_player_stats.stats_vehicleDestroy
import kotlinx.android.synthetic.main.fragment_matches_player_stats.stats_walkDist
import kotlinx.android.synthetic.main.fragment_matches_player_stats.stats_weaponsAqd
import kotlinx.android.synthetic.main.fragment_matches_player_stats.stats_winPoints
import kotlinx.android.synthetic.main.fragment_matches_player_stats.top
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.textColor
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.TimeZone
import kotlin.math.roundToLong

class MatchPlayerStatsFragment : Fragment() {

    lateinit var playerModel: MatchParticipant
    lateinit var killList: ArrayList<LogPlayerKill>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_matches_player_stats, container, false)
    }

    private var matchCreatedAt: String? = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null && arguments!!.containsKey("player")) {

            if (arguments!!.containsKey("isTabs")) {
                top.backgroundColor = Color.parseColor("#FAFAFA")
            }

            playerModel = arguments!!.getSerializable("player") as MatchParticipant
            killList = arguments!!.getSerializable("killList") as ArrayList<LogPlayerKill>
            matchCreatedAt = arguments!!.getString("matchCreatedAt")

            fillStats()
            setupAndFillKills()
        }
    }

    private fun setupAndFillKills() {
        val killsList = ArrayList<LogPlayerKill>()

        for (kill in killList) {
            if (kill.killer.accountId == playerModel.attributes.stats.playerId) {
                killsList.add(kill)
            } else if (kill.victim.accountId == playerModel.attributes.stats.playerId) {
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
            val matchStartDate = sdf.parse(matchCreatedAt)
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
    private fun fillStats() {
        val stats = playerModel.attributes.stats
        stats_rideDist.text = "${Math.rint(stats.rideDistance).toLong()}m"
        stats_walkDist.text = "${Math.rint(stats.walkDistance).toLong()}m"
        stats_swimDist.text = "${Math.rint(stats.swimDistance).toLong()}m"

        val winPointsText: String

        if (stats.winPointsDelta >= 0) {
            winPointsText = "${stats.winPoints} (+${String.format("%.0f", stats.winPointsDelta)})"
            stats_winPoints.textColor = resources.getColor(R.color.md_green_A700)
        } else {
            winPointsText = "${stats.winPoints} (${String.format("%.0f", stats.winPointsDelta)})"
            stats_winPoints.textColor = resources.getColor(R.color.md_red_A700)
        }

        val killPointsText: String

        if (stats.killPointsDelta >= 0) {
            killPointsText = "${stats.killPoints} (+${String.format("%.0f", stats.killPointsDelta)})"
            stats_killPoints.textColor = resources.getColor(R.color.md_green_A700)
        } else {
            killPointsText = "${stats.killPoints} (${String.format("%.0f", stats.killPointsDelta)})"
            stats_killPoints.textColor = resources.getColor(R.color.md_red_A700)
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
    }

    fun ordinal(i: Int): String {
        val sufixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
        return when (i % 100) {
            11, 12, 13 -> i.toString() + "th"
            else -> i.toString() + sufixes[i % 10]
        }
    }
}