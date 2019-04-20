package com.ahcjapps.battlebuddy.map

import android.content.Context
import com.google.gson.Gson
import com.ahcjapps.battlebuddy.R
import org.json.JSONObject

object MapHelper {

    fun getErangelPlaces(context: Context): MapDropModel {
        val objectString: String = context.resources.openRawResource(R.raw.maps).bufferedReader().use { it.readText() }
        val map = JSONObject(objectString).getJSONArray("maps").getJSONObject(0)
        return Gson().fromJson(map.toString(), MapDropModel::class.java)
    }

    fun getMiramarPlaces(context: Context): MapDropModel {
        val objectString: String = context.resources.openRawResource(R.raw.maps).bufferedReader().use { it.readText() }
        val map = JSONObject(objectString).getJSONArray("maps").getJSONObject(1)
        return Gson().fromJson(map.toString(), MapDropModel::class.java)
    }

    fun getSanhokPlaces(context: Context): MapDropModel {
        val objectString: String = context.resources.openRawResource(R.raw.maps).bufferedReader().use { it.readText() }
        val map = JSONObject(objectString).getJSONArray("maps").getJSONObject(3)
        return Gson().fromJson(map.toString(), MapDropModel::class.java)
    }

    fun getVikendiPlaces(context: Context): MapDropModel {
        val objectString: String = context.resources.openRawResource(R.raw.maps).bufferedReader().use { it.readText() }
        val map = JSONObject(objectString).getJSONArray("maps").getJSONObject(2)
        return Gson().fromJson(map.toString(), MapDropModel::class.java)
    }
}