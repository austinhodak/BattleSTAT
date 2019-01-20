package com.austinh.battlebuddy.models

data class LogWheelDestroy (
        val attackId: Int,
        val attacker: LogCharacter,
        val vehicle: LogVehicle,
        val damageTypeCategory: String,
        val damageCauserName: String,
        val _D: String,
        val _T: String
)