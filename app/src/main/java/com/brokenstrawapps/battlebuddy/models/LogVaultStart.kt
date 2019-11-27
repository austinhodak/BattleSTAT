package com.brokenstrawapps.battlebuddy.models

import com.brokenstrawapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogVaultStart (
        var character: LogCharacter,
        val _D: String,
        val _T: String
) : TelemetryInterface