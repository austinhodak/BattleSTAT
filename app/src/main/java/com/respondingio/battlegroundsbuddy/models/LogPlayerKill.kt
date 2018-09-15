package com.respondingio.battlegroundsbuddy.models

import java.io.Serializable

data class LogPlayerKill(
        val attackId: Int,
        val killer: LogCharacter,
        val victim: LogCharacter,
        val damageTypeCategory: String,
        val damageCauserName: String,
        val damageReason: String,
        val distance: Double,
        val _D: String,
        val _T: String
) : Serializable