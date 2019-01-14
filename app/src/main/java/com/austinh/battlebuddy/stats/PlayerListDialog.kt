package com.austinh.battlebuddy.stats

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.afollestad.materialdialogs.checkbox.isCheckPromptChecked
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinh.battlebuddy.R
import com.austinh.battlebuddy.map.MapDownloadActivity
import com.austinh.battlebuddy.models.PlayerListModel
import com.austinh.battlebuddy.models.PlayerStats
import com.austinh.battlebuddy.models.SeasonStatsAll
import com.austinh.battlebuddy.premium.UpgradeActivity
import com.austinh.battlebuddy.snacky.Snacky
import com.austinh.battlebuddy.stats.compare.ComparePlayerModel
import com.austinh.battlebuddy.stats.main.StatsHome
import com.austinh.battlebuddy.utils.*
import com.austinh.battlebuddy.viewmodels.models.PlayerModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.functions.FirebaseFunctions
import com.leinardi.android.speeddial.SpeedDialActionItem
import kotlinx.android.synthetic.main.dialog_player_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.appcompat.v7.navigationIconResource
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.set
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PlayerListDialog : AppCompatActivity() {

    private var players: MutableList<PlayerListModel> = ArrayList()
    lateinit var mAdapter: SlimAdapter
    private var userAccountID = ""

    private val listeners: MutableMap<DatabaseReference, ValueEventListener> = HashMap()
    private var mSharedPreferences: SharedPreferences? = null
    private var mDatabase: FirebaseDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.instabug_fadein, R.anim.instabug_fadeout)
        setContentView(R.layout.dialog_player_list)

        mSharedPreferences = this.getSharedPreferences("com.austinh.battlebuddy", Context.MODE_PRIVATE)
        mDatabase = FirebaseDatabase.getInstance()
        setSupportActionBar(toolbar)

        if (intent.action != null) {
            toolbar_title?.text = "Pick a Player"
            toolbar.navigationIconResource = R.drawable.instabug_ic_close
        } else {
            toolbar.navigationIconResource = R.drawable.instabug_ic_back
        }

        toolbar.setNavigationOnClickListener { onBackPressed() }

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            playerListRV?.layoutManager = GridLayoutManager(this, 1)
        } else {
            playerListRV?.layoutManager = GridLayoutManager(this, 2)
        }

        mAdapter = SlimAdapter.create().attachTo(playerListRV).updateData(players).register<PlayerListModel>(R.layout.player_list_item) { player, injector ->
            val cardView = injector.findViewById<CardView>(R.id.playerListCard)

            val rankIcon = injector.findViewById<ImageView>(R.id.game_version_icon)
            val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()

            if (mSharedPreferences?.contains("playerRankTitle-${player.playerID}") == true) {
                val rank = Ranks.getRankBy(mSharedPreferences?.getString("playerRankTitle-${player.playerID}", "0-0")!!)

                Glide.with(applicationContext)
                        .load(Ranks.getRankIcon(rank))
                        //.load(Ranks.getRankIcon(Rank.valueOf(mSharedPreferences?.getString("playerRankTitle-${player.playerID}", "UNKNOWN")!!.toUpperCase())))
                        .transition(DrawableTransitionOptions.withCrossFade(factory))
                        .into(rankIcon)

                cardView.setCardBackgroundColor(resources.getColor(Ranks.getRankColor(rank)))
                injector.text(R.id.playerListSubtitle, rank.title + " " + Ranks.getRankLevel(rank = mSharedPreferences?.getString("playerRankTitle-${player.playerID}", "0-0")!!))
            } else {
                injector.invisible(R.id.game_version_icon)
                cardView.setCardBackgroundColor(resources.getColor(Ranks.getRankColor(0.0)))
            }

            when (player.platform) {
                Platform.KAKAO,
                Platform.STEAM -> injector.image(R.id.player_list_platform_icon, R.drawable.windows_white)
                Platform.XBOX -> injector.image(R.id.player_list_platform_icon, R.drawable.xbox_white)
                Platform.PS4 -> injector.image(R.id.player_list_platform_icon, R.drawable.ic_icons8_playstation)
            }

            if (player.isPlayerCurrentUser) {
                injector.text(R.id.player_select_name, "${player.playerName}  \uf2bd")
            } else {
                val text = injector.findViewById<TextView>(R.id.player_select_name)
                text.text = player.playerName
            }

            if (intent.action == null) {
                cardView.setOnClickListener {
                    startActivity<StatsHome>("selectedPlayer" to player)
                }
            }

            injector.gone(R.id.player_pg)

            listeners[FirebaseDatabase.getInstance().getReference("user_stats/${player.playerID}/season_data/${player.getDatabaseSearchURL()}/${Seasons.getCurrentSeasonForPlatform(player.platform).codeString}/stats")] = FirebaseDatabase.getInstance().getReference("user_stats/${player.playerID}/season_data/${player.getDatabaseSearchURL()}/${Seasons.getCurrentSeasonForPlatform(player.platform).codeString}/stats").addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (!p0.exists()) {
                        if (!isDestroyed) {
                            Glide.with(this@PlayerListDialog)
                                    .load(Ranks.getRankIcon(0.0))
                                    .transition(DrawableTransitionOptions.withCrossFade(factory))
                                    .into(rankIcon)
                        }

                        injector.invisible(R.id.game_version_icon)
                        cardView.setCardBackgroundColor(resources.getColor(Ranks.getRankColor(0.0)))

                        injector.visible(R.id.player_pg)



                        player.getAllStats()[0].addOnSuccessListener {
                            injector.gone(R.id.player_pg)
                        }.addOnFailureListener {
                            injector.gone(R.id.player_pg)
                        }
                        return
                    }

                    injector.gone(R.id.player_pg)

                    val seasonStats = p0.getValue(SeasonStatsAll::class.java)!!

                    if (intent.action != null) {
                        cardView.setOnClickListener {
                            val intent = Intent()
                            intent.putExtra("selectedPlayer", ComparePlayerModel(player, seasonStats.getPlayerModel()))
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        }
                    }

                    injector.visible(R.id.game_version_icon)

                    Glide.with(applicationContext)
                            .load(Ranks.getRankIcon(seasonStats.getHighestRank()))
                            .transition(DrawableTransitionOptions.withCrossFade(factory))
                            .into(rankIcon)

                    cardView.setCardBackgroundColor(resources.getColor(Ranks.getRankColor(seasonStats.getHighestRank())))

                    injector.text(R.id.playerListSubtitle, seasonStats.getHighestRank().title + " " + seasonStats.getHighestRankLevel())

                    mSharedPreferences?.edit()?.putString("playerRankTitle-${player.playerID}", seasonStats.getHighestRankTitle())?.apply()

                    cardView.setOnLongClickListener {
                        MaterialDialog(this@PlayerListDialog)
                                .title(text = player.playerName)
                                .message(text = "Only one player can be chosen as yours, this will be used to show your stats across different screens and features.")
                                .positiveButton(text = "Save") {
                                    val isChecked = it.isCheckPromptChecked()
                                    if (isChecked) {
                                        FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().currentUser?.uid}/pubgAccountID/accountID").setValue(player.playerID)
                                        FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().currentUser?.uid}/pubgAccountID/platform").setValue(player.platform.id)
                                        FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().currentUser?.uid}/pubgAccountID/region").setValue(player.defaultConsoleRegion)
                                    }

                                    if (!isChecked && player.playerID == userAccountID) {
                                        userAccountID = ""
                                        FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().currentUser?.uid}/pubgAccountID").removeValue()
                                    }
                                }
                                .neutralButton(text = "Delete") {
                                    val index = players.indexOf(player)
                                    players.removeAt(index)
                                    mAdapter.notifyItemRemoved(index)

                                    FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().currentUser?.uid}/pubgPlayers/${player.playerID}").removeValue()

                                    if (player.playerID == userAccountID) {
                                        userAccountID = ""
                                        FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().currentUser?.uid}/pubgAccountID").removeValue()
                                    }
                                }
                                .checkBoxPrompt(text = "This is my player", isCheckedDefault = player.playerID == userAccountID) {}
                                .show()
                        true
                    }
                }
            })
        }

        playerListRV?.adapter = mAdapter

        playerListFAB.addActionItem(SpeedDialActionItem.Builder(R.id.fab_new_steam, R.drawable.windows_black)
                .setFabBackgroundColor(resources.getColor(R.color.md_light_blue_A400))
                .setLabel("Add Steam Player")
                .setLabelColor(resources.getColor(R.color.md_black_1000))
                .setLabelBackgroundColor(resources.getColor(R.color.md_light_blue_A400))
                .create())

        playerListFAB.addActionItem(SpeedDialActionItem.Builder(R.id.fab_new_kakao, R.drawable.windows_black)
                .setFabBackgroundColor(resources.getColor(R.color.md_grey_500))
                .setLabel("Add Kakao Player")
                .setLabelColor(resources.getColor(R.color.md_black_1000))
                .setLabelBackgroundColor(resources.getColor(R.color.md_grey_500))
                .create())

        playerListFAB.addActionItem(SpeedDialActionItem.Builder(R.id.fab_new_xbox, R.drawable.ic_xbox_black)
                .setFabBackgroundColor(resources.getColor(R.color.md_green_A700))
                .setLabel("Add Xbox Player")
                .setLabelColor(resources.getColor(R.color.md_black_1000))
                .setLabelBackgroundColor(resources.getColor(R.color.md_green_A700))
                .create())

        playerListFAB.addActionItem(SpeedDialActionItem.Builder(R.id.fab_new_ps4, R.drawable.ic_icons8_playstation_black)
                .setFabBackgroundColor(resources.getColor(R.color.md_indigo_A400))
                .setLabel("Add PS4 Player")
                .setLabelColor(resources.getColor(R.color.md_black_1000))
                .setLabelBackgroundColor(resources.getColor(R.color.md_indigo_A400))
                .create())

        playerListFAB?.setOnActionSelectedListener {
            playerListFAB?.close(true)
            if (Premium.isUserLevel2() && players.size >= 15) {
                Snacky.builder().setActivity(this).warning().setText("Player Limit Reached").setAction("UPGRADE") {
                    startActivity<UpgradeActivity>()
                }.show()
                return@setOnActionSelectedListener true
            } else if (!Premium.isPremiumUser() && players.size >= 5) {
                Snacky.builder().setActivity(this).warning().setText("Player Limit Reached").setAction("UPGRADE") {
                    startActivity<UpgradeActivity>()
                }.show()
                return@setOnActionSelectedListener true
            }
            when (it.id) {
                R.id.fab_new_steam -> {
                    MaterialDialog(this@PlayerListDialog)
                            .title(text = "Add Steam Player")
                            .noAutoDismiss()
                            .input(hint = "Player Name (Case Sensitive)") { dialog, text ->
                                if (text.contains(regex = Regex("[.$#\\[\\]?/!@%^&*(){},<>;:'|]"))) {
                                    dialog.getInputField()!!.error = "Invalid Characters in Player Name"
                                    return@input
                                }
                                dialog.positiveButton(text = "Adding")
                                dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
                                dialog.getInputField()?.isEnabled = false

                                GlobalScope.launch(Dispatchers.Main) {
                                    val mapping = isUserAlreadyMapped(text.toString(), Platform.STEAM)
                                    if (mapping != null) {
                                        addExistingPlayerToUser(mapping.split(".")[1], Platform.STEAM, text.toString())
                                        dialog.dismiss()
                                    } else {
                                        //User doesn't exist
                                        addPlayer(dialog, text.toString(), Platform.STEAM)
                                    }
                                }
                            }
                            .positiveButton(text = "Add")
                            .show()
                }
                R.id.fab_new_kakao -> {
                    MaterialDialog(this@PlayerListDialog)
                            .title(text = "Add Kakao Player")
                            .noAutoDismiss()
                            .input(hint = "Player Name (Case Sensitive)") { dialog, text ->
                                if (text.contains(regex = Regex("[.$#\\[\\]?/!@%^&*(){},<>;:'|]"))) {
                                    dialog.getInputField()!!.error = "Invalid Characters in Player Name"
                                    return@input
                                }
                                dialog.positiveButton(text = "Adding")
                                dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
                                dialog.getInputField()?.isEnabled = false

                                GlobalScope.launch(Dispatchers.Main) {
                                    val mapping = isUserAlreadyMapped(text.toString(), Platform.KAKAO)
                                    if (mapping != null) {
                                        addExistingPlayerToUser(mapping.split(".")[1], Platform.KAKAO, text.toString())
                                        dialog.dismiss()
                                    } else {
                                        //User doesn't exist
                                        addPlayer(dialog, text.toString(), Platform.KAKAO)
                                    }
                                }
                            }
                            .positiveButton(text = "Add")
                            .show()
                }
                R.id.fab_new_xbox -> {
                    MaterialDialog(this@PlayerListDialog)
                            .title(text = "First Things First...")
                            .message(text = "Select a default region for this player.")
                            .listItemsSingleChoice(items = Regions.xboxShardNames.toList()) { _, index, text ->
                                MaterialDialog(this@PlayerListDialog)
                                        .title(text = "Add Xbox Player (${Regions.shortXboxShardIDs[index].toUpperCase()})")
                                        .noAutoDismiss()
                                        .input(hint = "Player Name (Case Sensitive)") { dialog, text ->
                                            if (text.contains(regex = Regex("[.$#\\[\\]?/!@%^&*(){},<>;:'|]"))) {
                                                dialog.getInputField()!!.error = "Invalid Characters in Player Name"
                                                return@input
                                            }
                                            dialog.positiveButton(text = "Adding")
                                            dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
                                            dialog.getInputField()?.isEnabled = false

                                            GlobalScope.launch(Dispatchers.Main) {
                                                val mapping = isUserAlreadyMapped(text.toString(), Platform.XBOX)
                                                if (mapping != null) {
                                                    addExistingPlayerToUser(mapping.split(".")[1], Platform.XBOX, text.toString(), Regions.shortXboxShardIDs[index])
                                                    dialog.dismiss()
                                                } else {
                                                    //User doesn't exist
                                                    addPlayer(dialog, text.toString(), Platform.XBOX, Regions.shortXboxShardIDs[index])
                                                }
                                            }
                                        }
                                        .positiveButton(text = "Add")
                                        .show()
                            }
                            .show()
                }
                R.id.fab_new_ps4 -> {
                    MaterialDialog(this@PlayerListDialog)
                            .title(text = "First Things First...")
                            .message(text = "Select a default region for this player.")
                            .listItemsSingleChoice(items = Regions.psnShardNames.toList()) { _, index, text ->
                                MaterialDialog(this@PlayerListDialog)
                                        .title(text = "Add PS4 Player (${Regions.shortPSNShardIDs[index].toUpperCase()})")
                                        .noAutoDismiss()
                                        .input(hint = "Player Name (Case Sensitive)") { dialog, text ->
                                            if (text.contains(regex = Regex("[.$#\\[\\]?/!@%^&*(){},<>;:'|]"))) {
                                                dialog.getInputField()!!.error = "Invalid Characters in Player Name"
                                                return@input
                                            }
                                            dialog.positiveButton(text = "Adding")
                                            dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
                                            dialog.getInputField()?.isEnabled = false

                                            GlobalScope.launch(Dispatchers.Main) {
                                                val mapping = isUserAlreadyMapped(text.toString(), Platform.PS4)
                                                if (mapping != null) {
                                                    addExistingPlayerToUser(mapping.split(".")[1], Platform.PS4, text.toString(), Regions.shortPSNShardIDs[index])
                                                    dialog.dismiss()
                                                } else {
                                                    //User doesn't exist
                                                    addPlayer(dialog, text.toString(), Platform.PS4, Regions.shortPSNShardIDs[index])
                                                }
                                            }
                                        }
                                        .positiveButton(text = "Add")
                                        .show()
                            }
                            .show()
                }
            }

            return@setOnActionSelectedListener true
        }

        when (Premium.getUserLevel()) {
            Premium.Level.LEVEL_1,
            Premium.Level.FREE -> playerListLimitTV.text = "LIMIT OF 5 PLAYERS  \uD83C\uDF57"
            Premium.Level.LEVEL_2 -> playerListLimitTV.text = "LIMIT OF 15 PLAYERS  \uD83C\uDF57"
            else -> playerListLimitTV.text = "UNLIMITED PLAYERS 4 U!  \uD83C\uDF57"
        }
    }

    private fun addExistingPlayerToUser(playerID: String, platform: Platform, playerName: String, regionID: String? = null) {
        val updates: MutableMap<String, Any> = HashMap()
        updates["users/${FirebaseAuth.getInstance().currentUser!!.uid}/pubgPlayers/$playerID/platform"] = platform.id
        updates["users/${FirebaseAuth.getInstance().currentUser!!.uid}/pubgPlayers/$playerID/playerName"] = playerName
        updates["users/${FirebaseAuth.getInstance().currentUser!!.uid}/pubgPlayers/$playerID/region"] = regionID ?: ""
        mDatabase!!.reference.updateChildren(updates)
    }

    private var listener: ValueEventListener? = null
    private var listenerRef: DatabaseReference? = null

    private fun loadPlayers() {
        if (listener != null && listenerRef != null) {
            listenerRef!!.removeEventListener(listener!!)
            listener = null
            listenerRef = null
        }
        try {
            players.clear()
        } catch (e: Exception) {
        }
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener {
                loadPlayers()
            }
            return
        }

        val ref = FirebaseDatabase.getInstance().reference.child("users").child(currentUser.uid)
        listenerRef = ref.ref
        listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (!p0.exists()) {
                    return
                }

                players.clear()

                if (p0.hasChild("pubgAccountID/accountID")) {
                    userAccountID = p0.child("pubgAccountID/accountID").value.toString()
                }

                val children = p0.child("pubgPlayers").children.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.child("playerName").value.toString() })

                for (item in children) {
                    val platform = if (item.child("platform").value.toString() == "psn") {
                        Platform.PS4
                    } else {
                        Platform.valueOf(item.child("platform").value.toString().toUpperCase())
                    }

                    val defaultConsoleRegion = if (item.child("region").value.toString().isEmpty()) {
                        "na"
                    } else {
                        item.child("region").value.toString()
                    }

                    val player = PlayerListModel(
                            playerID = item.key.toString(),
                            playerIDAccount = "account.${item.key.toString()}",
                            playerName = item.child("playerName").value.toString(),
                            platform = platform,
                            defaultConsoleRegion = defaultConsoleRegion,
                            isPlayerCurrentUser = item.key.toString() == userAccountID
                    )

                    players.add(player)
                }

                mAdapter.notifyDataSetChanged()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        loadPlayers()
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(R.anim.nav_default_pop_enter_anim, R.anim.nav_default_pop_exit_anim)
    }

    override fun onStop() {
        super.onStop()
        if (listener != null && listenerRef != null) {
            listenerRef!!.removeEventListener(listener!!)
            listener = null
            listenerRef = null
        }

        if (listeners.isNotEmpty()) {
            for (item in listeners) {
                item.key.removeEventListener(item.value)
            }
        }
    }

    private fun addPlayer(dialog: MaterialDialog, playerName: String, platform: Platform, regionID: String = "") {
        addPlayerByName(playerName, platform, regionID).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                dialog.getInputField()!!.error = "No player found for \"$playerName\". Check capitalization and try again."
                dialog.positiveButton(text = "Add")
                dialog.setActionButtonEnabled(WhichButton.POSITIVE, true)
                dialog.getInputField()?.isEnabled = true

                return@addOnCompleteListener
            }
            dialog.dismiss()
            bypassCache = false
            playerListFAB.mainFabClosedBackgroundColor = resources.getColor(R.color.md_green_A700)
        }
    }

    private fun addPlayerByName(playerName: String, platform: Platform, regionID: String = ""): Task<Map<String, Any>> {
        val data = HashMap<String, Any>()
        data["playerName"] = playerName
        data["platform"] = platform.id
        data["regionID"] = regionID

        return FirebaseFunctions.getInstance().getHttpsCallable("addPlayerUsingName").call(data).continueWith { task ->
            val result = task.result
                    ?.data as Map<String, Any>
            result
        }
    }



    /**
     * @param playerName Takes string of playerName inputted from dialog.
     * @param platform Takes platform
     *
     * @return Account ID string if user already exists, empty string is not.
     */
    private suspend fun isUserAlreadyMapped(playerName: String, platform: Platform) : String? {
        return suspendCoroutine {
            if (bypassCache)  {
                it.resume(null)
                return@suspendCoroutine
            }
            mDatabase!!.getReference("playerNameMapping").child(platform.id).child(playerName).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (!p0.exists()) {
                        //Player is not in database, return null.
                        it.resume(null)
                        return
                    }
                    it.resume(p0.value.toString())
                }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.player_list_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private var bypassCache: Boolean = false

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.cache_bypass) {
            val dialog = MaterialDialog(this@PlayerListDialog)
                    .title(text = "Bypass Cache")
                    .message(text = "The cache only needs to be bypassed if the stats you are getting are not for the correct player, most likely a player name change has happened and the cache needs updated.\n\nAfter clicking bypass, add a player like normal, this setting will reset once you add a player.")
                    .positiveButton(text = "BYPASS") {
                        bypassCache = true
                        playerListFAB.mainFabClosedBackgroundColor = resources.getColor(R.color.timelineRed)
                    }
                    .neutralButton(text = "CANCEL BYPASS") {
                        bypassCache = false
                        playerListFAB.mainFabClosedBackgroundColor = resources.getColor(R.color.md_green_A700)
                    }
            dialog.setActionButtonEnabled(WhichButton.NEUTRAL, bypassCache)
            dialog.show()
            return true
        }

        if (item?.itemId == R.id.maps_manager) {
            startActivity<MapDownloadActivity>()
        }
        return super.onOptionsItemSelected(item)
    }
}