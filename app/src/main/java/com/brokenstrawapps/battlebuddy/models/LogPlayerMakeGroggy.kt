package com.brokenstrawapps.battlebuddy.models

import com.brokenstrawapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogPlayerMakeGroggy (
        val attackId: Int,
        val attacker: LogCharacter,
        val victim: LogCharacter,
        val damageTypeCategory: String,
        val damageCauserName: String,
        val distance: Float,
        val isAttackerInVehicle: Boolean,
        val dBNOId: Int,
        val _D: String,
        val _T: String
) : TelemetryInterface