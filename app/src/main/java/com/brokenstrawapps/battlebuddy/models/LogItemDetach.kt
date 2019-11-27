package com.brokenstrawapps.battlebuddy.models

import com.brokenstrawapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogItemDetach (
        val character: LogCharacter,
        val parentItem: LogItem,
        val childItem: LogItem,
        val _D: String,
        val _T: String
) : TelemetryInterface