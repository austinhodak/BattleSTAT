package com.respondingio.battlegroundsbuddy.models

import android.text.format.DateUtils
import com.google.firebase.firestore.IgnoreExtraProperties
import com.respondingio.battlegroundsbuddy.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

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
        when (mapName) {
            "Savage_Main" -> return R.drawable.sanhok_icon
            "Erangel_Main" -> return R.drawable.erangel_icon
            "Desert_Main" -> return R.drawable.cactu
            else -> return R.drawable.erangel_icon
        }
    }

    fun getFormattedCreatedAt(): String {
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

        return ago as String
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
        var createdAt: String
)

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