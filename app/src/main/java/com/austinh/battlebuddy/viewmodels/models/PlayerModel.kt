package com.austinh.battlebuddy.viewmodels.models

import com.austinh.battlebuddy.models.Gamemode
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
    fun getStatsByGamemode(gamemode: Gamemode): PlayerStats? {
        return when (gamemode) {
            Gamemode.SOLO -> soloStats
            Gamemode.SOLOFPP -> soloFPPStats
            Gamemode.DUO -> duoStats
            Gamemode.DUOFPP -> duoFPPStats
            Gamemode.SQUAD -> squadStats
            Gamemode.SQUADFPP -> squadFPPStats
            else -> null
        }
    }

    fun getMinutesSinceLastUpdated(): Long {
        if (lastUpdated == null) return 0
        return Math.abs(lastUpdated?.minus((System.currentTimeMillis() / 1000))!!) / 60
    }
}