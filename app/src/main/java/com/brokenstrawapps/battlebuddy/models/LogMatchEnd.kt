package com.brokenstrawapps.battlebuddy.models

import com.brokenstrawapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogMatchEnd (
        val characters: List<LogCharacter>,
        val _D: String,
        val _T: String
) : TelemetryInterface