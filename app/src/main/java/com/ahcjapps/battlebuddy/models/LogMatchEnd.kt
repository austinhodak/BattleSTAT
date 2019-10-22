package com.ahcjapps.battlebuddy.models

import com.ahcjapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogMatchEnd (
        val characters: List<LogCharacter>,
        val _D: String,
        val _T: String
) : TelemetryInterface