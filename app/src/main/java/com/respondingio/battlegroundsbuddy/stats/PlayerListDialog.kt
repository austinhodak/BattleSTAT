package com.respondingio.battlegroundsbuddy.stats

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.afollestad.materialdialogs.checkbox.isCheckPromptChecked
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.functions.FirebaseFunctions
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.models.PrefPlayer
import com.respondingio.battlegroundsbuddy.models.SeasonStatsAll
import com.respondingio.battlegroundsbuddy.models.Seasons
import com.respondingio.battlegroundsbuddy.premium.UpgradeActivity
import com.respondingio.battlegroundsbuddy.snacky.Snacky
import com.respondingio.battlegroundsbuddy.stats.main.StatsHome
import com.respondingio.battlegroundsbuddy.utils.Premium
import com.respondingio.battlegroundsbuddy.utils.Ranks
import jp.wasabeef.recyclerview.animators.FadeInAnimator
import kotlinx.android.synthetic.main.dialog_player_list.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.appcompat.v7.navigationIconResource
import org.jetbrains.anko.startActivity

class PlayerListDialog : AppCompatActivity() {

    private var players: MutableList<PrefPlayer> = ArrayList()
    lateinit var mAdapter: SlimAdapter
    private var userAccountID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.instabug_fadein, R.anim.instabug_fadeout)
        setContentView(R.layout.dialog_player_list)

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

        playerListRV?.itemAnimator = FadeInAnimator()

        mAdapter = SlimAdapter.create().attachTo(playerListRV).updateData(players).register<PrefPlayer>(R.layout.player_list_item) { player, injector ->
            val cardView = injector.findViewById<CardView>(R.id.playerListCard)
            injector.invisible(R.id.game_version_icon)
            cardView.setCardBackgroundColor(resources.getColor(Ranks.getRankColor(0.0)))

            if (userAccountID == player.playerID) {
                injector.text(R.id.player_select_name, "${player.playerName}  \uf2bd ")
            } else {
                injector.text(R.id.player_select_name, player.playerName)
            }

            injector.text(R.id.playerListSubtitle, regions[regionList.indexOf(player.defaultShardID)])

            injector.clicked(R.id.constraintLayout) {

            }

            var currentSeason = if (player.defaultShardID.contains("pc", true)) {
                Seasons.getInstance().pcCurrentSeason
            } else {
                Seasons.getInstance().xboxCurrentSeason
            }.toLowerCase()

            injector.gone(R.id.player_pg)

            Log.d("RANK", "${player.playerID} - ${player.defaultShardID.toLowerCase()} - $currentSeason")

            FirebaseDatabase.getInstance().getReference("user_stats/${player.playerID}/season_data/${player.defaultShardID.toLowerCase()}/$currentSeason/stats").addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    val rankIcon = injector.findViewById<ImageView>(R.id.game_version_icon)
                    val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()

                    if (!p0.exists()) {
                        Glide.with(this@PlayerListDialog)
                                .load(Ranks.getRankIcon(0.0))
                                .transition(DrawableTransitionOptions.withCrossFade(factory))
                                .into(rankIcon)

                        injector.invisible(R.id.game_version_icon)
                        cardView.setCardBackgroundColor(resources.getColor(Ranks.getRankColor(0.0)))

                        injector.visible(R.id.player_pg)

                        Log.d("PLAYER", "LOAD ${player.playerID} - ${player.selectedShardID} - ${player.selectedSeason}")

                        startPlayerStatsFunction(player.playerID, player.selectedShardID.toLowerCase(), player.selectedSeason!!)?.addOnSuccessListener {
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

                    cardView.setOnLongClickListener {
                        MaterialDialog(this@PlayerListDialog)
                                .title(text = player.playerName)
                                .message(text = "Only one player can be chosen as yours, this will be used to show your stats across different screens and features.")
                                .positiveButton(text = "Save") {
                                    val isChecked = it.isCheckPromptChecked()
                                    if (isChecked)
                                        FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().currentUser?.uid}/pubgAccountID").setValue(player.playerID)

                                    if (!isChecked && player.playerID == userAccountID) {
                                        FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().currentUser?.uid}/pubgAccountID").removeValue()
                                    }
                                }
                                .neutralButton(text = "Delete") {
                                    val index = players.indexOf(player)
                                    players.removeAt(index)
                                    mAdapter.notifyItemRemoved(index)

                                    if (player.playerID == userAccountID) {
                                        FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().currentUser?.uid}/pubgAccountID").removeValue()
                                    }

                                    FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().currentUser?.uid}/pubg_players/${player.playerID}").removeValue()
                                }
                                .checkBoxPrompt(text = "This is my player", isCheckedDefault = player.playerID == userAccountID) {}
                                .show()
                        true
                    }

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
                }
            })
        }

        playerListRV?.adapter = mAdapter

        playerListAdd?.setOnClickListener {
            val addPlayerBottomSheet = AddPlayerBottomSheet()
            if (players.size < 5 || Premium.isUserLevel(Premium.Level.LEVEL_3)) {
                //If players list is less than 5 players OR if user is LEVEL 3 (UNLIMITED)
                addPlayerBottomSheet.show(supportFragmentManager, addPlayerBottomSheet.tag)
            } else if (players.size < 15 && Premium.isUserLevel(Premium.Level.LEVEL_2)) {
                //Player size is less than 15 AND user is Level 2 (15 Players)
                addPlayerBottomSheet.show(supportFragmentManager, addPlayerBottomSheet.tag)
            } else {
                //Player size is at max and user isn't Level 2 or 3, show upgrade dialog.
                Snacky.builder().setView(playerListCord).warning().setDuration(BaseTransientBottomBar.LENGTH_LONG).setText("Player Limit Reached").setAction("UPGRADE") {
                    startActivity<UpgradeActivity>()
                }.show()
            }
        }

        playerListToolbarWaterfall?.scrollView = playerListScrollview

        when (Premium.getUserLevel()) {
            Premium.Level.LEVEL_1,
            Premium.Level.FREE -> playerListLimitTV.text = "LIMIT OF 5 PLAYERS \uD83C\uDF57 UPGRADE"
            Premium.Level.LEVEL_2 -> playerListLimitTV.text = "LIMIT OF 15 PLAYERS \uD83C\uDF57 UPGRADE"
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

        val ref = FirebaseDatabase.getInstance().reference.child("users").child(currentUser!!.uid)

        listenerRef = ref.ref
        listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (!p0.exists()) {
                    return
                }

                userAccountID = if (p0.hasChild("pubgAccountID")) {
                    p0.child("pubgAccountID").value.toString()
                } else {
                    ""
                }

                players.clear()

                for (child in p0.child("pubg_players").children) {
                    val player = PrefPlayer(
                            playerID = child.key.toString(),
                            playerName = child.child("playerName").value.toString(),
                            defaultShardID = child.child("shardID").value.toString().toUpperCase(),
                            selectedGamemode = "solo"
                    )

                    Log.d("PLAYER", player.playerID)

                    players.add(player)
                }

                players = players.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.playerName }).toMutableList()

                mAdapter.updateData(players)
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

    var regionList = arrayOf("XBOX-AS", "XBOX-EU", "XBOX-NA", "XBOX-OC", "XBOX-SA", "PC-KRJP", "PC-JP", "PC-NA", "PC-EU", "PC-RU", "PC-OC", "PC-KAKAO", "PC-SEA", "PC-SA", "PC-AS")
    var regions = arrayOf("Xbox Asia", "Xbox Europe", "Xbox North America", "Xbox Oceania", "Xbox South America", "PC Korea", "PC Japan", "PC North America", "PC Europe", "PC Russia", "PC Oceania", "PC Kakao", "PC South East Asia", "PC South and Central America", "PC Asia")
}