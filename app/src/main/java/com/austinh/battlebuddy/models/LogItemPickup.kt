package com.austinh.battlebuddy.models

import java.io.Serializable

data class LogItemPickup (
        val character: LogCharacter,
        val item: LogItem,
        val _D: String,
        val _T: String
) : Serializable