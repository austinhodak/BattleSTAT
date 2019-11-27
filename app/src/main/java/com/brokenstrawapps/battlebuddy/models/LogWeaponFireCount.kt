package com.brokenstrawapps.battlebuddy.models

import com.brokenstrawapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogWeaponFireCount (
        var character: LogCharacter,
        var weaponId: String,
        var fireCount: Int,
        val _D: String,
        val _T: String
) : TelemetryInterface