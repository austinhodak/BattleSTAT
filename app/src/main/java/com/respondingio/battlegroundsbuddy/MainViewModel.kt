package com.respondingio.battlegroundsbuddy

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Request.Method
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import java.lang.Double
import java.text.DecimalFormat

class MainViewModel(private var steamPlayerCount: String = "") : ViewModel() {
    val steamPlayerCountChange = MutableLiveData<String>()
    fun getSteamPlayerCount(application: Application) {
        val queue = Volley.newRequestQueue(application)

        val url = "https://api.steampowered.com/ISteamUserStats/GetNumberOfCurrentPlayers/v1/?appid=578080"
        val stringRequest = JsonObjectRequest(Method.GET, url, null,
                Response.Listener { response ->
                    val amount = Double.parseDouble(response.getJSONObject("response").getString("player_count"))
                    val formatter = DecimalFormat("#,###")
                    val formatted = formatter.format(amount)

                    steamPlayerCountChange.value = formatted
                },
                Response.ErrorListener { error ->
                    // TODO: Handle error
                })


        queue.add(stringRequest)
    }
}