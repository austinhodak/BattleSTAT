package com.respondingio.battlegroundsbuddy.models

data class Character(
        var name: String,
        val teamId: Int,
        val health: Double,
        val location: Location,
        val ranking: Int,
        val accountId: String
)