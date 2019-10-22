package com.ahcjapps.battlebuddy.models

import com.ahcjapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogParachuteLanding (
        val character: LogCharacter,
        val distance: Double,
        val _D: String,
        val _T: String
) : TelemetryInterface