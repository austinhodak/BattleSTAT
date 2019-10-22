package com.ahcjapps.battlebuddy.models

import com.ahcjapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogWheelDestroy (
        val attackId: Int,
        val attacker: LogCharacter,
        val vehicle: LogVehicle,
        val damageTypeCategory: String,
        val damageCauserName: String,
        val _D: String,
        val _T: String
) : TelemetryInterface