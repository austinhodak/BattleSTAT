package com.brokenstrawapps.battlebuddy.models

import com.brokenstrawapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogPlayerCreate (
        val character: LogCharacter,
        val _D: String,
        val _T: String
) : TelemetryInterface