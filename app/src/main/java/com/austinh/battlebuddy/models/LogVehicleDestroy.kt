package com.austinh.battlebuddy.models

data class LogVehicleDestroy (
        val attackerId: String,
        val attacker: LogCharacter,
        val vehicle: LogVehicle,
        val damageTypeCategory: String,
        val damageCauser: String,
        val distance: Double,
        val _D: String,
        val _T: String
)