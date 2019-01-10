package com.austinh.battlebuddy.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.austinh.battlebuddy.AlertManager
import com.austinh.battlebuddy.R
import com.austinh.battlebuddy.map.MapDownloadActivity
import com.instabug.bug.BugReporting
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import kotlinx.android.synthetic.main.activity_settings.*
import org.jetbrains.anko.appcompat.v7.navigationIconResource
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.browse
import org.jetbrains.anko.support.v4.startActivity

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

            findPreference("manage_alerts").setOnPreferenceClickListener {
                startActivity<AlertManager>()
                true
            }

            findPreference("feedback").setOnPreferenceClickListener {
                BugReporting.invoke()
                true
            }

            findPreference("manage_maps").setOnPreferenceClickListener {
                startActivity<MapDownloadActivity>()
                true
            }

            findPreference("discord").setOnPreferenceClickListener {
                browse("https://discord.gg/5bbJNvx")
                true
            }

            findPreference("twitter").setOnPreferenceClickListener {
                browse("https://twitter.com/pubgbuddy")
                true
            }

            findPreference("about").setOnPreferenceClickListener {
                LibsBuilder()
                        .withAboutIconShown(true)
                        .withAboutVersionShown(true)
                        .withActivityStyle(Libs.ActivityStyle.DARK)
                        .withActivityTitle("Battlegrounds Battle Buddy")
                        .withAboutDescription("")
                        .start(activity)

                true
            }
        }
    }
}

