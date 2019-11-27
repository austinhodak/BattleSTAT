package com.brokenstrawapps.battlebuddy.models

data class MasteryWeaponData (
        var XPTotal: Long = 0,
        var LevelCurrent: Int = 0,
        var TierCurrent: Int = 0,
        var StatsTotal: WeaponStats = WeaponStats(),
        var Medals: List<WeaponMedal> = ArrayList()
) {

    data class WeaponStats (
            var MostDamagePlayerInAGame: Double = 0.0,
            var DamagePlayer: Double = 0.0,
            var MostHeadShotsInAGame: Int = 0,
            var HeadShots: Long = 0,
            var LongestDefeat: Double = 0.0,
            var Kills: Long = 0,
            var MostKillsInAGame: Int = 0,
            var Groggies: Int = 0,
            var MostGroggiesInAGame: Int = 0
    )

    data class WeaponMedal (
            var MedalId: String = "",
            var Count: Long = 0
    )
}