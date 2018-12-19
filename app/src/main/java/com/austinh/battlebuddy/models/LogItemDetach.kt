package com.austinh.battlebuddy.models

data class LogItemDetach (
        val character: LogCharacter,
        val parentItem: LogItem,
        val childItem: LogItem,
        val _D: String,
        val _T: String
)