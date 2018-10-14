package com.respondingio.battlegroundsbuddy.map

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.respondingio.battlegroundsbuddy.R
import kotlinx.android.synthetic.main.activity_weapon_detail_timeline.*

class MapDropRouletteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drop_roulette)

        setSupportActionBar(weaponTimelineToolbar)
        weaponTimelineToolbar?.setNavigationIcon(R.drawable.ic_arrow_back_24dp)
        weaponTimelineToolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

}