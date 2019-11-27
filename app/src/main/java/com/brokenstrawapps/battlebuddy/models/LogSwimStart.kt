package com.brokenstrawapps.battlebuddy.models

import com.brokenstrawapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogSwimStart (
        val character: LogCharacter,
        val swimDistance: Float,
        val _D: String,
        val _T: String
) : TelemetryInterface