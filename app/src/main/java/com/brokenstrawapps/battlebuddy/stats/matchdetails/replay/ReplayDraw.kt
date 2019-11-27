package com.brokenstrawapps.battlebuddy.stats.matchdetails.replay

import com.brokenstrawapps.battlebuddy.models.LogPlayerPosition
import com.brokenstrawapps.battlebuddy.models.SafeZoneCircle

data class ReplayDraw (
        var redZoneCircle: SafeZoneCircle? = null,
        var blueZoneCircle: SafeZoneCircle? = null,
        var players: MutableMap<String, LogPlayerPosition>? = null
)