package com.ahcjapps.battlebuddy.models

import com.ahcjapps.battlebuddy.viewmodels.json.TelemetryInterface
import java.io.Serializable

data class LogPlayerKill (
        val attackId: Int,
        val killer: LogCharacter,
        val victim: LogCharacter,
        val assistant: LogCharacter? = null, //PC ONLY
        val damageTypeCategory: String,
        val damageCauserName: String,
        val damageReason: String,
        val distance: Double,
        val victimGameResult: LogGameResult, //PC ONLY
        val _D: String,
        val _T: String
) : Serializable, TelemetryInterface