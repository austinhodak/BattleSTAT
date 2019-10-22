package com.ahcjapps.battlebuddy.models

import com.ahcjapps.battlebuddy.viewmodels.json.TelemetryInterface

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
) : TelemetryInterface