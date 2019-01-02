package com.austinh.battlebuddy.utils

object Regions {


    var xboxShardIDs = arrayOf("XBOX-AS", "XBOX-EU", "XBOX-NA", "XBOX-OC", "XBOX-SA")
    var shortXboxShardIDs = arrayOf("as", "eu", "na", "oc", "sa")
    var xboxShardNames = arrayOf("Xbox Asia", "Xbox Europe", "Xbox North America", "Xbox Oceania", "Xbox South America")

    var pcShardIDs = arrayOf("PC-KRJP", "PC-JP", "PC-NA", "PC-EU", "PC-RU", "PC-OC", "PC-SEA", "PC-SA", "PC-AS")
    var pcShardNames = arrayOf("PC Korea", "PC Japan", "PC North America", "PC Europe", "PC Russia", "PC Oceania", "PC South East Asia", "PC South and Central America", "PC Asia")
    var pcNewShardNames = arrayOf("Steam", "Kakao")
    var pcShortShardIDs = arrayOf("krjp", "jp", "na", "eu", "ru", "oc", "sea", "sa", "as")

    var psnShardNames = arrayOf("PSN Asia", "PSN Europe", "PSN North America", "PSN Oceania")
    var psnShardIDs = arrayOf("PSN-AS", "PSN-EU", "PSN-NA", "PSN-OC")
    var shortPSNShardIDs = arrayOf("as", "eu", "na", "oc")

    fun getRegionNames(shardID: String, seasonID: String?): Array<String> {
        if (getNewRegion(shardID) == Region.STEAM || getNewRegion(shardID) == Region.KAKAO) {
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

        if (shardID.contains("psn", true)) {
            return psnShardNames
        }

        return emptyArray()
    }

    fun getRegionNames(platform: Platform): MutableList<String> {
        return when (platform) {
            Platform.KAKAO,
            Platform.STEAM -> pcShardNames
            Platform.XBOX -> xboxShardNames
            Platform.PS4 -> psnShardNames
        }.toMutableList()
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

        if (shardID.contains("psn", true)) {
            return psnShardIDs
        }

        return emptyArray()
    }

    fun getNewRegionID(oldShardID: String): String {
        return if (oldShardID.contains("kakao", true)) {
            "kakao"
        } else if (oldShardID.contains("pc", true) || oldShardID.contains("steam", true)) {
            "steam"
        } else "xbox"
    }

    fun getNewRegionName(shardID: String): String {
        return when {
            shardID.contains("kakao", true) || shardID.equals("kakao", true) -> "Kakao"
            shardID.contains("pc", true) || shardID.equals("steam", true) -> "Steam"
            shardID.contains("xbox", true) || shardID.equals("xbox", true) -> "Xbox"
            shardID.contains("psn", true) || shardID.equals("psn", true) -> "PS4"
            else -> "Unknown"
        }
    }

    fun getNewRegion(shardID: String): Region {
        return if (shardID.contains("kakao", true)) {
            Region.KAKAO
        } else if (shardID.contains("pc", true) || shardID.contains("steam", true)) {
            Region.STEAM
        } else if (shardID.contains("xbox", true)) {
            Region.XBOX
        } else {
            Region.PS4
        }
    }

    fun getShortRegionIDs(platform: Platform): Array<String> {
        return when (platform) {
            Platform.KAKAO,
            Platform.STEAM -> pcShortShardIDs
            Platform.XBOX -> shortXboxShardIDs
            Platform.PS4 -> shortPSNShardIDs
        }
    }

    enum class Region {
        STEAM,
        KAKAO,
        XBOX,
        PS4
    }


}