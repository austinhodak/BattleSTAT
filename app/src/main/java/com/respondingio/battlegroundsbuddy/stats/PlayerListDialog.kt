package com.respondingio.battlegroundsbuddy.stats

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.HORIZONTAL
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.models.PrefPlayer
import com.respondingio.battlegroundsbuddy.models.Seasons
import com.respondingio.battlegroundsbuddy.snacky.Snacky
import kotlinx.android.synthetic.main.activity_stats_main_new.*
import kotlinx.android.synthetic.main.dialog_player_list.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.appcompat.v7.navigationIconResource

class PlayerListDialog : DialogFragment() {

    private var players: MutableList<PrefPlayer> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.PlayerListDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_player_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.navigationIconResource = R.drawable.instabug_ic_close
        toolbar.setNavigationOnClickListener { dialog.dismiss() }

        val dividerItemDecoration = DividerItemDecoration(playerListRV?.context, HORIZONTAL)

        playerListRV?.addItemDecoration(dividerItemDecoration)
        playerListRV?.layoutManager = LinearLayoutManager(requireActivity())
        playerListRV?.adapter = SlimAdapter.create().attachTo(playerListRV).register<PrefPlayer>(R.layout.player_list_item) { player, injector ->
            val iconDrawable: Int = if (player.defaultShardID.contains("pc")) {
                R.drawable.rank_icon_grandmaster
            } else {
                R.drawable.unranked
            }
            injector.image(R.id.game_version_icon, iconDrawable)
            injector.text(R.id.player_select_name, player.playerName)

            injector.clicked(R.id.constraintLayout) {

            }
        }.updateData(players)

        loadPlayers()
    }

    private var listener: ChildEventListener? = null
    private var listenerRef: DatabaseReference? = null

    private fun loadPlayers() {
        if (listener != null && listenerRef != null) {
            listenerRef!!.removeEventListener(listener!!)
            listener = null
            listenerRef = null
        }
        try {
            players.clear()
        } catch (e: Exception) { }
        val currentUser = FirebaseAuth.getInstance().currentUser

        val ref = FirebaseDatabase.getInstance().reference.child("users").child(currentUser!!.uid).child("pubg_players").orderByChild("playerName")

        listenerRef = ref.ref
        listener = ref.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val player = PrefPlayer(
                        playerID = p0.key.toString(),
                        playerName = p0.child("playerName").value.toString(),
                        defaultShardID = p0.child("shardID").value.toString(),
                        selectedGamemode = "solo"
                )

                players.add(player)

                playerListRV.adapter?.notifyDataSetChanged()
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window.setLayout(width, height)
        }
    }

    override fun onStop() {
        super.onStop()
        if (listener != null && listenerRef != null) {
            listenerRef!!.removeEventListener(listener!!)
            listener = null
            listenerRef = null
        }
    }
}