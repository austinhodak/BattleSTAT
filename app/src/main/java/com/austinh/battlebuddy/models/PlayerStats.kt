package com.austinh.battlebuddy.models

import com.austinh.battlebuddy.utils.Rank
import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

@IgnoreExtraProperties
data class PlayerStats(
        val assists: Int = 0,
        val boosts: Int = 0,
        val dBNOs: Int = 0,
        val dailyKills: Int = 0,
        val damageDealt: Double = 0.0,
        val days: Int = 0,
        val headshotKills: Int = 0,
        val heals: Int = 0,
        val killPoints: Double = 0.0,
        val kills: Int = 0,
        val longestKill: Double = 0.0,
        val longestTimeSurvived: Double = 0.0,
        val losses: Int = 0,
        val maxKillStreaks: Int = 0,
        val revives: Int = 0,
        val rideDistance: Double = 0.0,
        val roadKills: Int = 0,
        val roundMostKills: Int = 0,
        val roundsPlayed: Int = 0,
        val suicides: Int = 0,
        val teamKills: Int = 0,
        val timeSurvived: Double = 0.0,
        val top10s: Int = 0,
        val vehicleDestroys: Int = 0,
        val walkDistance: Double = 0.0,
        val weaponsAcquired: Int = 0,
        val weeklyKills: Int = 0,
        val winPoints: Double = 0.0,
        val wins: Int = 0,
        val rankPoints: Double = 0.0,
        val bestRankPoint: Double = 0.0,
        val dailyWins: Int = 0,
        val swimDistance: Double = 0.0,
        val weeklyWins: Int = 0,
        val rankPointsTitle: String = ""
) : Serializable {
    fun getRank() : Rank {
        if (rankPointsTitle.isEmpty()) return Rank.UNKNOWN
        val rankNum = rankPointsTitle.split("-")[0]
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

    fun getRankLevel(roman: Boolean = true) : String {
        if (rankPointsTitle.isEmpty() || rankPointsTitle == "7-0") {
            return ""
        }
        val rankLevel = rankPointsTitle.split("-").toTypedArray()[1].toInt()
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
}