package com.austinh.battlebuddy.models

data class LogGamestate (
        val elapsedTime: Int,
        val numAliveTeams: Int,
        val numJoinPlayers: Int,
        val numStartPlayers: Int,
        val safetyZonePosition: LogLocation,
        val safetyZoneRadius: Int,
        val poisonGasWarningPosition: LogLocation,
        val poisonGasWarningRadius: Int,
        val redZonePosition: LogLocation,
        val redZoneRadius: Int
)

data class LogGamestatePeriodic (
        val gameState: LogGamestate
)