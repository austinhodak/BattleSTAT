package com.austinh.battlebuddy.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.austinh.battlebuddy.R
import kotlinx.android.synthetic.main.activity_settings.*
import org.jetbrains.anko.appcompat.v7.navigationIconResource

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)
        toolbar.navigationIconResource = R.drawable.instabug_ic_back
        toolbar.setNavigationOnClickListener { onBackPressed() }

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment())
                .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.settings)
        }
    }
}

