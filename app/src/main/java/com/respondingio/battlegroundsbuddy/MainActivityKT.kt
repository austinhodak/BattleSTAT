package com.respondingio.battlegroundsbuddy

import android.os.Bundle
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import co.zsmb.materialdrawerkt.draweritems.divider
import com.afollestad.aesthetic.Aesthetic
import com.afollestad.aesthetic.AestheticActivity
import com.mikepenz.materialdrawer.Drawer
import kotlinx.android.synthetic.main.activity_new_home.newHomeToolbar

class MainActivityKT : AestheticActivity() {

    private var mDrawer: Drawer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_home)
        setSupportActionBar(newHomeToolbar)

        Aesthetic.config {
            colorPrimaryRes(R.color.primary700)
            colorPrimaryDarkRes(R.color.primary800)
            colorStatusBarRes(R.color.primary800)
            colorAccentRes(R.color.secondary500)
            colorWindowBackgroundRes(R.color.background_material_light)
            textColorPrimary(R.color.md_white_1000)
            textColorSecondaryRes(R.color.md_white_1000)
            textColorPrimaryInverseRes(R.color.primary800)
        }

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
                iconTintingEnabled = true
                selectedTextColorRes = R.color.secondary500
            }
            primaryItem("Weapons") {
                icon = R.drawable.icons8_rifle
            }
            divider()

        }
    }
}