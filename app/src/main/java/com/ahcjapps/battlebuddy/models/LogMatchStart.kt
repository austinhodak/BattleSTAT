package com.ahcjapps.battlebuddy.models

import com.ahcjapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogMatchStart (
        val mapName: String,
        val weatherId: String,
        val characters: List<LogCharacter>,
        val cameraViewBehavior: String,
        val teamSize: Int,
        val isCustomGame: Boolean,
        val isEventMode: Boolean,
        val blueZoneCustomOptions: String,
        val _D: String,
        val _T: String
) : TelemetryInterface