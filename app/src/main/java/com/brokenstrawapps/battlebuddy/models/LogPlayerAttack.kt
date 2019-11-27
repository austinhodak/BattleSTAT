package com.brokenstrawapps.battlebuddy.models

import com.brokenstrawapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogPlayerAttack (
        val attackId: String,
        val attacker: LogCharacter,
        val attackType: String,
        val weapon: LogItem,
        val vehicle: LogVehicle,
        val _D: String,
        val _T: String
) : TelemetryInterface