package com.ahcjapps.battlebuddy.models

import com.ahcjapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogItemDrop (
        val character: LogCharacter,
        val item: LogItem,
        val _D: String,
        val _T: String
) : TelemetryInterface