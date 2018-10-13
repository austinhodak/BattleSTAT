package com.respondingio.battlegroundsbuddy.weapondetail

import android.os.Bundle
import android.view.Menu
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
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

        //NavigationUI.setupWithNavController(weaponTimelineToolbar, Navigation.findNavController(this, R.id.weaponDetailNavHost))
    }
}