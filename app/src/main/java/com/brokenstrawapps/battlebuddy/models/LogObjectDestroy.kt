package com.brokenstrawapps.battlebuddy.models

import com.brokenstrawapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogObjectDestroy (
        val character: LogCharacter,
        val objectType: String,
        val objectLocation: LogLocation,
        val _D: String,
        val _T: String
) : TelemetryInterface