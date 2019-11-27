package com.brokenstrawapps.battlebuddy.models

import com.brokenstrawapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogVehicleDestroy (
        val attackerId: String,
        val attacker: LogCharacter,
        val vehicle: LogVehicle,
        val damageTypeCategory: String,
        val damageCauser: String,
        val distance: Double,
        val _D: String,
        val _T: String
) : TelemetryInterface