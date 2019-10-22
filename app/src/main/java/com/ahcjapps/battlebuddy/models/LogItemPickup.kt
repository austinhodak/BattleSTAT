package com.ahcjapps.battlebuddy.models

import com.ahcjapps.battlebuddy.viewmodels.json.TelemetryInterface
import java.io.Serializable

data class LogItemPickup (
        val character: LogCharacter,
        val item: LogItem,
        val _D: String,
        val _T: String
) : Serializable, TelemetryInterface