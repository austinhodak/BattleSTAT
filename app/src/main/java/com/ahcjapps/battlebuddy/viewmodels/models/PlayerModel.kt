package com.ahcjapps.battlebuddy.viewmodels.models

import com.ahcjapps.battlebuddy.models.Gamemode
import com.ahcjapps.battlebuddy.models.PlayerStats
import com.ahcjapps.battlebuddy.utils.Rank
import java.io.Serializable

data class PlayerModel(
        var soloStats: PlayerStats = PlayerStats(),
        var soloFPPStats: PlayerStats = PlayerStats(),
        var duoStats: PlayerStats = PlayerStats(),
        var duoFPPStats: PlayerStats = PlayerStats(),
        var squadStats: PlayerStats = PlayerStats(),
        var squadFPPStats: PlayerStats = PlayerStats(),
        var lastUpdated: Long? = null,
        var error: Int? = null
) : Serializable {
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

    fun getOverallStats(): PlayerStats {
        val allStats = PlayerStats()
        val pointsList: MutableList<PlayerStats> = ArrayList()

        pointsList.add(soloStats)
        pointsList.add(soloFPPStats)
        pointsList.add(duoStats)
        pointsList.add(duoFPPStats)
        pointsList.add(squadStats)
        pointsList.add(squadFPPStats)

        for (item in pointsList) {
            allStats.assists += item.assists
            allStats.boosts += item.boosts
            allStats.dBNOs += item.dBNOs
            allStats.damageDealt += item.damageDealt
            allStats.days += item.days
            allStats.headshotKills += item.headshotKills
            allStats.heals += item.heals
            allStats.kills += item.kills
            allStats.losses += item.losses
            allStats.revives += item.revives
            allStats.rideDistance += item.rideDistance
            allStats.roadKills += item.roadKills
            allStats.roundsPlayed += item.roundsPlayed
            allStats.suicides += item.suicides
            allStats.teamKills += item.teamKills
            allStats.timeSurvived += item.timeSurvived
            allStats.top10s += item.top10s
            allStats.vehicleDestroys += item.vehicleDestroys
            allStats.walkDistance += item.walkDistance
            allStats.weaponsAcquired += item.weaponsAcquired
            allStats.wins += item.wins
            allStats.swimDistance += item.swimDistance

            allStats.roundMostKills = pointsList.sortedByDescending { it.roundMostKills }[0].roundMostKills
            allStats.longestKill = pointsList.sortedByDescending { it.longestKill }[0].longestKill
            allStats.longestTimeSurvived = pointsList.sortedByDescending { it.longestTimeSurvived }[0].longestTimeSurvived
        }

        return allStats
    }

    fun getHighestRankTitle(): String {
        val pointsList: MutableList<PlayerStats> = ArrayList()

        pointsList.add(soloStats)
        pointsList.add(soloFPPStats)
        pointsList.add(duoStats)
        pointsList.add(duoFPPStats)
        pointsList.add(squadStats)
        pointsList.add(squadFPPStats)

        return pointsList.sortedByDescending { it.getRank().order }[0].rankPointsTitle
    }

    fun getHighestRankLevel(): String {
        val pointsList: MutableList<PlayerStats> = ArrayList()

        pointsList.add(soloStats)
        pointsList.add(soloFPPStats)
        pointsList.add(duoStats)
        pointsList.add(duoFPPStats)
        pointsList.add(squadStats)
        pointsList.add(squadFPPStats)

        return pointsList.sortedByDescending { it.getRank().order }[0].getRankLevel()
    }

    fun getHighestRankPoints(): Double {
        val pointsList: MutableList<PlayerStats> = ArrayList()

        pointsList.add(soloStats)
        pointsList.add(soloFPPStats)
        pointsList.add(duoStats)
        pointsList.add(duoFPPStats)
        pointsList.add(squadStats)
        pointsList.add(squadFPPStats)

        return pointsList.sortedByDescending { it.rankPoints }[0].rankPoints
    }

    fun getHighestBestRankPoints(): Double {
        val pointsList: MutableList<PlayerStats> = ArrayList()

        pointsList.add(soloStats)
        pointsList.add(soloFPPStats)
        pointsList.add(duoStats)
        pointsList.add(duoFPPStats)
        pointsList.add(squadStats)
        pointsList.add(squadFPPStats)

        return pointsList.sortedByDescending { it.bestRankPoint }[0].bestRankPoint
    }

    fun getHighestRank(): Rank {
        val pointsList: MutableList<PlayerStats> = ArrayList()

        pointsList.add(soloStats)
        pointsList.add(soloFPPStats)
        pointsList.add(duoStats)
        pointsList.add(duoFPPStats)
        pointsList.add(squadStats)
        pointsList.add(squadFPPStats)

        return pointsList.sortedByDescending { it.getRank().order }[0].getRank()
    }
}