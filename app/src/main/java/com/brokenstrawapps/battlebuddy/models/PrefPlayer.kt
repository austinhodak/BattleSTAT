package com.brokenstrawapps.battlebuddy.models

import com.brokenstrawapps.battlebuddy.utils.Regions
import com.brokenstrawapps.battlebuddy.utils.Seasons
import java.io.Serializable

data class PrefPlayer(
        var playerID: String, //WITHOUT account.
        var playerName: String,
        var defaultShardID: String = "steam",
        var selectedShardID: String = defaultShardID,
        var oldXboxShard: String? = null,
        var selectedGamemode: String? = null,
        var selectedSeason: String? = Seasons.getCurrentSeasonForShard(selectedShardID)
) : Serializable {

    fun isSeasonNewFormat(shardID: String): Boolean {
        val region = Regions.getNewRegion(shardID)
        if (region == Regions.Region.KAKAO || region == Regions.Region.STEAM) {
            return when (selectedSeason) {
                "pc-2018-01" -> true
                "2018-01",
                "2018-02",
                "2018-03",
                "2018-04",
                "2018-05",
                "2018-06",
                "2018-07",
                "2018-08",
                "2018-09" -> false
                else -> true
            }
        } else if (region == Regions.Region.XBOX) {
            return when (selectedSeason) {
                "2018-08",
                "2018-01",
                "2018-02",
                "2018-03",
                "2018-04",
                "2018-05",
                "2018-06",
                "2018-07" -> false
                else -> true
            }
        } else if (region == Regions.Region.PS4) {
            return when (selectedSeason) {
                "2018-09" -> false
                else -> true
            }
        } else {
            return true
        }
    }

    /**
     * Gets the shardID using the new changes
     */

    fun getShardID(shardID: String?): String {
        var shard: String = shardID ?: defaultShardID

        //Return PS4 shard. Possibly might change upon release.
        if (shard.contains("psn", true)) {
            return "psn"
        }

        return ""
    }
}