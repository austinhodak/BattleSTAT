package com.brokenstrawapps.battlebuddy.models

import com.brokenstrawapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogHeal (
        val character: LogCharacter,
        val _D: String,
        val _T: String,
        var item: LogItem,
        var healAmount: Double
) : TelemetryInterface