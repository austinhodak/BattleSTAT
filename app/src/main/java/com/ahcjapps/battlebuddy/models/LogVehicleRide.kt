package com.ahcjapps.battlebuddy.models

import com.ahcjapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogVehicleRide (
        val character: LogCharacter,
        val vehicle: LogVehicle,
        val seatIndex: Int,
        val _D: String,
        val _T: String
) : TelemetryInterface