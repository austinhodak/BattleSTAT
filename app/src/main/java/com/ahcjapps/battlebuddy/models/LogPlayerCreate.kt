package com.ahcjapps.battlebuddy.models

import com.ahcjapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogPlayerCreate (
        val character: LogCharacter,
        val _D: String,
        val _T: String
) : TelemetryInterface