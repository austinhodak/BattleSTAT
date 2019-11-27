package com.brokenstrawapps.battlebuddy.utils

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

object Seasons {

    var mDatabase: FirebaseDatabase? = null

    val pcSeasons = HashMap<String, Boolean>()
    val xboxSeasons = HashMap<String, Boolean>()
    val ps4Seasons = HashMap<String, Boolean>()
    var pcCurrentSeason: String = "pc-2018-01"
    var xboxCurrentSeason: String = "2018-08"
    var ps4CurrentSeason: String = "2018-09"

    var pcSeasonsList: MutableList<Season> = ArrayList()
    var xboxSeasonsList: MutableList<Season> = ArrayList()
    var psnSeasonsList: MutableList<Season> = ArrayList()

    fun init() {
        mDatabase = FirebaseDatabase.getInstance()
        loadSeasons()
    }

    private fun loadSeasons() {
        /*mDatabase?.getReference("seasons")?.addListenerForSingleValueEvent(object : ValueEventListener {
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

                if (data.hasChild("psn")) {
                    for (child in data.child("psn").children) {
                        if (child.value == true) {
                            ps4CurrentSeason = child.key!!
                        }

                        ps4Seasons[child.key!!] = child.value as Boolean
                    }
                }
            }
        })*/

        mDatabase?.getReference("seasonsList")?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                //STEAM/KAKAO
                if (p0.hasChild("steam")) {
                    for (season in p0.child("steam").children) {
                        pcSeasonsList.add(Season(
                                name = season.child("name").value.toString(),
                                isActive = season.child("isActive").value as Boolean,
                                codeString = season.key.toString()
                        ))
                    }

                    pcSeasonsList.sortBy { it.name }
                    pcSeasonsList.reverse()
                }

                //XBOX
                if (p0.hasChild("xbox")) {
                    for (season in p0.child("xbox").children) {
                        xboxSeasonsList.add(Season(
                                name = season.child("name").value.toString(),
                                isActive = season.child("isActive").value as Boolean,
                                codeString = season.key.toString()
                        ))
                    }

                    xboxSeasonsList.sortBy { it.name }
                    xboxSeasonsList.reverse()
                }

                //PS4
                if (p0.hasChild("psn")) {
                    for (season in p0.child("psn").children) {
                        psnSeasonsList.add(Season(
                                name = season.child("name").value.toString(),
                                isActive = season.child("isActive").value as Boolean,
                                codeString = season.key.toString()
                        ))
                    }

                    psnSeasonsList.sortBy { it.name }
                    psnSeasonsList.reverse()
                }
            }
        })
    }

    fun getSeasonsMapForShard(shardID: String): HashMap<String, Boolean>? {
        if (shardID.contains("pc", true)) return pcSeasons
        if (shardID.contains("xbox", true)) return xboxSeasons
        return null
    }

    fun getSeasonStringList(platform: Platform, highlightCurrent: Boolean = true): List<String> {
        val seasonList = ArrayList<String>()
        if (platform == Platform.STEAM || platform == Platform.KAKAO) {
            for (item in pcSeasonsList) {
                if (item.isActive) {
                    seasonList.add(0, "${item.name}")
                    continue
                }

                seasonList.add("${item.name}")
            }
        }

        if (platform == Platform.XBOX) {
            for (item in xboxSeasonsList) {
                if (item.isActive) {
                    seasonList.add(0, "${item.name}")
                    continue
                }

                seasonList.add("${item.name}")
            }
        }

        if (platform == Platform.PS4) {
            for (item in psnSeasonsList) {
                if (item.isActive) {
                    seasonList.add(0, "${item.name}")
                    continue
                }

                seasonList.add("${item.name}")
            }
        }
        return seasonList
    }

    @Deprecated("")
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

        if (shardID.contains("psn", true)) {
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
        return 0
    }

    fun getCurrentSeasonForShard(shardID: String): String {
        if (shardID.contains("pc", true) || shardID.contains("steam", true) || shardID.contains("kakao", true)) {
            return pcCurrentSeason
        }

        if (shardID.contains("xbox", true)) {
            return xboxCurrentSeason
        }

        if (shardID.contains("psn", true)) {
            return ps4CurrentSeason
        }

        return pcCurrentSeason
    }

    /*@Deprecated("")
    fun getCurrentSeasonForPlatform(platform: Platform): String {
        return when (platform) {
            Platform.STEAM -> pcCurrentSeason
            Platform.KAKAO -> pcCurrentSeason
            Platform.XBOX -> xboxCurrentSeason
            Platform.PS4 -> ps4CurrentSeason
        }
    }*/

    fun getCurrentSeasonForPlatform(platform: Platform) : Season {
        if (platform == Platform.STEAM || platform == Platform.KAKAO) {
            for (season in pcSeasonsList) {
                if (season.isActive) {
                    return season
                }
            }
        }

        if (platform == Platform.XBOX) {
            for (season in xboxSeasonsList) {
                if (season.isActive) {
                    return season
                }
            }
        }

        if (platform == Platform.PS4) {
            for (season in psnSeasonsList) {
                if (season.isActive) {
                    return season
                }
            }
        }

        return Season(name = "Beta Season 2", codeString = "pc-2018-02", isActive = false)
    }

    fun getSeasonsForPlatform(platform: Platform): MutableList<Season> {
        return when(platform) {
            Platform.KAKAO,
            Platform.STEAM -> pcSeasonsList
            Platform.XBOX -> xboxSeasonsList
            Platform.PS4 -> psnSeasonsList
        }
    }

    fun isSeasonNewFormat(platform: Platform, season: Season? = null): Boolean {
        if (platform == Platform.STEAM || platform == Platform.STEAM) {
            return when (season?.codeString ?: pcCurrentSeason) {
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
        } else if (platform == Platform.XBOX) {
            return when (season?.codeString ?: xboxCurrentSeason) {
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
        } else if (platform == Platform.PS4) {
            return when (season?.codeString ?: ps4CurrentSeason) {
                "2018-09" -> false
                else -> true
            }
        } else {
            return true
        }
    }

    data class Season (
            var name: String,
            var codeString: String,
            var isActive: Boolean
    ) : Serializable
}