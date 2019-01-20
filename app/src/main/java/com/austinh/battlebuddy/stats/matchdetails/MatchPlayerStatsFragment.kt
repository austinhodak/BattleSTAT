package com.austinh.battlebuddy.stats.matchdetails

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.austinh.battlebuddy.R
import com.austinh.battlebuddy.models.LogPlayerKill
import com.austinh.battlebuddy.models.LogPlayerTakeDamage
import com.austinh.battlebuddy.utils.Telemetry
import com.austinh.battlebuddy.viewmodels.MatchDetailViewModel
import com.austinh.battlebuddy.viewmodels.models.MatchModel
import kotlinx.android.synthetic.main.fragment_matches_player_stats.*
import net.idik.lib.slimadapter.SlimAdapter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt
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

        playerKillsCard?.setOnClickListener {
            if (playerKillsRV?.visibility == View.GONE) {
                playerKillsRV?.visibility = View.VISIBLE
            } else {
                playerKillsRV?.visibility = View.GONE
            }
        }

        statsDamageCard?.setOnClickListener {
            if (damageBottomExtras?.visibility == View.GONE) {
                damageBottomExtras?.visibility = View.VISIBLE
            } else {
                damageBottomExtras?.visibility = View.GONE
            }
        }

        //stats_scroll_view.visibility = View.VISIBLE
    }

    private fun matchDataLoaded(it: MatchModel) {
        fillStats(it)
        setupAndFillKills(it)
        setupAndFillDamage(it)
    }

    private fun setupAndFillKills(it: MatchModel) {
        var match = it

        val killsList = ArrayList<LogPlayerKill>()

        for (kill in match.killFeedList) {
            //Log.d("MATCH", "ID: $playerID - ${kill.killer.accountId}")
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

        playerKillsRV.layoutManager = LinearLayoutManager(requireActivity())
        playerKillsRV.isNestedScrollingEnabled = false
        SlimAdapter.create().attachTo(playerKillsRV).updateData(killsList).register(R.layout.stats_kill_feed_item2) { data: LogPlayerKill, injector ->
            if (killsList.indexOf(data) == (killsList.size -1)) {
                //Last item so remove div.
                //injector.gone(R.id.div)
            }

            injector.text(R.id.kill_feed_victim, data.victim.name)
            //injector.text(R.id.textView9, ordinal(data.victim.ranking))

            var reasonTV = injector.findViewById<TextView>(R.id.killReasonTV)
            var reasonIcon = injector.findViewById<ImageView>(R.id.killReasonIcon)

            if (Telemetry.getDamageCausers(requireContext())[data.damageCauserName].toString() == "Player") {
                injector.text(R.id.kill_feed_cause, Telemetry.getDamageTypes(requireContext())[data.damageTypeCategory].toString())
            } else {
                injector.text(R.id.kill_feed_cause, Telemetry.getDamageCausers(requireContext())[data.damageCauserName].toString())
            }

            when (data.damageReason) {
                "ArmShot" -> {
                    reasonTV.text = "ARM"
                    reasonIcon.setImageResource(R.drawable.icons8_arm)
                }
                "HeadShot" -> {
                    reasonTV.text = "HEAD"
                    reasonIcon.setImageResource(R.drawable.icons8_skull)
                }
                "LegShot" -> {
                    reasonTV.text = "LEG"
                    reasonIcon.setImageResource(R.drawable.icons8_leg)
                }
                "PelvisShot" -> {
                    reasonTV.text = "PELVIS"
                    reasonIcon.setImageResource(R.drawable.pelvis)
                }
                "TorsoShot" -> {
                    reasonTV.text = "TORSO"
                    reasonIcon.setImageResource(R.drawable.torso)
                }
                else -> {
                    reasonTV.text = "N/A"
                    reasonIcon.setImageResource(R.drawable.question)
                }
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

    private fun setupAndFillDamage(it: MatchModel) {
        val match = it

        var damageList: MutableList<LogPlayerTakeDamage>

        Log.d("PLAYERID", "$playerID - ${match.currentPlayerID}")

        var id = match.currentPlayerID
        if (playerID != null) id = match.participantHash[playerID!!]!!.attributes.stats.playerId

        try {
            damageList = it.logPlayerTakeDamage.filter { it.attacker.accountId == id || it.victim.accountId == id && it.attacker.name.isNotEmpty() && it.victim.name.isNotEmpty() }.toMutableList()
            damageList = damageList.filterNot { it.attacker.accountId == it.victim.accountId }.toMutableList()
            damageList = damageList.sortedWith(compareBy { it._D }).toMutableList()

            playerDamageRV.layoutManager = LinearLayoutManager(requireActivity())
            playerDamageRV.isNestedScrollingEnabled = false
            SlimAdapter.create().attachTo(playerDamageRV).updateData(damageList).register(R.layout.stats_damage_list_item) { data: LogPlayerTakeDamage, injector ->

                injector.clicked(R.id.damageTop) {
                    //toast(data.)
                }

                injector.text(R.id.kill_feed_victim, "${data.attacker.name} attacked ${data.victim.name}")

                var reasonTV = injector.findViewById<TextView>(R.id.killReasonTV)
                var reasonIcon = injector.findViewById<ImageView>(R.id.killReasonIcon)

                if (Telemetry.getDamageCausers(requireContext())[data.damageCauserName].toString() == "Player") {
                    injector.text(R.id.kill_feed_cause, Telemetry.getDamageTypes(requireContext())[data.damageTypeCategory].toString())
                } else {
                    injector.text(R.id.kill_feed_cause, Telemetry.getDamageCausers(requireContext())[data.damageCauserName].toString())
                }

                when (data.damageReason) {
                    "ArmShot" -> {
                        reasonTV.text = "ARM"
                        reasonIcon.setImageResource(R.drawable.icons8_arm)
                    }
                    "HeadShot" -> {
                        reasonTV.text = "HEAD"
                        reasonIcon.setImageResource(R.drawable.icons8_skull)
                    }
                    "LegShot" -> {
                        reasonTV.text = "LEG"
                        reasonIcon.setImageResource(R.drawable.icons8_leg)
                    }
                    "PelvisShot" -> {
                        reasonTV.text = "PELVIS"
                        reasonIcon.setImageResource(R.drawable.pelvis)
                    }
                    "TorsoShot" -> {
                        reasonTV.text = "TORSO"
                        reasonIcon.setImageResource(R.drawable.torso)
                    }
                    else -> {
                        reasonTV.text = "N/A"
                        reasonIcon.setImageResource(R.drawable.question)
                    }
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

                val damageString = if ((data.victim.health.roundToInt() - data.damage.roundToInt()) <= 0) {
                    "${data.victim.health.roundToInt()} TO DEAD"
                } else {
                    "${data.victim.health.roundToInt()} TO ${(data.victim.health.roundToInt() - data.damage.roundToInt())}"
                }

                injector.text(R.id.kill_feed_distance, "$damageString • ${data.damage.roundToInt()} DMG")

                val sideBarHeight = 60.0 * ((data.victim.health.roundToInt() - data.damage.roundToInt()) / 100.0)
                Log.d("SIDEBAR", "${(data.victim.health.roundToInt() - data.damage.roundToInt()) / 100.0}")

                val damageBar = injector.findViewById<View>(R.id.damagebar)
                val params = RelativeLayout.LayoutParams(getDp(3f), getDp(sideBarHeight.toFloat()))
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                damageBar?.layoutParams = params
                if (sideBarHeight == 0.0) {
                    damageBar?.visibility = View.INVISIBLE
                } else {
                    damageBar?.visibility = View.VISIBLE
                }

                if (match.getPlayerByAccountID(data.victim.accountId)?.id == playerID || data.victim.accountId == match.currentPlayerID) {
                    damageBar.setBackgroundColor(resources.getColor(R.color.timelineRed))
                } else {
                    damageBar.setBackgroundColor(resources.getColor(R.color.md_white_1000))
                }
            }
        } catch (e: Exception) {
        }
    }

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

        statsWinPlace?.text = "#${stats.winPlace}"
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


        try {
            val playerAttacks = matchModel.logPlayerAttack.filter { it.attacker.accountId == stats.playerId }
            val playerDamageAttacks = matchModel.logPlayerTakeDamage.filter { it.attacker.accountId == stats.playerId }
            damageTotalAttacks?.text = "${playerAttacks.size}"
            damageTotalDamageAttacks?.text = "${playerDamageAttacks.size}"
        } catch (e: Exception) {
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

    fun getDp(dp: Float): Int {
        val r = requireContext().resources
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.displayMetrics
        ).toInt()
    }
}