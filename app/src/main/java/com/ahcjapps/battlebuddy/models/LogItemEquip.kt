package com.ahcjapps.battlebuddy.models

import com.ahcjapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogItemEquip (
        val character: LogCharacter,
        val item: LogItem,
        val _D: String,
        val _T: String
) : TelemetryInterface