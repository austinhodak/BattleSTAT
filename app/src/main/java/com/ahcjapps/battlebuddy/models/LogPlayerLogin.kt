package com.ahcjapps.battlebuddy.models

import com.ahcjapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogPlayerLogin (
        val accountId: String,
        val _D: String,
        val _T: String
) : TelemetryInterface