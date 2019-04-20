package com.ahcjapps.battlebuddy.models

import java.io.Serializable

data class LogGameResult (
        val rank: Int,
        val gameResult: String,
        val teamId: Int,
        val stats: LogStats,
        val accountId: String,
        val _D: String,
        val _T: String
) : Serializable