package com.brokenstrawapps.battlebuddy.settings

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.brokenstrawapps.battlebuddy.AlertManager
import com.brokenstrawapps.battlebuddy.R
import com.brokenstrawapps.battlebuddy.map.MapDownloadActivity
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_settings.toolbar
import kotlinx.android.synthetic.main.dialog_player_list.*
import org.jetbrains.anko.appcompat.v7.navigationIconResource
import org.jetbrains.anko.configuration
import org.jetbrains.anko.support.v4.browse
import org.jetbrains.anko.support.v4.startActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)
        if (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            toolbar.navigationIconResource = R.drawable.ic_arrow_back_24dp
        } else {
            toolbar.navigationIconResource = R.drawable.ic_arrow_back_black_24dp
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment())
                .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.settings)

            /*findPreference<Preference>("manage_alerts")?.setOnPreferenceClickListener {
                startActivity<AlertManager>()
                true
            }*/

            findPreference<Preference>("feedback")?.setOnPreferenceClickListener {

                true
            }

            findPreference<Preference>("manage_maps")?.setOnPreferenceClickListener {
                startActivity<MapDownloadActivity>()
                true
            }

            findPreference<Preference>("discord")?.setOnPreferenceClickListener {
                browse("https://discord.gg/5bbJNvx")
                true
            }

            findPreference<Preference>("twitter")?.setOnPreferenceClickListener {
                browse("https://twitter.com/pubgbuddy")
                true
            }

            findPreference<Preference>("about")?.setOnPreferenceClickListener {
                LibsBuilder()
                        .withAboutIconShown(true)
                        .withAboutVersionShown(true)
                        .withActivityStyle(Libs.ActivityStyle.DARK)
                        .withActivityTitle("BattleSTAT")
                        .withAboutDescription("")
                        .start(activity)

                true
            }
        }
    }
}

