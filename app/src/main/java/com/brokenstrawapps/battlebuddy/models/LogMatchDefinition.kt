package com.brokenstrawapps.battlebuddy.models

import com.brokenstrawapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogMatchDefinition (
        val MatchId: String,
        val PingQuality: String,
        val _D: String,
        val _T: String
) : TelemetryInterface