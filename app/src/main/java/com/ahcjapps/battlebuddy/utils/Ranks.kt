package com.ahcjapps.battlebuddy.utils

import com.ahcjapps.battlebuddy.R

object Ranks {

    fun getRankIcon(rank: Double): Int {
        val rankPoints = Math.floor(rank).toInt()

        return when {
            rankPoints == 0 -> R.drawable.unranked
            rankPoints in 1..1399 -> R.drawable.rank_icon_bronze
            rankPoints in 1400..1499 -> R.drawable.rank_icon_silver
            rankPoints in 1500..1599 -> R.drawable.rank_icon_gold
            rankPoints in 1600..1699 -> R.drawable.rank_icon_platinum
            rankPoints in 1700..1799 -> R.drawable.rank_icon_diamond
            rankPoints in 1800..1899 -> R.drawable.rank_icon_elite
            rankPoints in 1900..1999 -> R.drawable.rank_icon_master
            rankPoints >= 2000 -> R.drawable.rank_icon_grandmaster
            else -> R.drawable.unranked
        }
    }

    fun getRankIcon(rank: Rank): Int {
        return when (rank) {
            Rank.UNKNOWN -> R.drawable.unranked
            Rank.BEGINNER -> R.drawable.rank_icon_bronze
            Rank.NOVICE -> R.drawable.rank_icon_silver
            Rank.EXPERIENCED -> R.drawable.rank_icon_gold
            Rank.SKILLED -> R.drawable.rank_icon_platinum
            Rank.SPECIALIST -> R.drawable.rank_icon_diamond
            Rank.EXPERT -> R.drawable.rank_icon_elite
            Rank.SURVIVOR -> R.drawable.rank_icon_master
            Rank.LONE_SURVIVOR -> R.drawable.rank_icon_grandmaster
        }
    }

    fun getRankColor(rank: Double): Int {
        val rankPoints = Math.floor(rank).toInt()

        return when {
            rankPoints == 0 -> R.color.timelineGrey
            rankPoints in 1..1399 -> R.color.rankBronze
            rankPoints in 1400..1499 -> R.color.timelineNavy
            rankPoints in 1500..1599 -> R.color.rankGold
            rankPoints in 1600..1699 -> R.color.rankPlatinum
            rankPoints in 1700..1799 -> R.color.rankDiamond
            rankPoints in 1800..1899 -> R.color.rankElite
            rankPoints in 1900..1999 -> R.color.rankMaster
            rankPoints >= 2000 -> R.color.timelineYellow
            else -> R.color.timelineGrey
        }
    }

    fun getRankColor(rank: Rank): Int {
        return when (rank) {
            Rank.UNKNOWN -> R.color.timelineGrey
            Rank.BEGINNER -> R.color.rankBronze
            Rank.NOVICE -> R.color.timelineNavy
            Rank.EXPERIENCED -> R.color.rankGold
            Rank.SKILLED -> R.color.rankPlatinum
            Rank.SPECIALIST -> R.color.rankDiamond
            Rank.EXPERT -> R.color.rankElite
            Rank.SURVIVOR -> R.color.rankMaster
            Rank.LONE_SURVIVOR -> R.color.timelineYellow
        }
    }

    fun getRankTitle(rank: Double): String {
        val rankPoints = Math.floor(rank).toInt()

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

    fun getRankBy(string: String) : Rank {
        val rankNum = string.split("-")[0]

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

    fun getRankLevel(roman: Boolean = true, rank: String) : String {
        if (rank.isEmpty() || rank == "7-0" || rank == "6-0") return ""
        val rankLevel = rank.split("-").toTypedArray()[1].toInt()

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
}