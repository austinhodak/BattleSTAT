package com.brokenstrawapps.battlebuddy.models

data class LogVehicle (
        val vehicleType: String,
        val vehicleId: String,
        val healthPercent: Double,
        val feulPercent: Double,
        val _D: String,
        val _T: String
)