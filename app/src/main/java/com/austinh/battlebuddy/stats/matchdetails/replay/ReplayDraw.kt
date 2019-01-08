package com.austinh.battlebuddy.stats.matchdetails.replay

import com.austinh.battlebuddy.models.LogPlayerPosition
import com.austinh.battlebuddy.models.SafeZoneCircle

data class ReplayDraw (
        var redZoneCircle: SafeZoneCircle? = null,
        var blueZoneCircle: SafeZoneCircle? = null,
        var players: MutableMap<String, LogPlayerPosition>? = null
)