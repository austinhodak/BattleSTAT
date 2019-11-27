package com.brokenstrawapps.battlebuddy.models

import com.brokenstrawapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogPlayerRevive (
        val reviver: LogCharacter,
        val victim: LogCharacter,
        val _D: String,
        val _T: String
) : TelemetryInterface