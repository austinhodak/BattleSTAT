package com.respondingio.battlegroundsbuddy.viewmodels.models

data class LeaderboardModel(
        var playerList: MutableList<LeaderboardPlayer> = ArrayList(),
        var gameMode: String
)

data class LeaderboardPlayer(
        var type: String? = null,
        var id: String? = null,
        var attributes: Attributes
)

data class Attributes(
        var name: String,
        var rank: Int,
        var stats: Stats
)

data class Stats(
        var rankPoints: Int = 0,
        var wins: Int = 0,
        var games: Int = 0,
        var winRatio: Double = 0.0,
        var averageDamage: Double = 0.0,
        var kills: Int = 0,
        var killDeathRatio: Double = 0.0,
        var averageRank: Double = 0.0
)