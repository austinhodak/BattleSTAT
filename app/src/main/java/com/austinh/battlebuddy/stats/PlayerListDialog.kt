package com.austinh.battlebuddy.stats

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.functions.FirebaseFunctions
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.austinh.battlebuddy.R
import com.austinh.battlebuddy.models.PrefPlayer
import com.austinh.battlebuddy.models.SeasonStatsAll
import com.austinh.battlebuddy.premium.UpgradeActivity
import com.austinh.battlebuddy.snacky.Snacky
import com.austinh.battlebuddy.stats.main.StatsHome
import com.austinh.battlebuddy.utils.Premium
import com.austinh.battlebuddy.utils.Ranks
import com.austinh.battlebuddy.utils.Regions
import kotlinx.android.synthetic.main.dialog_player_list.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.appcompat.v7.navigationIconResource
import org.jetbrains.anko.startActivity
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.set

class PlayerListDialog : AppCompatActivity() {

    private var players: MutableList<PrefPlayer> = ArrayList()
    lateinit var mAdapter: SlimAdapter
    private var userAccountID = ""

    private val listeners: MutableMap<DatabaseReference, ValueEventListener> = HashMap()
    private var mSharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.instabug_fadein, R.anim.instabug_fadeout)
        setContentView(R.layout.dialog_player_list)

        mSharedPreferences = this.getSharedPreferences("com.austinh.battlebuddy", Context.MODE_PRIVATE)

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


        //playerListRV?.layoutAnimation = android.view.animation.AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fall_down)

        mAdapter = SlimAdapter.create().attachTo(playerListRV).updateData(players).register<PrefPlayer>(R.layout.player_list_item) { player, injector ->
            val cardView = injector.findViewById<CardView>(R.id.playerListCard)

            val rankIcon = injector.findViewById<ImageView>(R.id.game_version_icon)
            val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()

            if (mSharedPreferences?.contains("playerRank-${player.playerID}") == true) {
                Glide.with(applicationContext)
                        .load(Ranks.getRankIcon(mSharedPreferences?.getFloat("playerRank-${player.playerID}", 0f)?.toDouble()
                                ?: 0.0))
                        //.transition(DrawableTransitionOptions.withCrossFade(factory))
                        .into(rankIcon)

                cardView.setCardBackgroundColor(resources.getColor(Ranks.getRankColor(mSharedPreferences?.getFloat("playerRank-${player.playerID}", 0f)?.toDouble()
                        ?: 0.0)))
            } else {
                injector.invisible(R.id.game_version_icon)
                cardView.setCardBackgroundColor(resources.getColor(Ranks.getRankColor(0.0)))
            }

            if (userAccountID == player.playerID) {
                injector.text(R.id.player_select_name, "${player.playerName}  \uf2bd ")
            } else {
                injector.text(R.id.player_select_name, player.playerName)
            }

            injector.text(R.id.playerListSubtitle, Regions.getNewRegionName(player.defaultShardID))

            cardView.setOnClickListener {
                if (intent.action != null) {
                    //Called to pick a player, return with selection.
                    val intent = Intent()
                    intent.putExtra("selectedPlayer", player)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else {
                    //Start Stats Activity with Selected Player
                    startActivity<StatsHome>("selectedPlayer" to player)
                }
            }

            val currentSeason = com.austinh.battlebuddy.utils.Seasons.getCurrentSeasonForShard(player.defaultShardID)

            injector.gone(R.id.player_pg)

            Log.d("RANK", "${player.playerID} - ${player.defaultShardID.toLowerCase()} - $currentSeason")

            if (player.selectedShardID.equals("xbox", true)) {
                player.selectedShardID = "xbox-na"
            }

            var searchShardID = ""

            if (player.defaultShardID == "xbox") {
                if (player.isSeasonNewFormat("xbox")) {
                    searchShardID = "xbox"
                } else {
                    searchShardID = player.oldXboxShard ?: "xbox-na"
                }
            } else if (player.defaultShardID == "psn")  {
                if (player.isSeasonNewFormat("psn")) {
                    searchShardID = "psn"
                } else {
                    searchShardID = player.oldXboxShard ?: "psn-na"
                }
            } else {
                searchShardID = player.defaultShardID
            }

            listeners[FirebaseDatabase.getInstance().getReference("user_stats/${player.playerID}/season_data/${searchShardID.toLowerCase()}/$currentSeason/stats")] = FirebaseDatabase.getInstance().getReference("user_stats/${player.playerID}/season_data/${searchShardID.toLowerCase()}/$currentSeason/stats").addValueEventListener(object : ValueEventListener {
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

                        Log.d("PLAYER", "LOAD ${player.playerID} - ${player.selectedShardID} - ${player.selectedSeason}")

                        startPlayerStatsFunction(player.playerID, searchShardID, player.selectedSeason!!)?.addOnSuccessListener {
                            injector.gone(R.id.player_pg)
                        }?.addOnFailureListener {
                            injector.gone(R.id.player_pg)
                        }
                        return
                    }

                    injector.gone(R.id.player_pg)

                    val seasonStats = p0.getValue(SeasonStatsAll::class.java)!!
                    val pointsList: MutableList<Double> = ArrayList()

                    pointsList.add(seasonStats.solo.rankPoints)
                    pointsList.add(seasonStats.`solo-fpp`.rankPoints)
                    pointsList.add(seasonStats.duo.rankPoints)
                    pointsList.add(seasonStats.`duo-fpp`.rankPoints)
                    pointsList.add(seasonStats.squad.rankPoints)
                    pointsList.add(seasonStats.`squad-fpp`.rankPoints)

                    pointsList.sort()
                    pointsList.reverse()

                    injector.visible(R.id.game_version_icon)

                    Glide.with(applicationContext)
                            .load(Ranks.getRankIcon(pointsList[0]))
                            .transition(DrawableTransitionOptions.withCrossFade(factory))
                            .into(rankIcon)

                    cardView.setCardBackgroundColor(resources.getColor(Ranks.getRankColor(pointsList[0])))

                    mSharedPreferences?.edit()?.putFloat("playerRank-${player.playerID}", pointsList[0].toFloat())?.apply()

                    Log.d("PLAYER", "ID: ${player.playerID}")

                    cardView.setOnLongClickListener {
                        MaterialDialog(this@PlayerListDialog)
                                .title(text = player.playerName)
                                .message(text = "Only one player can be chosen as yours, this will be used to show your stats across different screens and features.")
                                .positiveButton(text = "Save") {
                                    val isChecked = it.isCheckPromptChecked()
                                    if (isChecked) {
                                        FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().currentUser?.uid}/pubgAccountID/accountID").setValue(player.playerID)
                                        FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().currentUser?.uid}/pubgAccountID/shardID").setValue(player.defaultShardID.toLowerCase())
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

                                    if (player.playerID == userAccountID) {
                                        userAccountID = ""
                                        FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().currentUser?.uid}/pubgAccountID").removeValue()
                                    }

                                    FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().currentUser?.uid}/pubg_players/${player.playerID}").removeValue()
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
                                dialog.positiveButton(text = "Adding")
                                dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
                                addPlayerByName(text.toString(), "steam").addOnCompleteListener { task ->
                                    if (!task.isSuccessful) {
                                        dialog.getInputField()!!.error = "No player found for \"$text\". Please try again."
                                        dialog.positiveButton(text = "Add")
                                        dialog.setActionButtonEnabled(WhichButton.POSITIVE, true)

                                        return@addOnCompleteListener
                                    }
                                    dialog.dismiss()
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
                                dialog.positiveButton(text = "Adding")
                                dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
                                addPlayerByName(text.toString(), "kakao").addOnSuccessListener {
                                    dialog.dismiss()
                                }.addOnFailureListener {
                                    dialog.getInputField()!!.error = "No player found for \"$text\". Please try again."
                                    dialog.positiveButton(text = "Add")
                                    dialog.setActionButtonEnabled(WhichButton.POSITIVE, true)
                                }
                            }
                            .positiveButton(text = "Add")
                            .show()
                }
                R.id.fab_new_xbox -> {
                    MaterialDialog(this@PlayerListDialog)
                            .title(text = "Add Xbox Player")
                            .noAutoDismiss()
                            .input(hint = "Player Name (Case Sensitive)") { dialog, text ->
                                dialog.positiveButton(text = "Adding")
                                dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
                                addPlayerByName(text.toString(), "xbox").addOnSuccessListener {
                                    dialog.dismiss()
                                }.addOnFailureListener {
                                    dialog.getInputField()!!.error = "No player found for \"$text\". Please try again."
                                    dialog.positiveButton(text = "Add")
                                    dialog.setActionButtonEnabled(WhichButton.POSITIVE, true)
                                }
                            }
                            .positiveButton(text = "Add")
                            .show()
                }
                R.id.fab_new_ps4 -> {
                    MaterialDialog(this@PlayerListDialog)
                            .title(text = "Add PS4 Player")
                            .noAutoDismiss()
                            .input(hint = "Player Name (Case Sensitive)") { dialog, text ->
                                dialog.positiveButton(text = "Adding")
                                dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
                                addPlayerByName(text.toString(), "psn").addOnSuccessListener {
                                    dialog.dismiss()
                                }.addOnFailureListener {
                                    dialog.getInputField()!!.error = "No player found for \"$text\". Please try again."
                                    dialog.positiveButton(text = "Add")
                                    dialog.setActionButtonEnabled(WhichButton.POSITIVE, true)
                                }
                            }
                            .positiveButton(text = "Add")
                            .show()
                }
            }

            return@setOnActionSelectedListener true
        }

        when (Premium.getUserLevel()) {
            Premium.Level.LEVEL_1,
            Premium.Level.FREE -> playerListLimitTV.text = "LIMIT OF 5 PLAYERS \uD83C\uDF57"
            Premium.Level.LEVEL_2 -> playerListLimitTV.text = "LIMIT OF 15 PLAYERS \uD83C\uDF57"
            else -> playerListLimitTV.text = "UNLIMITED PLAYERS 4 U! \uD83C\uDF57"
        }
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

                if (p0.hasChild("pubgAccountID") && !p0.hasChild("pubgAccountID/accountID") && !p0.hasChild("pubgAccountID/shardID")) {
                    userAccountID = p0.child("pubgAccountID").value.toString()
                    p0.ref.child("pubgAccountID/accountID").setValue(userAccountID)
                }

                if (p0.hasChild("pubgAccountID/accountID")) {
                    userAccountID = p0.child("pubgAccountID/accountID").value.toString()
                }

                players.clear()

                for (child in p0.child("pubg_players").children) {
                    var shardID = child.child("shardID").value.toString().toLowerCase()
                    var oldXboxShard: String? = null
                    if (shardID.contains("kakao") && shardID != "kakao") {
                        child.ref.child("shardID").setValue("kakao")
                        shardID = "kakao"
                    } else if (shardID.contains("pc")) {
                        child.ref.child("shardID").setValue("steam")
                        shardID = "steam"
                    } else if (shardID.contains("xbox") && shardID != "xbox") {
                        child.ref.child("shardID").setValue("xbox")
                        child.ref.child("oldXboxShard").setValue(shardID)
                        oldXboxShard = shardID
                        shardID = "xbox"
                    } else if (shardID.contains("psn") && shardID != "psn") {
                        child.ref.child("shardID").setValue("psn")
                        child.ref.child("oldXboxShard").setValue(shardID)
                        oldXboxShard = shardID
                        shardID = "psn"
                    }

                    if (!p0.hasChild("pubgAccountID/shardID") && userAccountID == child.key) {
                        p0.ref.child("pubgAccountID/shardID").setValue(shardID)
                    }

                    val player = PrefPlayer(
                            playerID = child.key.toString(),
                            playerName = child.child("playerName").value.toString(),
                            defaultShardID = shardID.toLowerCase(),
                            selectedGamemode = "solo"
                    )

                    if (oldXboxShard != null) {
                        player.oldXboxShard = oldXboxShard
                    } else if (child.hasChild("oldXboxShard")) {
                        player.oldXboxShard = child.child("oldXboxShard").value.toString()
                    }

                    Log.d("PLAYER", player.playerID)

                    players.add(player)
                }

                players = players.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.playerName }).toMutableList()

                mAdapter.updateData(players)
                //if (!isDestroyed)
                //playerListRV?.scheduleLayoutAnimation()
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

    private fun startPlayerStatsFunction(playerID: String, shardID: String, seasonID: String): Task<Map<String, Any>>? {
        val data = java.util.HashMap<String, Any>()
        data["playerID"] = playerID
        data["shardID"] = shardID
        data["seasonID"] = seasonID

        return FirebaseFunctions.getInstance().getHttpsCallable("loadPlayerStats")?.call(data)?.continueWith { task ->
            val result = task.result?.data as Map<String, Any>
            Log.d("REQUEST", result.toString())
            result
        }
    }

    private fun addPlayerByName(playerName: String, shardID: String): Task<Map<String, Any>> {
        val data = HashMap<String, Any>()
        data["playerName"] = playerName
        data["shardID"] = shardID

        return FirebaseFunctions.getInstance().getHttpsCallable("addPlayerByName").call(data).continueWith { task ->
            val result = task.result
                    ?.data as Map<String, Any>
            result
        }
    }

    var regionList = arrayOf("XBOX-AS", "XBOX-EU", "XBOX-NA", "XBOX-OC", "XBOX-SA", "PC-KRJP", "PC-JP", "PC-NA", "PC-EU", "PC-RU", "PC-OC", "PC-KAKAO", "PC-SEA", "PC-SA", "PC-AS")
    var regions = arrayOf("Xbox Asia", "Xbox Europe", "Xbox North America", "Xbox Oceania", "Xbox South America", "PC Korea", "PC Japan", "PC North America", "PC Europe", "PC Russia", "PC Oceania", "PC Kakao", "PC South East Asia", "PC South and Central America", "PC Asia")
}