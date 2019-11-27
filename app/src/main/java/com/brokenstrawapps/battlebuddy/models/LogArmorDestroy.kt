package com.brokenstrawapps.battlebuddy.models

import com.brokenstrawapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogArmorDestroy (
        val attackId: Int,
        val attacker: LogCharacter,
        val victim: LogCharacter,
        val damageTypeCategory: String,
        val damageReason: String,
        val damageCauserName: String,
        val item: LogItem,
        val distance: Double,
        val _D: String,
        val _T: String
) : TelemetryInterface