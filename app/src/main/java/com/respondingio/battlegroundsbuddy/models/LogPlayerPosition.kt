package com.respondingio.battlegroundsbuddy.models

data class LogPlayerPosition (
        val character: LogCharacter,
        val elapsedTime: Double,
        val numAlivePlayers: Int,
        val _D: String,
        val _T: String
)