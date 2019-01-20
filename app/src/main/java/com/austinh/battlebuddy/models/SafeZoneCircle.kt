package com.austinh.battlebuddy.models

import java.io.Serializable

data class SafeZoneCircle (
        val position: LogLocation,
        var radius: Double,
        var timeInMatch: Long? = null
) : Serializable