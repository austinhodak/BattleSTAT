package com.ahcjapps.battlebuddy.models

import com.ahcjapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogSwimEnd (
        val character: LogCharacter,
        val swimDistance: Float,
        val _D: String,
        val _T: String
) : TelemetryInterface