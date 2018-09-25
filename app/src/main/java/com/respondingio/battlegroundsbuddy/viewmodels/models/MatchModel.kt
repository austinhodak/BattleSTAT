package com.respondingio.battlegroundsbuddy.viewmodels.models

import android.text.format.DateUtils
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.models.LogItemPickup
import com.respondingio.battlegroundsbuddy.models.LogPlayerKill
import com.respondingio.battlegroundsbuddy.models.MatchParticipant
import com.respondingio.battlegroundsbuddy.models.MatchRoster
import java.io.Serializable
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.HashMap
import java.util.TimeZone

data class MatchModel(
        var error: String? = null,
        var participantList: ArrayList<MatchParticipant> = ArrayList(),
        var rosterList: ArrayList<MatchRoster> = ArrayList(),
        var participantHash: HashMap<String, MatchParticipant> = HashMap(),
        var attributes: MatchData? = null,
        var currentPlayerRoster: MatchRoster? = null,
        var currentPlayer: MatchParticipant? = null,
        var killFeedList: ArrayList<LogPlayerKill> = ArrayList(),
        var currentPlayerID: String,
        var currentPlayerMatchID: String = "",
        var logItemPickup: ArrayList<LogItemPickup> = ArrayList()
) : Serializable {

    fun getMapIcon(): Int {
        return when (attributes?.mapName) {
            "Savage_Main" -> R.drawable.sanhok_icon
            "Erangel_Main" -> R.drawable.erangel_icon
            "Desert_Main" -> R.drawable.cactu
            else -> R.drawable.erangel_icon
        }
    }

    fun getMapName(): String {
        return when (attributes?.mapName) {
            "Savage_Main" -> "Sanhok"
            "Erangel_Main" -> "Erangel"
            "Desert_Main" -> "Miramar"
            else -> ""
        }
    }

    fun getMapAsset(): String {
        return when (attributes?.mapName) {
            "Savage_Main" -> "sanhok/Savage_Main_Low_Res.jpg"
            "Erangel_Main" -> "erangel/Erangel_Main.jpg"
            "Desert_Main" -> "miramar/Miramar_Main_High_Res.jpg"
            else -> ""
        }
    }

    fun getFormattedCreatedAt(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        var time: Long = 0
        try {
            time = sdf.parse(attributes?.createdAt).time
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val offset = TimeZone.getDefault().rawOffset + TimeZone.getDefault().dstSavings
        val now = System.currentTimeMillis()

        val ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS)

        return ago as String
    }

    fun getPlayerByAccountID(accountID: String) : MatchParticipant? {
        for (player in participantList) {
            if (player.attributes.stats.playerId == accountID) {
                return player
            }
        }
        return null
    }
}