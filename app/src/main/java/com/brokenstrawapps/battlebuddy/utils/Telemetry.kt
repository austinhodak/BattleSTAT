package com.brokenstrawapps.battlebuddy.utils

import android.content.Context
import com.brokenstrawapps.battlebuddy.R
import org.json.JSONObject

object Telemetry {

    var itemIds: JSONObject? = null
    var vehicleIds: JSONObject? = null
    var damageCausers: JSONObject? = null
    var damateTypes: JSONObject? = null

    fun getItemIds(context: Context): JSONObject {
        if (itemIds!= null) return itemIds!!

        val objectString: String = context.resources.openRawResource(R.raw.item_id).bufferedReader().use { it.readText() }
        itemIds = JSONObject(objectString)
        return itemIds!!
    }

    fun getVehicleIds(context: Context): JSONObject {
        if (vehicleIds!= null) return vehicleIds!!

        val objectString: String = context.resources.openRawResource(R.raw.vehicle_id).bufferedReader().use { it.readText() }
        vehicleIds = JSONObject(objectString)
        return vehicleIds!!
    }

    fun getDamageCausers(context: Context): JSONObject {
        if (damageCausers!= null) return damageCausers!!

        val objectString: String = context.resources.openRawResource(R.raw.damage_causer_name).bufferedReader().use { it.readText() }
        damageCausers = JSONObject(objectString)
        return damageCausers!!
    }

    fun getDamageTypes(context: Context): JSONObject {
        if (damateTypes!= null) return damateTypes!!

        val objectString: String = context.resources.openRawResource(R.raw.damage_type_category).bufferedReader().use { it.readText() }
        damateTypes = JSONObject(objectString)
        return damateTypes!!
    }
}