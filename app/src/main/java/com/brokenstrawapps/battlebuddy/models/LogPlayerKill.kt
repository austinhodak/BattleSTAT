package com.brokenstrawapps.battlebuddy.models

import com.brokenstrawapps.battlebuddy.viewmodels.json.TelemetryInterface
import java.io.Serializable

data class LogPlayerKill (
        val attackId: Int,
        val killer: LogCharacter? = null,
        val victim: LogCharacter,
        val assistant: LogCharacter? = null,
        val damageTypeCategory: String,
        val damageCauserName: String,
        val damageReason: String,
        val distance: Double,
        val victimGameResult: LogGameResult,
        val _D: String,
        val _T: String
) : Serializable, TelemetryInterface