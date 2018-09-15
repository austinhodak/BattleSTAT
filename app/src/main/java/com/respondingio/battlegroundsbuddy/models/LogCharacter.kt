package com.respondingio.battlegroundsbuddy.models

import java.io.Serializable

data class LogCharacter(
        var name: String,
        val teamId: Int,
        val health: Double,
        val location: LogLocation,
        val ranking: Int,
        val accountId: String
) : Serializable