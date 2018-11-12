package com.respondingio.battlegroundsbuddy.weapondetail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.ads.AdView
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.utils.Ads
import com.respondingio.battlegroundsbuddy.utils.Premium
import com.respondingio.battlegroundsbuddy.viewmodels.WeaponDetailViewModel
import kotlinx.android.synthetic.main.activity_weapon_detail_timeline.*

class WeaponDetailTimeline : AppCompatActivity() {

    private val viewModel: WeaponDetailViewModel by lazy {
        ViewModelProviders.of(this).get(WeaponDetailViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        viewModel.getWeaponData(intent.getStringExtra("weaponPath") ?: "")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weapon_detail_timeline)

        if (!Premium.isAdFreeUser()) {
            val statsBanner = AdView(this)
            statsBanner.adSize = com.google.android.gms.ads.AdSize.BANNER
            statsBanner.adUnitId = "ca-app-pub-1946691221734928/9265393389"
            statsBanner.loadAd(Ads.getAdBuilder())
            weaponStatsAdLL?.addView(statsBanner)
        }
    }
}