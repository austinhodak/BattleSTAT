package com.respondingio.battlegroundsbuddy.models

import java.io.Serializable

data class PrefPlayer(
        var playerID: String, //WITHOUT account.
        var playerName: String,
        var defaultShardID: String = "XBOX-AS",
        var selectedShardID: String = defaultShardID,
        var selectedGamemode: String? = null,
        var selectedSeason: String? = null
) : Serializable