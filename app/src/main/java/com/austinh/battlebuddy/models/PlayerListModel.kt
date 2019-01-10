package com.austinh.battlebuddy.models

import android.util.Log
import com.austinh.battlebuddy.utils.Platform
import com.austinh.battlebuddy.utils.Seasons
import com.google.android.gms.tasks.Task
import com.google.firebase.functions.FirebaseFunctions
import java.io.Serializable
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.set

data class PlayerListModel(
        var playerID: String, //WITHOUT account. prefix
        var playerIDAccount: String? = null, // WITH account. prefix
        var playerName: String? = null,
        var platform: Platform,
        var defaultConsoleRegion: String? = null,
        var selectedSeason: Seasons.Season = Seasons.getCurrentSeasonForPlatform(platform),
        var selectedRegion: String = defaultConsoleRegion ?: "na",
        var defaultGamemode: Gamemode = Gamemode.SOLO,
        var selectedGamemode: Gamemode = defaultGamemode,
        var isPlayerCurrentUser: Boolean = false,
        var selectedMatchModes: MatchModes = MatchModes.NORMAL,
        var isLifetimeSelected: Boolean = false
) : Serializable {
    fun getDatabaseSearchURL() : String {
        if (defaultConsoleRegion.isNullOrEmpty()) defaultConsoleRegion = "na"

        when (platform) {
            Platform.STEAM -> return if (Seasons.isSeasonNewFormat(platform, selectedSeason)) {
                "steam"
            } else {
                "pc-$selectedRegion"
            }
            Platform.KAKAO -> return if (Seasons.isSeasonNewFormat(platform, selectedSeason)) {
                "kakao"
            } else {
                "pc-kakao"
            }
            Platform.XBOX -> return if (Seasons.isSeasonNewFormat(platform, selectedSeason)) {
                "xbox"
            } else {
                "xbox-$selectedRegion"
            }
            Platform.PS4 -> return if (Seasons.isSeasonNewFormat(platform, selectedSeason)) {
                "psn"
            } else {
                "psn-$selectedRegion"
            }
            else -> return ""
        }
    }

    /**
     * @return boolean true if console player
     */
    fun isConsolePlayer() : Boolean {
        return platform == Platform.XBOX || platform == Platform.PS4
    }


    /**
     * @return String for searching match list for different modes
     */
    fun getGamemodeSearch() : String {
        if (selectedMatchModes == MatchModes.EVENT) {
            return "EVENT"
        }

        if (selectedMatchModes == MatchModes.CUSTOM) {
            return "CUSTOM"
        }

        return selectedGamemode.id.toUpperCase()
    }

    /**
     * Calls the Function to get last 14 days of matches from API
     */
    fun runGetMatches(): Task<Map<String, Any>> {
        val data = HashMap<String, Any>()
        data["platformID"] = this.platform.id.toLowerCase()
        data["playerID"] = this.playerID
        data["seasonID"] = this.selectedSeason.codeString

        return FirebaseFunctions.getInstance().getHttpsCallable("getPlayerMatches").call(data).continueWith { task ->
            val result = task.result?.data as Map<String, Any>
            Log.d("REQUEST", result.toString())
            result
        }
    }

    /**
     * Calls the Function to get season stats
     */
    fun runGetStats(): Task<Map<String, Any>> {
        val data = java.util.HashMap<String, Any>()
        data["playerID"] = this.playerID
        data["shardID"] = this.getDatabaseSearchURL()
        data["seasonID"] = if (isLifetimeSelected) {
            "lifetime"
        } else {
            this.selectedSeason.codeString
        }
        data["platform"] = this.platform.id

        Log.d("GETSTATS", data.toString())

        return FirebaseFunctions.getInstance().getHttpsCallable("getPlayerSeasonStats").call(data).continueWith { task ->
            val result = task.result?.data as Map<String, Any>
            Log.d("REQUEST", result.toString())
            result
        }
    }

    /**
     * Gets all the players stats, including season and matches
     */
    fun getAllStats(): List<Task<Map<String, Any>>> {
        val list: MutableList<Task<Map<String, Any>>> = ArrayList()

        list.add(runGetStats())
        list.add(runGetMatches())

        return list
    }
}