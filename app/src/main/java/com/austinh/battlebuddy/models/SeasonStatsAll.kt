package com.austinh.battlebuddy.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class SeasonStatsAll(
        val duo: PlayerStats = PlayerStats(),
        val `duo-fpp`: PlayerStats = PlayerStats(),
        val solo: PlayerStats = PlayerStats(),
        val `solo-fpp`: PlayerStats = PlayerStats(),
        val squad: PlayerStats = PlayerStats(),
        val `squad-fpp`: PlayerStats = PlayerStats()
)