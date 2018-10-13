package com.respondingio.battlegroundsbuddy.premium

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.respondingio.battlegroundsbuddy.R
import kotlinx.android.synthetic.main.activity_upgrade.*

class UpgradeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upgrade)

        setSupportActionBar(upgradeToolbar)
        title = "Upgrade"
        upgradeToolbarWaterfall?.scrollView = upgradeScrollview
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}