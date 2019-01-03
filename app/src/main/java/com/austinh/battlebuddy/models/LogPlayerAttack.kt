package com.austinh.battlebuddy.models

data class LogPlayerAttack (
        val attackId: String,
        val attacker: LogCharacter,
        val attackType: String,
        val weapon: LogItem,
        val vehicle: LogVehicle,
        val _D: String,
        val _T: String
)