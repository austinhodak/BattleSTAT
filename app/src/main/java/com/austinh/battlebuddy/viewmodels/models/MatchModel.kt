package com.austinh.battlebuddy.viewmodels.models

import android.content.Context
import android.graphics.Color
import android.text.format.DateUtils
import com.austinh.battlebuddy.R
import com.austinh.battlebuddy.map.Map
import com.austinh.battlebuddy.models.*
import java.io.File
import java.io.Serializable
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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
        var logItemPickup: ArrayList<LogItemPickup> = ArrayList(),
        var logPlayerTakeDamage: ArrayList<LogPlayerTakeDamage> = ArrayList(),
        var logPlayerAttack: ArrayList<LogPlayerAttack> = ArrayList(),
        var matchDefinition: LogMatchDefinition? = null,
        var carePackageList: ArrayList<LogCarePackageLand> = ArrayList(),
        var safeZoneList: ArrayList<SafeZoneCircle> = ArrayList(),
        var redZoneList: ArrayList<SafeZoneCircle> = ArrayList(),
        var gameStates: ArrayList<LogGamestatePeriodic> = ArrayList(),
        var logPlayerPositions: ArrayList<LogPlayerPosition> = ArrayList(),
        var teamColors: HashMap<Int, Int> = HashMap(),
        var logPlayerCreate: ArrayList<LogCharacter> = ArrayList()
) : Serializable {

    fun getMapIcon(): Int {
        return when (attributes?.mapName) {
            "Savage_Main" -> R.drawable.sanhok_icon
            "Erangel_Main" -> R.drawable.erangel_icon
            "Desert_Main" -> R.drawable.cactu
            else -> R.drawable.snowflake
        }
    }

    fun getMapAsset(context: Context): String {
        return when (attributes?.mapName) {
            "Savage_Main" -> {
                if (File(context.filesDir, Map.SANHOK_HIGH.fileName).exists()) {
                    Map.SANHOK_HIGH.fileName
                } else {
                    Map.SANHOK_LOW.fileName
                }
            }
            "Erangel_Main" -> {
                if (File(context.filesDir, Map.ERANGEL_HIGH.fileName).exists()) {
                    Map.ERANGEL_HIGH.fileName
                } else {
                    Map.ERANGEL_LOW.fileName
                }
            }
            "Desert_Main" -> {
                if (File(context.filesDir, Map.MIRAMAR_HIGH.fileName).exists()) {
                    Map.MIRAMAR_HIGH.fileName
                } else {
                    Map.MIRAMAR_LOW.fileName
                }
            }
            else -> {
                if (File(context.filesDir, Map.VIKENDI_HIGH.fileName).exists()) {
                    Map.VIKENDI_HIGH.fileName
                } else {
                    Map.VIKENDI_LOW.fileName
                }
            }
        }
    }

    fun getFormattedCreatedAt(): String {
        if (attributes?.createdAt == null) return "Unknown Time"
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

    fun randomizeTeamColors() {
        for (team in rosterList) {
            teamColors[team.attributes.stats.teamId] = getRandomColor()
        }
    }

    private fun getRandomColor(): Int {
        val rnd = Random()
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }
}