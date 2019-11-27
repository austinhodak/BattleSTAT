package com.brokenstrawapps.battlebuddy.models

import java.io.Serializable

data class LogStats (
        val killCount: Int,
        val distanceOnFoot: Double,
        val distanceOnSwim: Double,
        val distanceOnVehicle: Double,
        val distanceOnParachute: Double,
        val distanceOnFreeFall: Double,
        val _D: String,
        val _T: String
) : Serializable