package com.respondingio.battlegroundsbuddy.models

data class MatchParticipant(
        val id: String,
        val attributes: MatchAttributes,
        val type: String
)

data class MatchAttributes(
        val shardId: String,
        val stats: Stats,
        val actor: String
)

data class Stats(
        val DBNOs: Int,
        val assists: Int,
        val boosts: Int,
        val damageDealt: Double,
        val deathType: String,
        val headshotKills: Int,
        val heals: Int,
        val killPlace: Int,
        val killPoints: Int,
        val killPointsDelta: Double,
        val killStreaks: Int,
        val kills: Int,
        val lastKillPoints: Int,
        val lastWinPoints: Int,
        val longestKill: Double,
        val mostDamage: Double,
        val name: String,
        val playerId: String,
        val revives: Int,
        val rideDistance: Double,
        val roadKills: Int,
        val swimDistance: Double,
        val teamKills: Int,
        val timeSurvived: Double,
        val vehicleDestroys: Int,
        val walkDistance: Double,
        val weaponsAcquired: Int,
        val winPlace: Int,
        val winPoints: Int,
        val winPointsDelta: Double
)