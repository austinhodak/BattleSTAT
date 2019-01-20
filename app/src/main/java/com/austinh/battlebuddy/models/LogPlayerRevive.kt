package com.austinh.battlebuddy.models

data class LogPlayerRevive (
        val reviver: LogCharacter,
        val victim: LogCharacter,
        val _D: String,
        val _T: String
)