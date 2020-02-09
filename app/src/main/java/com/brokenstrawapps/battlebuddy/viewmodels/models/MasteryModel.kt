package com.brokenstrawapps.battlebuddy.viewmodels.models

import android.content.Context
import com.brokenstrawapps.battlebuddy.R

data class MasteryModel(
        var weaponMaster: MasteryModelV? = null
) {

    data class MasteryModelV(
            var id: String? = null,
            var type: String? = null,
            var attributes: Attributes? = null,
            //var lastUpdated: Long? = null,
            var error: Int? = null
    ) {

        data class Attributes(
                var latestMatchId: String? = null,
                var platform: String? = null,
                var seasonId: String? = null,
                var weaponSummaries: HashMap<String, WeaponSummaries>? = null
        ) {

            data class WeaponSummaries(
                    var weaponId: String? = null,
                    var weaponName: String? = null,
                    var LevelCurrent: Int? = null,
                    var TierCurrent: Int? = null,
                    var XPTotal: Long? = null,
                    var Medals: ArrayList<MedalsD>? = null,
                    var StatsTotal: StatsTotalD? = null
            ) {

                data class MedalsD(
                        var Count: Long? = null,
                        var MedalId: String? = null
                ) {
                    fun getMedalIcon(context: Context): Int {
                        return context.resources.getIdentifier(MedalId?.toLowerCase(), "drawable", context.packageName)
                    }

                    fun getMedalExplaination(): String {
                        return when (MedalId) {
                            "MedalAnnihilation" -> "Defeat an entire squad by yourself."
                            "MedalAssassin" -> "Defeat an enemy with a headshot without taking damage."
                            "MedalDeadeye" -> "Defeat an enemy with a headshot."
                            "MedalDoubleKill" -> "Defeat 2 enemies in rapid succession."
                            "MedalTripleKill" -> "Defeat 3 enemies in rapid succession."
                            "MedalQuadKill" -> "Defeat 4 enemies in rapid succession."
                            "MedalFirstBlood" -> "Defeat the first opponent in a match."
                            "MedalFrenzy" -> "Defeat 5 opponents with a single weapon in a match."
                            "MedalLastManStanding" -> "Defeat the last opponent in a match."
                            "MedalLongshot" -> "Defeat an enemy from at least 200 meters."
                            "MedalPunisher" -> "Explanation not provided."
                            "MedalRampage" -> "Defeat 10 opponents with a single weapon in a match."
                            else -> "Explanation not provided."
                        }
                    }
                }

                data class StatsTotalD(
                        var DamagePlayer: Double? = null,
                        var Defeats: Long? = null,
                        var Groggies: Long? = null,
                        var HeadShots: Long? = null,
                        var Kills: Long? = null,
                        var LongRangeDefeats: Long? = null,
                        var LongestDefeat: Double? = null,
                        var MostDamagePlayerInAGame: Double? = null,
                        var MostDefeatsInAGame: Long? = null,
                        var MostGroggiesInAGame: Long? = null,
                        var MostHeadShotsInAGame: Long? = null,
                        var MostKillsInAGame: Long? = null
                )

                fun getEmblem(): Int {
                    return when (LevelCurrent) {
                        in 0..9 -> R.drawable.mastery_newbie
                        in 10..19 -> R.drawable.mastery_student
                        in 20..29 -> R.drawable.mastery_novice
                        in 30..39 -> R.drawable.mastery_amateur
                        in 40..49 -> R.drawable.mastery_certified
                        in 50..59 -> R.drawable.mastery_licensed
                        in 60..69 -> R.drawable.mastery_specialist
                        in 70..79 -> R.drawable.mastery_professional
                        in 80..89 -> R.drawable.mastery_expert
                        in 90..99 -> R.drawable.mastery_ace
                        in 100..200 -> R.drawable.mastery_master
                        else -> R.drawable.mastery_newbie
                    }
                }

                fun getLevelName(): String {
                    return when (LevelCurrent) {
                        in 0..9 -> "Newbie"
                        in 10..19 -> "Student"
                        in 20..29 -> "Novice"
                        in 30..39 -> "Amateur"
                        in 40..49 -> "Certified"
                        in 50..59 -> "Licensed"
                        in 60..69 -> "Specialist"
                        in 70..79 -> "Professional"
                        in 80..89 -> "Expert"
                        in 90..99 -> "Ace"
                        in 100..200 ->"Master"
                        else -> "Newbie"
                    }
                }
            }
        }
    }
}