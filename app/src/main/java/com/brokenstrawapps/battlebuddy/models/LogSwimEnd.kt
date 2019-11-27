package com.brokenstrawapps.battlebuddy.models

import com.brokenstrawapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogSwimEnd (
        val character: LogCharacter,
        val swimDistance: Float,
        val _D: String,
        val _T: String
) : TelemetryInterface