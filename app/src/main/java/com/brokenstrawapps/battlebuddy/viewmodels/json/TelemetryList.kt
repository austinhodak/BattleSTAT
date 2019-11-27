package com.brokenstrawapps.battlebuddy.viewmodels.json

import com.brokenstrawapps.battlebuddy.models.*

class TelemetryList : ArrayList<TelemetryInterface>() {

    fun getKills() : List<LogPlayerKill> {
        return this.filterNotNull().filter { it::class == LogPlayerKill::class } as List<LogPlayerKill>
    }

    fun getTakeDamages() : List<LogPlayerTakeDamage> {
        return this.filterNotNull().filter { it::class == LogPlayerTakeDamage::class } as List<LogPlayerTakeDamage>
    }

    fun getPlayerAttacks() : List<LogPlayerAttack> {
        return this.filterNotNull().filter { it::class == LogPlayerAttack::class } as List<LogPlayerAttack>
    }

    fun getCarePackageLands() : List<LogCarePackageLand> {
        return this.filterNotNull().filter { it::class == LogCarePackageLand::class } as List<LogCarePackageLand>
    }

    fun getMatchDefinition() : LogMatchDefinition {
        return this.find { it::class == LogMatchDefinition::class } as LogMatchDefinition
    }
}