package com.brokenstrawapps.battlebuddy.models

import com.brokenstrawapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogVehicleLeave (
        val character: LogCharacter,
        val vehicle: LogVehicle,
        val rideDistance: Double,
        val seatIndex: Int,
        val _D: String,
        val _T: String
) : TelemetryInterface