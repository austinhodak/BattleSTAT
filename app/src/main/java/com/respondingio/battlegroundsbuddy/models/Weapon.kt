package com.respondingio.battlegroundsbuddy.models

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Weapon(
        val ammo: String = "--",
        val ammoPerMag: String = "--",
        val attachments: List<DocumentReference> = ArrayList(),
        val burstDelay: String = "N/A",
        val burstShots: String = "N/A",
        val damageBody0: String = "--",
        val damageHead0: String = "--",
        val desc: String = "--",
        val firingModes: String = "--",
        val icon: String = "--",
        val likes: Int = 0,
        val pickupDelay: String = "--",
        val power: String = "--",
        val range: String = "--",
        val readyDelay: String = "--",
        val reloadDurationFull: String = "--",
        val reloadDurationTac: String = "--",
        val reloadMethod: String = "--",
        val speed: String = "--",
        val weapon_name: String = "",
        val wiki: String = "",
        val airDropOnly: Boolean = false,
        val bestInClass: Boolean = false,
        val miramar_only: Boolean = false,
        val sanhok_only: Boolean = false,
        val TBS: String = "--"
)

data class WeaponStat(
        var statName: String,
        var statValue: String
)