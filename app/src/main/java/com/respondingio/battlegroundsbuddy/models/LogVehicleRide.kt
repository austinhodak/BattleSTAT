package com.respondingio.battlegroundsbuddy.models

data class LogVehicleRide (
        val character: LogCharacter,
        val vehicle: LogVehicle,
        val seatIndex: Int,
        val _D: String,
        val _T: String
)