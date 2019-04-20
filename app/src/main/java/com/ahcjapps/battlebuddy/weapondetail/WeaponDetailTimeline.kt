package com.ahcjapps.battlebuddy.weapondetail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.ahcjapps.battlebuddy.R
import com.ahcjapps.battlebuddy.utils.Ads
import com.ahcjapps.battlebuddy.utils.Premium
import com.ahcjapps.battlebuddy.viewmodels.WeaponDetailViewModel
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdView
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
            statsBanner.adUnitId = "ca-app-pub-2237535196399997/3242631898"
            statsBanner.loadAd(Ads.getAdBuilder())
            statsBanner.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    weaponStatsAdLL?.removeAllViews()
                    weaponStatsAdLL?.addView(statsBanner)
                }

                override fun onAdFailedToLoad(errorCode: Int) {
                    // Code to be executed when an ad request fails.
                }

                override fun onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                }

                override fun onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                }

                override fun onAdClosed() {
                    // Code to be executed when when the user is about to return
                    // to the app after tapping on an ad.
                }
            }

        }
    }
}