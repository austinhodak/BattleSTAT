package com.ahcjapps.battlebuddy.models

import com.ahcjapps.battlebuddy.viewmodels.json.TelemetryInterface
import java.io.Serializable

data class LogHeal (
        val character: LogCharacter,
        val _D: String,
        val _T: String,
        var item: LogItem,
        var healAmount: Double
) : TelemetryInterface