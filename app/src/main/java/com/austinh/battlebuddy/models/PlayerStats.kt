package com.austinh.battlebuddy.models

import com.austinh.battlebuddy.stats.main.Unit
import com.austinh.battlebuddy.utils.Rank
import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

@IgnoreExtraProperties
data class PlayerStats(
        var assists: Int = 0,
        var boosts: Int = 0,
        var dBNOs: Int = 0,
        var dailyKills: Int = 0,
        var damageDealt: Double = 0.0,
        var days: Int = 0,
        var headshotKills: Int = 0,
        var heals: Int = 0,
        var killPoints: Double = 0.0,
        var kills: Int = 0,
        var longestKill: Double = 0.0,
        var longestTimeSurvived: Double = 0.0,
        var losses: Int = 0,
        var maxKillStreaks: Int = 0,
        var revives: Int = 0,
        var rideDistance: Double = 0.0,
        var roadKills: Int = 0,
        var roundMostKills: Int = 0,
        var roundsPlayed: Int = 0,
        var suicides: Int = 0,
        var teamKills: Int = 0,
        var timeSurvived: Double = 0.0,
        var top10s: Int = 0,
        var vehicleDestroys: Int = 0,
        var walkDistance: Double = 0.0,
        var weaponsAcquired: Int = 0,
        var weeklyKills: Int = 0,
        var winPoints: Double = 0.0,
        var wins: Int = 0,
        var rankPoints: Double = 0.0,
        var bestRankPoint: Double = 0.0,
        var dailyWins: Int = 0,
        var swimDistance: Double = 0.0,
        var weeklyWins: Int = 0,
        var rankPointsTitle: String = "0-0"
) : Serializable {
    fun getRank(string: String? = null) : Rank {
        val rankNum: String = if (string == null) {
            if (rankPointsTitle.isEmpty()) return Rank.UNKNOWN
            rankPointsTitle.split("-")[0]
        } else {
            string.split("-")[0]
        }

        return when(rankNum) {
            "0" -> Rank.UNKNOWN
            "1" -> Rank.BEGINNER
            "2" -> Rank.NOVICE
            "3" -> Rank.EXPERIENCED
            "4" -> Rank.SKILLED
            "5" -> Rank.SPECIALIST
            "6" -> Rank.EXPERT
            "7" -> Rank.SURVIVOR
            else -> Rank.UNKNOWN
        }
    }

    fun getRankLevel(roman: Boolean = true, rank: String? = null) : String {
        val rankLevel: Int = if (rank == null) {
            if (rankPointsTitle.isEmpty() || rankPointsTitle == "7-0") {
                return ""
            }
            rankPointsTitle.split("-").toTypedArray()[1].toInt()
        } else {
            rank.split("-").toTypedArray()[1].toInt()
        }

        if (roman) {
            return when (rankLevel) {
                0 -> "0"
                1 -> "I"
                2 -> "II"
                3 -> "III"
                4 -> "IV"
                5 -> "V"
                else -> "0"
            }
        } else {
            return when (rankLevel) {
                0 -> "Level 0"
                1 -> "Level 1"
                2 -> "Level 2"
                3 -> "Level 3"
                4 -> "Level 4"
                5 -> "Level 5"
                else -> "Level 0"
            }
        }
    }

    fun getTotalTimeSurvived(): String {
        val time = timeSurvived.roundToLong()

        val days: Long = TimeUnit.SECONDS.toDays(time)

        val hours = TimeUnit.SECONDS.toHours(time) - (days * 24)

        val minutes = TimeUnit.SECONDS.toMinutes(time) - (TimeUnit.SECONDS.toHours(time)* 60)

        return "${days}d ${hours}h ${minutes}m"
    }

    fun getLongTimeSurvived(): String {
        val time = longestTimeSurvived.roundToLong()
        val minutes = (time % 3600) / 60
        val seconds = time % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun getDistance(which: Distance, unit: Unit = Unit.METRIC): String {
        val value = when (which) {
            Distance.RIDING -> rideDistance
            Distance.SWIMMING -> swimDistance
            Distance.WALKING -> walkDistance
            Distance.LONGEST_KILL -> longestKill
        }

        return if (unit == Unit.METRIC) {
            if (value < 10000) "${String.format("%.0f", Math.ceil(value))}m"
            else "${String.format("%.2f", value / 1000)}km"
        } else {
            if (value < 10000) "${String.format("%.0f", Math.ceil(value * 1.094))}yd"
            else "${String.format("%.2f", value / 1609.344)}mi"
        }
    }

    enum class Distance {
        RIDING,
        SWIMMING,
        WALKING,
        LONGEST_KILL
    }
}