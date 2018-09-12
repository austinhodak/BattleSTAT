package com.respondingio.battlegroundsbuddy.stats

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.Telemetry
import com.respondingio.battlegroundsbuddy.models.PrefPlayer
import com.respondingio.battlegroundsbuddy.models.Seasons
import de.mateware.snacky.Snacky
import kotlinx.android.synthetic.main.activity_stats_main_new.bottom_navigation
import kotlinx.android.synthetic.main.activity_stats_main_new.no_player
import kotlinx.android.synthetic.main.activity_stats_main_new.stats_gamemode_picker
import kotlinx.android.synthetic.main.activity_stats_main_new.stats_gamemode_text
import kotlinx.android.synthetic.main.activity_stats_main_new.stats_player_picker
import kotlinx.android.synthetic.main.activity_stats_main_new.stats_player_text
import kotlinx.android.synthetic.main.activity_stats_main_new.stats_region_picker
import kotlinx.android.synthetic.main.activity_stats_main_new.stats_region_text
import kotlinx.android.synthetic.main.activity_stats_main_new.stats_season_picker
import kotlinx.android.synthetic.main.activity_stats_main_new.stats_season_text
import kotlinx.android.synthetic.main.activity_stats_main_new.weapon_detail_toolbar

class MainStatsActivity : AppCompatActivity() {

    var regionList = arrayOf("XBOX-AS", "XBOX-EU", "XBOX-NA", "XBOX-OC", "PC-KRJP", "PC-JP", "PC-NA", "PC-EU", "PC-RU", "PC-OC", "PC-KAKAO", "PC-SEA", "PC-SA", "PC-AS")
    var regions = arrayOf("Xbox Asia", "Xbox Europe", "Xbox North America", "Xbox Oceania", "PC Korea", "PC Japan", "PC North America", "PC Europe", "PC Russia", "PC Oceania", "PC Kakao", "PC South East Asia", "PC South and Central America", "PC Asia")
    var modesList = arrayOf("solo", "solo-fpp", "duo", "duo-fpp", "squad", "squad-fpp")

    lateinit var mSharedPreferences: SharedPreferences
    public var playersMap = HashMap<String, String>()
    var currentPlayer: PrefPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats_main_new)
        setSupportActionBar(weapon_detail_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mSharedPreferences = this.getSharedPreferences("com.respondingio.battlegroundsbuddy", Context.MODE_PRIVATE)

        if (mSharedPreferences.getBoolean("isStatsFirstLaunch", true)) {
            MaterialDialog.Builder(this)
                    .title("Player Stats (Beta)")
                    .content(R.string.statsOnBoarding)
                    .backgroundColorRes(R.color.md_orange_700)
                    .titleColorRes(R.color.md_white_1000)
                    .contentColorRes(R.color.md_white_1000)
                    .positiveColorRes(R.color.md_white_1000)
                    .positiveText("OKAY")
                    .onPositive { dialog, which ->
                        dialog.dismiss()
                        mSharedPreferences.edit().putBoolean("isStatsFirstLaunch", false).apply()
                    }
                    .show()
        }

        setupTopNav()
        setupBottomNav()
        loadPlayers()

        if (mSharedPreferences.contains("selected-player-id")) {
            no_player.visibility = View.GONE
            setPlayerSelected(mSharedPreferences.getString("selected-player-id", "")!!)
        } else {
            //TODO SETUP NO PLAYER SELECTED STUFF.
            no_player.visibility = View.VISIBLE
            stats_player_text.text = "Select Player"
            stats_region_text.text = "Select Region"
            stats_gamemode_text.text = "Select Gamemode"
            stats_season_text.text = "Select Season"
        }
    }

    private var selectedRegion: Int = -1
    private var selectedGamemode: Int = -1
    private var selectedSeason: Int = -1

    private fun setupTopNav() {
        stats_player_picker.setOnClickListener {
            MaterialDialog.Builder(this@MainStatsActivity)
                    .title("Select Player")
                    .adapter(playerListAdapter, null)
                    .neutralText("LINK NEW")
                    .onNeutral { dialog, which ->
                        val addPlayerBottomSheet = AddPlayerBottomSheet()
                        addPlayerBottomSheet.show(supportFragmentManager, addPlayerBottomSheet.tag)
                    }
                    .show()
        }
        stats_region_picker.setOnClickListener {
            if (currentPlayer == null) {
                Toast.makeText(this@MainStatsActivity, "Must select player first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            MaterialDialog.Builder(this)
                    .title("Select Region")
                    .items(regions.toMutableList())
                    .itemsCallbackSingleChoice(selectedRegion) { dialog, itemView, which, text ->
                        selectedRegion = which

                        val existingPlayer = Gson().fromJson(mSharedPreferences.getString("player-${mSharedPreferences.getString("selected-player-id", "")}", null), PrefPlayer::class.java)
                        if (existingPlayer != null) {
                            existingPlayer.selectedShardID = regionList[which].toLowerCase()
                            mSharedPreferences.edit().putString("player-${mSharedPreferences.getString("selected-player-id", "")}", Gson().toJson(existingPlayer)).apply()
                        }

                        val regionText = stats_region_picker.getChildAt(1) as TextView
                        regionText.text = text

                        setPlayerSelected(existingPlayer.playerID)

                        false
                    }
                    .show()
        }
        stats_gamemode_picker.setOnClickListener {
            if (currentPlayer == null) {
                Toast.makeText(this@MainStatsActivity, "Must select player first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            MaterialDialog.Builder(this@MainStatsActivity)
                    .title("Select Gamemode")
                    .items("Solo TPP", "Solo FPP", "Duo TPP", "Duo FPP", "Squad TPP", "Squad FPP")
                    .itemsCallbackSingleChoice(selectedGamemode) { dialog, itemView, which, text ->
                        selectedGamemode = which
                        stats_gamemode_text.text = text

                        val existingPlayer = Gson().fromJson(mSharedPreferences.getString("player-${mSharedPreferences.getString("selected-player-id", "")}", null), PrefPlayer::class.java)
                        if (existingPlayer != null) {
                            existingPlayer.selectedGamemode = modesList[which]
                            mSharedPreferences.edit().putString("player-${mSharedPreferences.getString("selected-player-id", "")}", Gson().toJson(existingPlayer)).apply()
                        }

                        setPlayerSelected(existingPlayer.playerID)
                        false
                    }
                    .show()
        }
        stats_season_picker.setOnClickListener {
            if (currentPlayer == null) {
                Toast.makeText(this@MainStatsActivity, "Must select player first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val regionInt = if (regionList[selectedRegion].contains("PC")) {
                1
            } else {
                2
            }
            MaterialDialog.Builder(this)
                    .title("Select Season (" + stats_region_text.text + ")")
                    .items(Seasons.getInstance().getSeasonListArray(regionInt))
                    .itemsCallbackSingleChoice(selectedSeason) { dialog, itemView, which, text ->
                        selectedSeason = which
                        var seasonID = text.toString()
                        if (seasonID.contains("Current")) {
                            seasonID = seasonID.replace(" .*".toRegex(), "")
                        }

                        stats_season_text.text = Seasons.getInstance().getSeasonListArray(regionInt)[selectedSeason]

                        val existingPlayer = Gson().fromJson(mSharedPreferences.getString("player-${mSharedPreferences.getString("selected-player-id", "")}", null), PrefPlayer::class.java)
                        if (existingPlayer != null) {
                            existingPlayer.selectedSeason = seasonID
                            mSharedPreferences.edit().putString("player-${mSharedPreferences.getString("selected-player-id", "")}", Gson().toJson(existingPlayer)).apply()
                        }

                        setPlayerSelected(existingPlayer.playerID)
                        false
                    }
                    .show()
        }
    }

    val playerListAdapter = MaterialSimpleListAdapter(MaterialSimpleListAdapter.Callback { dialog, index, item ->
        if (dialog.titleView.text == "Select Player") {
            setPlayerSelected(playersMap[item.content].toString())
            dialog.dismiss()
        } else {
            deletePlayer(item.content.toString())
            dialog.dismiss()
        }
    })

    private fun loadPlayers() {
        playersMap.clear()
        playerListAdapter.clear()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().reference.child("users").child(currentUser!!.uid).child("pubg_players")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val player: PrefPlayer

                if (mSharedPreferences.contains("player-${p0.key}")) {
                    //Player exists in shared prefs, update any needed items and add to list.
                    val existingPlayer = Gson().fromJson(mSharedPreferences.getString("player-${p0.key}", null), PrefPlayer::class.java)
                    existingPlayer.playerID = p0.key.toString()
                    existingPlayer.playerName = p0.child("playerName").value.toString()
                    existingPlayer.defaultShardID = p0.child("shardID").value.toString()

                    if (existingPlayer.selectedSeason.isNullOrEmpty()) {
                        val seasonID: String = if (existingPlayer.defaultShardID.contains("pc")) {
                            Seasons.getInstance().pcCurrentSeason
                        } else {
                            Seasons.getInstance().xboxCurrentSeason
                        }

                        existingPlayer.selectedSeason = seasonID
                    }

                    mSharedPreferences.edit().putString("player-${p0.key}", Gson().toJson(existingPlayer)).apply()

                    player = existingPlayer
                } else {
                    //Player doesn't exist.
                    val newPlayer = PrefPlayer(
                            playerID = p0.key.toString(),
                            playerName = p0.child("playerName").value.toString(),
                            defaultShardID = p0.child("shardID").value.toString(),
                            selectedGamemode = "solo"
                    )

                    val seasonID: String
                    seasonID = if (newPlayer.defaultShardID.contains("pc")) {
                        Seasons.getInstance().pcCurrentSeason
                    } else {
                        Seasons.getInstance().xboxCurrentSeason
                    }

                    newPlayer.selectedSeason = seasonID

                    mSharedPreferences.edit().putString("player-${p0.key}", Gson().toJson(newPlayer)).apply()

                    player = newPlayer

                    setPlayerSelected(p0.key.toString())
                }

                val iconDrawable: Int
                iconDrawable = if (player.defaultShardID.contains("pc")) {
                    R.drawable.windows_color
                } else {
                    R.drawable.xbox_logo
                }

                playerListAdapter.add(MaterialSimpleListItem.Builder(this@MainStatsActivity)
                        .content(player.playerName)
                        .icon(iconDrawable)
                        .iconPaddingDp(8)
                        .backgroundColor(Color.WHITE)
                        .build())

                playersMap[player.playerName] = player.playerID
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (!p0.exists()) {
                    Snacky.builder().setView(bottom_navigation).warning().setText("You haven't linked your PUBG account yet.").setAction("LINK"
                    ) {
                        val addPlayerBottomSheet = AddPlayerBottomSheet()
                        addPlayerBottomSheet.show(supportFragmentManager, addPlayerBottomSheet.tag)
                    }.setDuration(Snacky.LENGTH_INDEFINITE).show()
                }
            }

        })
    }

    public fun setPlayerSelected(id: String) {
        no_player.visibility = View.GONE

        mSharedPreferences.edit().putString("selected-player-id", id).apply()

        val player = Gson().fromJson(mSharedPreferences.getString("player-$id", null), PrefPlayer::class.java)
        currentPlayer = player

        stats_player_text.text = player.playerName
        stats_region_text.text = regions[regionList.indexOf(player.selectedShardID.toUpperCase())]
        selectedRegion = regionList.indexOf(player.selectedShardID.toUpperCase())

        try {
            if (!player.selectedSeason.isNullOrEmpty()) {
                if (player.selectedShardID.contains("pc")) {
                    selectedSeason = Seasons.getInstance().getSeasonListArrayOG(1).indexOf(player.selectedSeason)
                    stats_season_text.text = Seasons.getInstance().getSeasonListArray(1)[selectedSeason].toString()
                } else {
                    selectedSeason = Seasons.getInstance().getSeasonListArrayOG(2).indexOf(player.selectedSeason)
                    stats_season_text.text = Seasons.getInstance().getSeasonListArray(2)[selectedSeason].toString()
                }
            }
        } catch (e: Exception) {
            try {
                if (player.selectedShardID.contains("pc")) {
                    selectedSeason = Seasons.getInstance().getCurrentSeasonInt(1)
                    stats_season_text.text = Seasons.getInstance().getSeasonListArray(1)[selectedSeason].toString()
                } else {
                    selectedSeason = Seasons.getInstance().getCurrentSeasonInt(2)
                    stats_season_text.text = Seasons.getInstance().getSeasonListArray(2)[selectedSeason].toString()
                }
            } catch (e: Exception) {
                Snacky.builder().error().setText("Unknown Error Occurred").show()
            }
        }

        var gamemode = "solo"
        if (!player.selectedGamemode.isNullOrEmpty()) {
            gamemode = player.selectedGamemode.toString()
            selectedGamemode = modesList.indexOf(player.selectedGamemode)
        } else {
            selectedGamemode = modesList.indexOf(gamemode)
        }
        stats_gamemode_text.text = Telemetry().gameModes.getString(gamemode)

        reloadFragments(currentPlayer!!.playerID)
    }

    private fun setupBottomNav() {
        bottom_navigation.setOnNavigationItemSelectedListener { item ->
            if (currentPlayer == null || !mSharedPreferences.contains("selected-player-id")) {
                if (supportFragmentManager.findFragmentByTag("STATS") != null) {
                    val fragment: MainStatsFragment = supportFragmentManager.findFragmentByTag("STATS") as MainStatsFragment
                    supportFragmentManager.beginTransaction().remove(fragment).commit()
                } else if (supportFragmentManager.findFragmentByTag("MATCHES") != null) {
                    val fragment: MatchesListFragment = supportFragmentManager.findFragmentByTag("MATCHES") as MatchesListFragment
                    supportFragmentManager.beginTransaction().remove(fragment).commit()
                }
                return@setOnNavigationItemSelectedListener false
            }
            when (item.itemId) {
                R.id.your_stats_menu -> {
                    if (supportFragmentManager.findFragmentByTag("STATS") != null) return@setOnNavigationItemSelectedListener true
                    val yourStatsFragment = MainStatsFragment()
                    val mBundle = Bundle()
                    mBundle.putSerializable("player", currentPlayer)
                    yourStatsFragment.arguments = mBundle
                    supportFragmentManager.beginTransaction().replace(R.id.home_frame, yourStatsFragment, "STATS").commit()
                }
                R.id.matches_menu -> {
                    if (supportFragmentManager.findFragmentByTag("MATCHES") != null) return@setOnNavigationItemSelectedListener true
                    val matchesListFragment = MatchesListFragment()
                    val mBundle = Bundle()
                    mBundle.putSerializable("player", currentPlayer)
                    matchesListFragment.arguments = mBundle
                    supportFragmentManager.beginTransaction().replace(R.id.home_frame, matchesListFragment, "MATCHES").commit()
                }
            }
            true
        }
    }

    private fun reloadFragments(id: String) {
        if (currentPlayer == null || !mSharedPreferences.contains("selected-player-id")) {
            if (supportFragmentManager.findFragmentByTag("STATS") != null) {
                val fragment: MainStatsFragment = supportFragmentManager.findFragmentByTag("STATS") as MainStatsFragment
                supportFragmentManager.beginTransaction().remove(fragment).commit()
            } else if (supportFragmentManager.findFragmentByTag("MATCHES") != null) {
                val fragment: MatchesListFragment = supportFragmentManager.findFragmentByTag("MATCHES") as MatchesListFragment
                supportFragmentManager.beginTransaction().remove(fragment).commit()
            }
            return
        }

        val mBundle = Bundle()
        mBundle.putSerializable("player", currentPlayer)

        val yourStatsFragment = MainStatsFragment()
        when (bottom_navigation.selectedItemId) {
            R.id.your_stats_menu -> {
                if (supportFragmentManager.findFragmentByTag("STATS") != null) {
                    val fragment: MainStatsFragment = supportFragmentManager.findFragmentByTag("STATS") as MainStatsFragment
                    try {
                        mBundle.putSerializable("previousPlayer", fragment.currentPlayer)
                    } catch (e: Exception) {
                        //I tried :/
                    }
                }
                yourStatsFragment.arguments = mBundle
                supportFragmentManager.beginTransaction().replace(R.id.home_frame, yourStatsFragment, "STATS").commit()
            }
            R.id.matches_menu -> {
                val matchesListFragment = MatchesListFragment()
                matchesListFragment.arguments = mBundle
                supportFragmentManager.beginTransaction().replace(R.id.home_frame, matchesListFragment, "MATCHES").commit()
            }
            else -> {
                yourStatsFragment.arguments = mBundle
                supportFragmentManager.beginTransaction().replace(R.id.home_frame, yourStatsFragment, "STATS").commit()
            }
        }
    }

    private fun deletePlayer(name: String) {
        var playerID = playersMap[name]
        playersMap.remove(name)

        if (mSharedPreferences.getString("selected-player-id", "") == playerID) {
            //Deleted player is currently selected, remove that.
            mSharedPreferences.edit().remove("selected-player-id").remove("player-$playerID").apply()

            no_player.visibility = View.VISIBLE
            stats_player_text.text = "Select Player"
            stats_region_text.text = "Select Region"
            stats_gamemode_text.text = "Select Gamemode"
            stats_season_text.text = "Select Season"

            selectedSeason = -1
            selectedGamemode = -1
            selectedRegion = -1

            currentPlayer = null

            if (supportFragmentManager.findFragmentByTag("STATS") != null) {
                val fragment: MainStatsFragment = supportFragmentManager.findFragmentByTag("STATS") as MainStatsFragment
                supportFragmentManager.beginTransaction().remove(fragment).commit()
            } else if (supportFragmentManager.findFragmentByTag("MATCHES") != null) {
                val fragment: MatchesListFragment = supportFragmentManager.findFragmentByTag("MATCHES") as MatchesListFragment
                supportFragmentManager.beginTransaction().remove(fragment).commit()
            }
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        FirebaseDatabase.getInstance().getReference("users").child(currentUser!!.uid).child("pubg_players").child(playerID.toString()).removeValue()

        loadPlayers()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.game_stats, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.stats_delete_player) {
            MaterialDialog.Builder(this@MainStatsActivity)
                    .title("Select Player to Delete")
                    .adapter(playerListAdapter, null)
                    .negativeText("CANCEL")
                    .show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1002) {
            val responseCode = data?.getIntExtra("RESPONSE_CODE", 0)
            val purchaseData = data?.getStringExtra("INAPP_PURCHASE_DATA")
            val dataSignature = data?.getStringExtra("INAPP_DATA_SIGNATURE")

            if (resultCode == Activity.RESULT_OK) {
                val m2SharedPreferences = this.getSharedPreferences("com.austinhodak.pubgcenter", Context.MODE_PRIVATE)

                mSharedPreferences.edit().putBoolean("premiumV1", true).apply()
                m2SharedPreferences.edit().putBoolean("removeAds", true).apply()
                Snacky.builder().setActivity(this).info().setText("Thanks! Enjoy the new stuff!").show()
            }
        }
    }
}