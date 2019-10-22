package com.ahcjapps.battlebuddy.models

import com.ahcjapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogVaultStart (
        var character: LogCharacter,
        val _D: String,
        val _T: String
) : TelemetryInterface