package com.austinh.battlebuddy.models

data class LogGamestate (
        val elapsedTime: Int,
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
        val gameState: LogGamestate
)