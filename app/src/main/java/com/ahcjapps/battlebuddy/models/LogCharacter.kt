package com.ahcjapps.battlebuddy.models

import java.io.Serializable

data class LogCharacter (
        var name: String = "",
        val teamId: Int = 0,
        val health: Double = 0.0,
        val location: LogLocation = LogLocation(0.0, 0.0, 0.0),
        val ranking: Int = 0,
        val accountId: String = "",
        val isInBluezone: Boolean = false,
        val isInRedZone: Boolean = false,
        val zone: List<String> = ArrayList(),
        var color: Int? = null
) : Serializable