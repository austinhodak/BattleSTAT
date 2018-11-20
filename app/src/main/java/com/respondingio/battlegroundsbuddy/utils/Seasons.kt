package com.respondingio.battlegroundsbuddy.utils

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

object Seasons {

    var mDatabase: FirebaseDatabase? = null

    val pcSeasons = HashMap<String, Boolean>()
    val xboxSeasons = HashMap<String, Boolean>()
    val ps4Seasons = HashMap<String, Boolean>()
    var pcCurrentSeason: String = "pc-2018-01"
    var xboxCurrentSeason: String = "2018-08"
    var ps4CurrentSeason: String = "2018-08"

    fun init() {
        mDatabase = FirebaseDatabase.getInstance()
        loadSeasons()
    }

    private fun loadSeasons() {
        mDatabase?.getReference("seasons")?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(data: DataSnapshot) {
                if (data.hasChild("steam")) {
                    for (child in data.child("steam").children) {
                        if (child.value == true) {
                            pcCurrentSeason = child.key!!
                        }

                        pcSeasons[child.key!!] = child.value as Boolean
                    }
                } else if (data.hasChild("pc-na")) {
                    for (child in data.child("pc-na").children) {
                        if (child.value == true) {
                            pcCurrentSeason = child.key!!
                        }

                        pcSeasons[child.key!!] = child.value as Boolean
                    }
                }

                if (data.hasChild("xbox")) {
                    for (child in data.child("xbox").children) {
                        if (child.value == true) {
                            xboxCurrentSeason = child.key!!
                        }

                        xboxSeasons[child.key!!] = child.value as Boolean
                    }
                } else if (data.hasChild("xbox-na")) {
                    for (child in data.child("xbox-na").children) {
                        if (child.value == true) {
                            xboxCurrentSeason = child.key!!
                        }

                        xboxSeasons[child.key!!] = child.value as Boolean
                    }
                }

                if (data.hasChild("ps4")) {
                    for (child in data.child("ps4").children) {
                        if (child.value == true) {
                            ps4CurrentSeason = child.key!!
                        }

                        ps4Seasons[child.key!!] = child.value as Boolean
                    }
                }
            }
        })
    }

    fun getSeasonsMapForShard(shardID: String): HashMap<String, Boolean>? {
        if (shardID.contains("pc", true)) return pcSeasons
        if (shardID.contains("xbox", true)) return xboxSeasons
        return null
    }

    fun getSeasonsListForShard(shardID: String, highlightCurrent: Boolean = true): List<String> {
        val seasonList = ArrayList<String>()
        if (shardID.contains("pc", true) || shardID.contains("kakao", true) || shardID.contains("steam", true)) {
            for (entry in pcSeasons) {
                if ((entry.value)) {
                    if (highlightCurrent) {
                        seasonList.add("${entry.key} (Current)")
                        continue
                    }
                }
                seasonList.add(entry.key)
            }
        }
        if (shardID.contains("xbox", true)) {
            for (entry in xboxSeasons) {
                if ((entry.value)) {
                    if (highlightCurrent) {
                        seasonList.add("${entry.key} (Current)")
                        continue
                    }
                }
                seasonList.add(entry.key)
            }
        }

        if (shardID.contains("ps4", true)) {
            for (entry in ps4Seasons) {
                if ((entry.value)) {
                    if (highlightCurrent) {
                        seasonList.add("${entry.key} (Current)")
                        continue
                    }
                }
                seasonList.add(entry.key)
            }
        }


        seasonList.sort()
        seasonList.reverse()

        return seasonList
    }

    fun getCurrentSeasonInt(shardID: String): Int {
        if (shardID.contains("pc", true) || shardID.contains("steam", true)) {
            return getSeasonsListForShard(shardID, false).indexOf(pcCurrentSeason)
        }

        if (shardID.contains("xbox", true)) {
            return getSeasonsListForShard(shardID, false).indexOf(xboxCurrentSeason)
        }

        return 0
    }

    fun getCurrentSeasonForShard(shardID: String): String {
        if (shardID.contains("pc", true) || shardID.contains("steam", true) || shardID.contains("kakao", true)) {
            return pcCurrentSeason
        }

        if (shardID.contains("xbox", true)) {
            return xboxCurrentSeason
        }

        if (shardID.contains("ps4", true)) {
            return ps4CurrentSeason
        }

        return pcCurrentSeason
    }

    fun isSeasonNewFormat(seasonID: String, region: Regions.Region): Boolean {
        if (region == Regions.Region.KAKAO || region == Regions.Region.STEAM) {
            return when (seasonID) {
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
            return when (seasonID) {
                "2018-08" -> true
                "2018-01",
                "2018-02",
                "2018-03",
                "2018-04",
                "2018-05",
                "2018-06",
                "2018-07" -> false
                else -> true
            }
        } else {
            return true
        }
    }

}