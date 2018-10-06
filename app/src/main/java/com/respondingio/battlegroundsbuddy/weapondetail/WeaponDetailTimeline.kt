package com.respondingio.battlegroundsbuddy.weapondetail

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.respondingio.battlegroundsbuddy.R
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

        supportFragmentManager.beginTransaction().replace(R.id.weaponTimelineFrame, WeaponDetailTimelineStats()).commit()
    }
}