package com.ahcjapps.battlebuddy.stats.matchdetails.replay

import com.ahcjapps.battlebuddy.models.LogPlayerPosition
import com.ahcjapps.battlebuddy.models.SafeZoneCircle

data class ReplayDraw (
        var redZoneCircle: SafeZoneCircle? = null,
        var blueZoneCircle: SafeZoneCircle? = null,
        var players: MutableMap<String, LogPlayerPosition>? = null
)