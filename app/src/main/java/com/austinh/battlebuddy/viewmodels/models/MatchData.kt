package com.austinh.battlebuddy.viewmodels.models

import java.io.Serializable

data class MatchData (
        val shardId: String,
        val tags: String,
        val mapName: String,
        val createdAt: String,
        val stats: String,
        val titleId: String,
        val isCustomMatch: Boolean,
        val duration: Long,
        val gameMode: String
) : Serializable