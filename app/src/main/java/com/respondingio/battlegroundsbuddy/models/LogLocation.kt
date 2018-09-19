package com.respondingio.battlegroundsbuddy.models

import java.io.Serializable

data class LogLocation(
        val x: Double,
        val y: Double,
        val z: Double
) : Serializable {
    fun isValidLocation(): Boolean {
        if (x > 0 && y > 0 && z > 0) return true
        return false
    }
}