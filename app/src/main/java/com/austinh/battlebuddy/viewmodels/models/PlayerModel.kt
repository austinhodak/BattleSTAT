package com.austinh.battlebuddy.viewmodels.models

import com.austinh.battlebuddy.models.PlayerStats

data class PlayerModel(
        var soloStats: PlayerStats? = PlayerStats(),
        var soloFPPStats: PlayerStats? = PlayerStats(),
        var duoStats: PlayerStats? = PlayerStats(),
        var duoFPPStats: PlayerStats? = PlayerStats(),
        var squadStats: PlayerStats? = PlayerStats(),
        var squadFPPStats: PlayerStats? = PlayerStats(),
        var lastUpdated: Long? = null,
        var error: Int? = null
) {
    fun getStatsByGamemode(gamemode: String): PlayerStats? {
        return when (gamemode) {
            "solo" -> soloStats
            "solo-fpp" -> soloFPPStats
            "duo" -> duoStats
            "duo-fpp" -> duoFPPStats
            "squad" -> squadStats
            "squad-fpp" -> squadFPPStats
            else -> null
        }
    }

    fun getMinutesSinceLastUpdated(): Long {
        if (lastUpdated == null) return 0
        return Math.abs(lastUpdated?.minus((System.currentTimeMillis() / 1000))!!) / 60
    }
}