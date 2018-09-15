package com.respondingio.battlegroundsbuddy.models

data class LogItem (
        val itemId: String,
        val stackCount: Int,
        val category: String,
        val subCategory: String,
        val attachedItems: List<String>
)