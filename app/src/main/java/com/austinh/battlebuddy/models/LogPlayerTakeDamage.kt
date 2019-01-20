package com.austinh.battlebuddy.models

data class LogPlayerTakeDamage (
        val attackId: Int,
        var attacker: LogCharacter,
        val victim: LogCharacter,
        val damageTypeCategory: String,
        val damageReason: String,
        val damage: Double,
        val damageCauserName: String,
        val _D: String,
        val _T: String
)