package com.respondingio.battlegroundsbuddy.models

import java.io.Serializable

data class Character(
        var name: String,
        val teamId: Int,
        val health: Double,
        val location: Location,
        val ranking: Int,
        val accountId: String
) : Serializable