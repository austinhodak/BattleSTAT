package com.respondingio.battlegroundsbuddy.models

data class LogPlayerRevive (
        val reviver: LogCharacter,
        val victim: LogCharacter,
        val _D: String,
        val _T: String
)