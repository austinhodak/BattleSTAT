package com.ahcjapps.battlebuddy.viewmodels.json

import com.ahcjapps.battlebuddy.models.*
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException

import java.lang.reflect.Type

class TelemetryInterfaceDeserializer : JsonDeserializer<TelemetryInterface> {

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): TelemetryInterface? {
        val jObject = json as JsonObject
        val typeObj = jObject.get("_T")

        if (typeObj != null) {
            val typeVal = typeObj.asString
            //Timber.d(typeVal);

            when (typeVal) {
                "LogArmorDestroy" -> return context.deserialize<TelemetryInterface>(json, LogArmorDestroy::class.java)
                "LogCarePackageLand" -> return context.deserialize<TelemetryInterface>(json, LogCarePackageLand::class.java)
                "LogCarePackageSpawn" -> return context.deserialize<TelemetryInterface>(json, LogCarePackageSpawn::class.java)
                "LogGameStatePeriodic" -> return context.deserialize<TelemetryInterface>(json, LogGamestatePeriodic::class.java)
                "LogHeal" -> return context.deserialize<TelemetryInterface>(json, LogHeal::class.java)
                "LogItemAttach" -> return context.deserialize<TelemetryInterface>(json, LogItemAttach::class.java)
                "LogItemDetach" -> return context.deserialize<TelemetryInterface>(json, LogItemDetach::class.java)
                "LogItemDrop" -> return context.deserialize<TelemetryInterface>(json, LogItemDrop::class.java)
                "LogItemEquip" -> return context.deserialize<TelemetryInterface>(json, LogItemEquip::class.java)
                "LogItemPickup" -> return context.deserialize<TelemetryInterface>(json, LogItemPickup::class.java)
                "LogItemPickupFromCarepackage" -> return context.deserialize<TelemetryInterface>(json, LogItemPickupFromCarepackage::class.java)
                "LogItemPickupFromLootbox" -> return context.deserialize<TelemetryInterface>(json, LogItemPickupFromLootbox::class.java)
                "LogItemUnequip" -> return context.deserialize<TelemetryInterface>(json, LogItemUnequip::class.java)
                "LogItemUse" -> return context.deserialize<TelemetryInterface>(json, LogItemUse::class.java)
                "LogMatchDefinition" -> return context.deserialize<TelemetryInterface>(json, LogMatchDefinition::class.java)
                "LogMatchEnd" -> return context.deserialize<TelemetryInterface>(json, LogMatchEnd::class.java)
                "LogMatchStart" -> return context.deserialize<TelemetryInterface>(json, LogMatchStart::class.java)
                "LogObjectDestroy" -> return context.deserialize<TelemetryInterface>(json, LogObjectDestroy::class.java)
                "LogParachuteLanding" -> return context.deserialize<TelemetryInterface>(json, LogParachuteLanding::class.java)
                "LogPlayerAttack" -> return context.deserialize<TelemetryInterface>(json, LogPlayerAttack::class.java)
                "LogPlayerCreate" -> return context.deserialize<TelemetryInterface>(json, LogPlayerCreate::class.java)
                "LogPlayerLogin" -> return context.deserialize<TelemetryInterface>(json, LogPlayerLogin::class.java)
                "LogPlayerLogout" -> return context.deserialize<TelemetryInterface>(json, LogPlayerLogout::class.java)
                "LogPlayerMakeGroggy" -> return context.deserialize<TelemetryInterface>(json, LogPlayerMakeGroggy::class.java)
                "LogPlayerPosition" -> return context.deserialize<TelemetryInterface>(json, LogPlayerPosition::class.java)
                "LogPlayerRevive" -> return context.deserialize<TelemetryInterface>(json, LogPlayerRevive::class.java)
                "LogPlayerTakeDamage" -> return context.deserialize<TelemetryInterface>(json, LogPlayerTakeDamage::class.java)
                "LogVaultStart" -> return context.deserialize<TelemetryInterface>(json, LogVaultStart::class.java)
                "LogVehicleDestroy" -> return context.deserialize<TelemetryInterface>(json, LogVehicleDestroy::class.java)
                "LogVehicleLeave" -> return context.deserialize<TelemetryInterface>(json, LogVehicleLeave::class.java)
                "LogVehicleRide" -> return context.deserialize<TelemetryInterface>(json, LogVehicleRide::class.java)
                "LogWeaponFireCount" -> return context.deserialize<TelemetryInterface>(json, LogWeaponFireCount::class.java)
                "LogWheelDestroy" -> return context.deserialize<TelemetryInterface>(json, LogWheelDestroy::class.java)
                "LogSwimEnd" -> return context.deserialize<TelemetryInterface>(json, LogSwimEnd::class.java)
                "LogSwimStart" -> return context.deserialize<TelemetryInterface>(json, LogSwimStart::class.java)
                "LogPlayerKill" -> return context.deserialize<TelemetryInterface>(json, LogPlayerKill::class.java)
            }
        }
        return null
    }
}

