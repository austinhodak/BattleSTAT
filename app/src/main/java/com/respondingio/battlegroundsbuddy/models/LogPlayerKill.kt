package com.respondingio.battlegroundsbuddy.models

data class LogPlayerKill(
        val attackId: Int,
        val killer: Character,
        val victim: Character,
        val damageTypeCategory: String,
        val damageCauserName: String,
        val damageReason: String,
        val distance: Double,
        val _D: String,
        val _T: String
)