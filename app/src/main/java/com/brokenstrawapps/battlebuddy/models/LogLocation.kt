package com.brokenstrawapps.battlebuddy.models

import java.io.Serializable

data class LogLocation(
        var x: Double,
        var y: Double,
        var z: Double
) : Serializable {
    fun isValidLocation(): Boolean {
        if (x > 0 && y > 0 && z > 0) return true
        return false
    }

    fun isValidCirclePosition(): Boolean {
        if (x > 0 && y > 0) return true
        return false
    }
}