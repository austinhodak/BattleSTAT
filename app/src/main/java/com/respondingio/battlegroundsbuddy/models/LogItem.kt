package com.respondingio.battlegroundsbuddy.models

import java.io.Serializable

data class LogItem (
        val itemId: String,
        val stackCount: Int,
        val category: String,
        val subCategory: String,
        val attachedItems: List<String>
) : Serializable