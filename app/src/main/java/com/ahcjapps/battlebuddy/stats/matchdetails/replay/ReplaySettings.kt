package com.ahcjapps.battlebuddy.stats.matchdetails.replay

data class ReplaySettings (
        var elapsedSeconds: Long = 0,
        var speedMultiplier: Int = 1,
        var circleSettings: CircleSettings = CircleSettings(),
        var showCarePackages: Boolean = true
)

data class CircleSettings (
        var showCircle: Boolean = true,
        var showRedZones: Boolean = true,
        var showSafeZones: Boolean = true,
        var showBlueCircle: Boolean= true
)