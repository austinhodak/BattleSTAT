package com.brokenstrawapps.battlebuddy.models

import com.brokenstrawapps.battlebuddy.utils.Rank
import com.brokenstrawapps.battlebuddy.viewmodels.models.PlayerModel
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

        //Remove the dash to get two digit number then sort.

        /*for (item in pointsList) {
            Log.d("RANKL", item.rankPointsTitle + " -- " + item.rankPointsTitle.split("-").toTypedArray().last())
            Log.d("RANKL", RankLevel.valueOf(item.rankPointsTitle.split("-").toTypedArray().last()).order.toString())
        }*/
        //return pointsList.sortedWith(compareBy({ it.getRank().order + RankLevel.valueOf(it.rankPointsTitle.split("-").toTypedArray().last()).order })).last().rankPointsTitle
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