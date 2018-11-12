package com.respondingio.battlegroundsbuddy.utils

object Regions {


    var xboxShardIDs = arrayOf("XBOX-AS", "XBOX-EU", "XBOX-NA", "XBOX-OC", "XBOX-SA")
    var xboxShardNames = arrayOf("Xbox Asia", "Xbox Europe", "Xbox North America", "Xbox Oceania", "Xbox South America")

    var pcShardIDs = arrayOf("PC-KRJP", "PC-JP", "PC-NA", "PC-EU", "PC-RU", "PC-OC", "PC-KAKAO", "PC-SEA", "PC-SA", "PC-AS")
    var pcShardNames = arrayOf("PC Korea", "PC Japan", "PC North America", "PC Europe", "PC Russia", "PC Oceania", "PC Kakao", "PC South East Asia", "PC South and Central America", "PC Asia")
    var pcNewShardNames = arrayOf("Steam", "Kakao")

    fun getRegionNames(shardID: String, seasonID: String?): Array<String> {
        if (shardID.contains("pc", true)) {
            return pcShardNames
            /*when (seasonID) {
                "pc-2018-01" -> {
                    return pcNewShardNames
                }
                "2018-01",
                "2018-02",
                "2018-03",
                "2018-04",
                "2018-05",
                "2018-06",
                "2018-07",
                "2018-08",
                "2018-09" -> {

                }
            }*/
        }

        if (shardID.contains("xbox", true)) {
            return xboxShardNames
        }

        return emptyArray()
    }

    fun getRegionIDs(shardID: String, seasonID: String?): Array<String> {
        if (shardID.contains("pc", true)) {
            return pcShardIDs
            /*when (seasonID) {
                "pc-2018-01" -> {
                    return pcNewShardNames
                }
                "2018-01",
                "2018-02",
                "2018-03",
                "2018-04",
                "2018-05",
                "2018-06",
                "2018-07",
                "2018-08",
                "2018-09" -> {
                    return pcShardIDs
                }
            }*/
        }

        if (shardID.contains("xbox", true)) {
            return xboxShardIDs
        }

        return emptyArray()
    }

    fun getNewRegionID(oldShardID: String): String {
        return if (oldShardID.contains("kakao", true)) {
            "kakao"
        } else if (oldShardID.contains("pc", true)) {
            "steam"
        } else "xbox"
    }
}