package com.respondingio.battlegroundsbuddy.models

data class LogVehicleLeave (
        val character: LogCharacter,
        val vehicle: LogVehicle,
        val rideDistance: Double,
        val seatIndex: Int,
        val _D: String,
        val _T: String
)