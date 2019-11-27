package com.brokenstrawapps.battlebuddy.models

import com.brokenstrawapps.battlebuddy.viewmodels.json.TelemetryInterface

data class LogPlayerLogout (
        val accountId: String,
        val _D: String,
        val _T: String
) : TelemetryInterface