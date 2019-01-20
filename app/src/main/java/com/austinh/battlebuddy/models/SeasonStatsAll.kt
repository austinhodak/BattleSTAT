package com.austinh.battlebuddy.models

import com.austinh.battlebuddy.utils.Rank
import com.austinh.battlebuddy.viewmodels.models.PlayerModel
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class SeasonStatsAll(
        val duo: PlayerStats = PlayerStats(),
        val `duo-fpp`: PlayerStats = PlayerStats(),
        val solo: PlayerStats = PlayerStats(),
        val `solo-fpp`: PlayerStats = PlayerStats(),
        val squad: PlayerStats = PlayerStats(),
        val `squad-fpp`: PlayerStats = PlayerStats(),
        var highRank: Rank? = null
) {

    init {
        val pointsList: MutableList<PlayerStats> = ArrayList()

        pointsList.add(solo)
        pointsList.add(`solo-fpp`)
        pointsList.add(duo)
        pointsList.add(`duo-fpp`)
        pointsList.add(squad)
        pointsList.add(`squad-fpp`)

        highRank = pointsList.sortedByDescending { it.getRank().order }[0].getRank()
    }

    fun getHighestRankTitle(): String {
        val pointsList: MutableList<PlayerStats> = ArrayList()

        pointsList.add(solo)
        pointsList.add(`solo-fpp`)
        pointsList.add(duo)
        pointsList.add(`duo-fpp`)
        pointsList.add(squad)
        pointsList.add(`squad-fpp`)

        return pointsList.sortedByDescending { it.getRank().order }[0].rankPointsTitle
    }

    fun getHighestRankLevel(): String {
        val pointsList: MutableList<PlayerStats> = ArrayList()

        pointsList.add(solo)
        pointsList.add(`solo-fpp`)
        pointsList.add(duo)
        pointsList.add(`duo-fpp`)
        pointsList.add(squad)
        pointsList.add(`squad-fpp`)

        return pointsList.sortedByDescending { it.getRank().order }[0].getRankLevel()
    }

    fun getHighestRank(): Rank {
        val pointsList: MutableList<PlayerStats> = ArrayList()

        pointsList.add(solo)
        pointsList.add(`solo-fpp`)
        pointsList.add(duo)
        pointsList.add(`duo-fpp`)
        pointsList.add(squad)
        pointsList.add(`squad-fpp`)

        return pointsList.sortedByDescending { it.getRank().order }[0].getRank()
    }
    
    fun getPlayerModel(): PlayerModel {
        return PlayerModel (
                solo,
                `solo-fpp`,
                duo,
                `duo-fpp`,
                squad,
                `squad-fpp`
        )
    }
}