package com.brokenstrawapps.battlebuddy.stats.scanner

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import com.brokenstrawapps.battlebuddy.R
import com.brokenstrawapps.battlebuddy.models.PlayerListModel
import com.brokenstrawapps.battlebuddy.models.PlayerStats
import com.brokenstrawapps.battlebuddy.utils.*
import com.brokenstrawapps.battlebuddy.viewmodels.models.PlayerModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.android.synthetic.main.activity_scanned_results.*
import kotlinx.android.synthetic.main.activity_scanned_results.toolbar
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.viewinjector.IViewInjector
import org.jetbrains.anko.appcompat.v7.navigationIconResource
import org.jetbrains.anko.toast
import timber.log.Timber
import java.io.File
import java.util.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ScannedResults : AppCompatActivity() {

    lateinit var mAdapter: SlimAdapter
    private var players: MutableList<ScannedListModel> = ArrayList()
    private var region = Platform.STEAM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanned_results)
        setSupportActionBar(toolbar)
        toolbar.navigationIconResource = R.drawable.ic_arrow_back_24dp
        toolbar.setNavigationOnClickListener { onBackPressed() }

        if (intent.hasExtra("path")) {
            loadModel(intent.extras?.getString("path").toString())
        }

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            scannedRV?.layoutManager = GridLayoutManager(this, 1)
        } else {
            scannedRV?.layoutManager = GridLayoutManager(this, 2)
        }

        mAdapter = SlimAdapter.create().attachTo(scannedRV).updateData(players).register<ScannedListModel>(R.layout.scanned_list_player) { player, injector ->
            injector.visible(R.id.player_pg)

            val cardView = injector.findViewById<CardView>(R.id.playerListCard)

            val rankIcon = injector.findViewById<ImageView>(R.id.game_version_icon)
            val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()

            injector.text(R.id.player_select_name, "${player.scannedName}")

            FirebaseDatabase.getInstance().getReference("playerNameMapping").child(region.id).child(player.scannedName).addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (!p0.exists()) {
                        //Player is not in database, return null.
                        addPlayer(injector, player.scannedName, Platform.STEAM)
                        return
                    }

                    player.player = PlayerListModel(
                            playerID = p0.value.toString().removeRange(0..p0.value.toString().indexOf(".")),
                            playerIDAccount = "account.${p0.value.toString()}",
                            playerName = player.scannedName,
                            platform = region
                    )

                    //Player Exists
                    FirebaseDatabase.getInstance().getReference("user_stats/${player.player!!.playerID}/season_data/${region.id}/${Seasons.getCurrentSeasonForPlatform(region).codeString}").addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if (!p0.exists()) {
                                if (!isDestroyed) {
                                    Glide.with(this@ScannedResults)
                                            .load(Ranks.getRankIcon(0.0))
                                            .transition(DrawableTransitionOptions.withCrossFade(factory))
                                            .into(rankIcon)
                                }

                                cardView.setCardBackgroundColor(resources.getColor(Ranks.getRankColor(null)))

                                injector.visible(R.id.player_pg)

                                player.player!!.runGetStats().addOnSuccessListener {
                                    injector.gone(R.id.player_pg)
                                }.addOnFailureListener {
                                    injector.gone(R.id.player_pg)
                                }
                                return
                            }

                            val playerStats = PlayerModel()

                            playerStats.lastUpdated = p0.child("lastUpdated").value.toString().toLongOrNull() ?: 0

                            Timber.d(player.player!!.playerID)
                            Timber.d(p0.value.toString())

                            playerStats.soloStats = p0.child("stats/solo").getValue(PlayerStats::class.java) ?: PlayerStats()
                            playerStats.soloFPPStats = p0.child("stats/solo-fpp").getValue(PlayerStats::class.java)?: PlayerStats()
                            playerStats.duoStats = p0.child("stats/duo").getValue(PlayerStats::class.java)?: PlayerStats()
                            playerStats.duoFPPStats = p0.child("stats/duo-fpp").getValue(PlayerStats::class.java)?: PlayerStats()
                            playerStats.squadStats = p0.child("stats/squad").getValue(PlayerStats::class.java)?: PlayerStats()
                            playerStats.squadFPPStats = p0.child("stats/squad-fpp").getValue(PlayerStats::class.java)?: PlayerStats()

                            injector.gone(R.id.player_pg)

                            injector.text(R.id.statsKills, playerStats.getOverallStats().kills.toString())
                            injector.text(R.id.statsWins, playerStats.getOverallStats().wins.toString())
                            injector.text(R.id.statsTopWins, playerStats.getOverallStats().top10s.toString())
                            injector.text(R.id.statsHeadshots, playerStats.getOverallStats().headshotKills.toString())
                            if (playerStats.getOverallStats().kills != 0 && playerStats.getOverallStats().losses != 0) injector.text(R.id.statsKD, String.format("%.2f", playerStats.getOverallStats().kills.toDouble() / playerStats.getOverallStats().losses.toDouble()))

                            Timber.d(playerStats.getOverallStats().toString())

                            cardView.setCardBackgroundColor(resources.getColor(Ranks.getRankColor(playerStats.getHighestRank())))

                            Glide.with(applicationContext)
                                    .load(Ranks.getRankIcon(playerStats.getHighestRank()))
                                    .transition(DrawableTransitionOptions.withCrossFade(factory))
                                    .into(rankIcon)

                            injector.text(R.id.playerListSubtitle, playerStats.getHighestRank().title + " " + playerStats.getHighestRankLevel())
                        }
                    })
                }
            })
        }
    }

    private fun loadModel(path: String) {
        Log.d("IMAGE", path)
        val image = FirebaseVisionImage.fromFilePath(this, Uri.fromFile(File(path)))
        val recognizer = FirebaseVision.getInstance().onDeviceTextRecognizer
        recognizer.processImage(image).addOnSuccessListener {
            val blocks = it.textBlocks
            if (blocks.size == 0) {
                toast("No text found.")
                return@addOnSuccessListener
            }

            for (block in blocks) {
                for (line in block.lines) {
                    val lineText = line.text

                    Log.d("IMAGE", lineText)
                    val text = cleanLine(lineText)
                    players.add(ScannedListModel(scannedName = text))
                }
            }

            mAdapter.updateData(players)
        }.addOnFailureListener {
            Log.d("IMAGE", "error", it)
        }
    }

    private fun cleanLine(lineText: String): String {
        if (lineText.length < 3) return lineText
        Timber.d("${lineText[0].isDigit()} -- ${lineText[1].isWhitespace()}")
        return if (lineText[0].isDigit() && lineText[1].isWhitespace()) {
            lineText.removeRange(0..1)
        } else {
            lineText
        }
    }

    private fun addPlayer(injector: IViewInjector<IViewInjector<*>>, playerName: String, platform: Platform, regionID: String = "") {
        addPlayerByName(playerName, platform, regionID).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                injector.text(R.id.playerListSubtitle, "Player Not Found")
                return@addOnCompleteListener
            }
        }
    }

    private fun addPlayerByName(playerName: String, platform: Platform, regionID: String = ""): Task<Map<String, Any>> {
        val data = HashMap<String, Any>()
        data["playerName"] = playerName
        data["platform"] = platform.id
        data["regionID"] = regionID

        return FirebaseFunctions.getInstance().getHttpsCallable("addPlayerByNameToMapping").call(data).continueWith { task ->
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
            FirebaseDatabase.getInstance().getReference("playerNameMapping").child(platform.id).child(playerName).addListenerForSingleValueEvent(object : ValueEventListener {
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

    data class ScannedListModel(
            val scannedName: String,
            var player: PlayerListModel? = null
    )
}
