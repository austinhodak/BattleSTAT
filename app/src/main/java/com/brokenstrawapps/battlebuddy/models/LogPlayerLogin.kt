package com.brokenstrawapps.battlebuddy.models

import com.brokenstrawapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogPlayerLogin (
        val accountId: String,
        val _D: String,
        val _T: String
) : TelemetryInterface