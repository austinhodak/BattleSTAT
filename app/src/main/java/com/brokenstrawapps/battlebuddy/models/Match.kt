package com.brokenstrawapps.battlebuddy.models

import android.text.format.DateUtils
import com.brokenstrawapps.battlebuddy.R
import com.google.firebase.firestore.IgnoreExtraProperties
import java.io.Serializable
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

@IgnoreExtraProperties
data class Match (
        val assetURL: String = "",
        val createdAt: String = "",
        val duration: Int = 0,
        val gameMode: String = "",
        val id: String = "",
        val isCustomMatch: Boolean = false,
        val mapName: String = "",
        val participantCount: Int = 0,
        val participants: HashMap<String, ParticipantShort> = HashMap(),
        val shardId: String = "",
        val titleId: String = "",
        val type: String = ""
) {
    fun getMapIcon(): Int {
        return when (mapName) {
            "Savage_Main" -> R.drawable.ic_palm_tree
            "Erangel_Main" -> R.drawable.ic_tree
            "Desert_Main" -> R.drawable.ic_cactus
            else -> R.drawable.snowflake_white
        }
    }

    fun getFormattedCreatedAt(capitalize: Boolean? = false): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        var time: Long = 0
        try {
            time = sdf.parse(createdAt).time
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val now = System.currentTimeMillis()

        val ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS)

        return if (capitalize == true) {
            com.brokenstrawapps.battlebuddy.models.capitalize(ago as String)
        } else {
            ago as String
        }
    }

    fun getMatchDuration(): String {
        return DateUtils.formatElapsedTime(duration.toLong())
    }

    fun getCreatedAtDate(): Date? {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        try {
            return sdf.parse(createdAt)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return null
    }
}

data class MatchTop (
        var match: Match?,
        var isLoading: Boolean,
        var matchKey: String,
        var currentPlayer: String,
        var createdAt: String,
        var isFavorite: Boolean? = false,
        var isDownloaded: Boolean? = false,
        var season: String
) : Serializable {
    fun getSerializable() : MatchTop {
        val match = this
        match.match = null
        return match
    }

    /**
     * Gets participant data
     * @param playerID (optional) playerID without account.
     * If playerID is not specified, the current players id is used.
     *
     * @return participant object
     */
    fun getPlayerWithID(playerID: String? = null): ParticipantShort? {
        return match?.participants?.values?.find { it.playerId == "account.$currentPlayer" }
    }
}

@IgnoreExtraProperties
data class ParticipantShort (
        val damageDealt: Double = 0.0,
        val id: String = "",
        val kills: Int = 0,
        val name: String = "",
        val playerId: String = "",
        val rideDistance: Double = 0.0,
        val swimDistance: Double = 0.0,
        val walkDistance: Double = 0.0,
        val winPlace: Int = 0,
        val totalDistance: Double = 0.0
)

private fun capitalize(capString: String): String {
    val capBuffer = StringBuffer()
    val capMatcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString)
    while (capMatcher.find()) {
        capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase())
    }

    return capMatcher.appendTail(capBuffer).toString()
}