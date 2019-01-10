package com.austinh.battlebuddy.models

import java.text.SimpleDateFormat
import java.util.*

data class LogGamestate (
        var elapsedTime: Long,
        val numAliveTeams: Int,
        val numJoinPlayers: Int,
        val numStartPlayers: Int,
        val safetyZonePosition: LogLocation,
        val safetyZoneRadius: Double,
        val poisonGasWarningPosition: LogLocation,
        val poisonGasWarningRadius: Double,
        val redZonePosition: LogLocation,
        val redZoneRadius: Double
)

data class LogGamestatePeriodic (
        val gameState: LogGamestate,
        val _D: String,
        val _T: String,
        var matchTime: String,
        var common: Common
) {
    fun getRedzoneCircle(): SafeZoneCircle {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        sdf.timeZone = TimeZone.getTimeZone("GMT")

        val sdf2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf2.timeZone = TimeZone.getTimeZone("GMT")

        val matchStartDate = sdf.parse(matchTime)
        val killTime = sdf2.parse(_D)

        val difference = killTime.time - matchStartDate.time

        val circle = SafeZoneCircle (
                position = gameState.redZonePosition,
                radius = gameState.redZoneRadius,
                timeInMatch = difference / 1000
        )

        return circle
    }
}