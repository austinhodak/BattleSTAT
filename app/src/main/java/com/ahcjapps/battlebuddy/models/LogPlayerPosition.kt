package com.ahcjapps.battlebuddy.models

import com.ahcjapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogPlayerPosition (
        val character: LogCharacter,
        val elapsedTime: Double,
        val numAlivePlayers: Int,
        val _D: String,
        val _T: String
) : TelemetryInterface