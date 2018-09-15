package com.respondingio.battlegroundsbuddy.models

data class LogItemAttach (
        val character: LogCharacter,
        val parentItem: LogItem,
        val childItem: LogItem,
        val _D: String,
        val _T: String
)