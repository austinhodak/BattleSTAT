package com.respondingio.battlegroundsbuddy.models

import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

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
        val RankPoint: Double = 0.0
) : Serializable