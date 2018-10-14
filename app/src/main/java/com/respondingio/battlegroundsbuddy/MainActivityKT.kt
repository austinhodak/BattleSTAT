package com.respondingio.battlegroundsbuddy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.divider
import com.afollestad.aesthetic.Aesthetic
import com.afollestad.aesthetic.AestheticActivity
import com.mikepenz.materialdrawer.Drawer
import kotlinx.android.synthetic.main.activity_new_home.newHomeToolbar

class MainActivityKT : AppCompatActivity() {

    private var mDrawer: Drawer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_home)
        setSupportActionBar(newHomeToolbar)

        setupDrawer()
    }

    private fun setupDrawer() {
        mDrawer = drawer {
            headerDivider = false
            headerViewRes = R.layout.home_drawer_header
            toolbar = newHomeToolbar
            sliderBackgroundColorRes = R.color.primary800
            primaryItem("Player Stats") {
                icon = R.drawable.icons8_chart
            }
            primaryItem("Weapons") {
                icon = R.drawable.icons8_rifle
            }
            divider()

        }
    }
}