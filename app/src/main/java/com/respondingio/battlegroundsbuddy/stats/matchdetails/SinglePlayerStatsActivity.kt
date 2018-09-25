package com.respondingio.battlegroundsbuddy.stats.matchdetails

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.models.MatchParticipant
import kotlinx.android.synthetic.main.activity_match_detail.match_detail_toolbar
import kotlinx.android.synthetic.main.activity_match_detail.toolbar_title

class SinglePlayerStatsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_detail)
        setSupportActionBar(match_detail_toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val player: MatchParticipant = intent.getSerializableExtra("player") as MatchParticipant

        toolbar_title.text = player.attributes.stats.name

        val bundle = Bundle()
        bundle.putString("playerID", intent.getStringExtra("playerID"))
        bundle.putSerializable("match", intent.getSerializableExtra("match"))
        val matchesPlayerStatsFragment = MatchPlayerStatsFragment()
        matchesPlayerStatsFragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.match_frame, matchesPlayerStatsFragment)
                .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}