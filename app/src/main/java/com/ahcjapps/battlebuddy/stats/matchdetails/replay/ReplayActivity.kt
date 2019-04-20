package com.ahcjapps.battlebuddy.stats.matchdetails.replay

import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.text.format.DateUtils
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.expandable.expandableItem
import co.zsmb.materialdrawerkt.draweritems.switchable.secondarySwitchItem
import co.zsmb.materialdrawerkt.draweritems.switchable.switchItem
import com.ahcjapps.battlebuddy.R
import com.ahcjapps.battlebuddy.Telemetry
import com.ahcjapps.battlebuddy.map.Map
import com.ahcjapps.battlebuddy.models.LogCharacter
import com.ahcjapps.battlebuddy.utils.Stopwatch
import com.ahcjapps.battlebuddy.viewmodels.models.MatchModel
import com.davemorrissey.labs.subscaleview.ImageSource
import com.mikepenz.materialdrawer.Drawer
import kotlinx.android.synthetic.main.activity_replay.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.forEachChildWithIndex
import org.jetbrains.anko.textColor
import org.jetbrains.anko.topPadding
import org.json.JSONException
import java.io.File


class ReplayActivity : AppCompatActivity() {
    var match: MatchModel = MatchGod.match!!

    lateinit var mDrawer: Drawer
    lateinit var mRightDrawer: Drawer

    var replaySettings = ReplaySettings()

    var currentProgress = 0
    var currentChrono = SystemClock.elapsedRealtime()
    var speedMultiplier = 5

    var isPlaying = false

    var stopwatch = Stopwatch()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_replay)
        setSupportActionBar(replay_toolbar)

        setupDrawer()
        setupRightDrawer()
        setupMap()
        setupSeeker()
        setupSpeedListeners()
    }

    private fun setupRightDrawer() {
        var characterList: MutableList<LogCharacter> = ArrayList()

        mRightDrawer = drawer {
            primaryDrawer = mDrawer
            gravity = Gravity.END
        }

        mRightDrawer.recyclerView?.clipToPadding = false
        mRightDrawer.recyclerView?.setPadding(0, mRightDrawer.recyclerView?.topPadding!!, 0, 24)

        val mRightAdapter = SlimAdapter.create().attachTo(mRightDrawer.recyclerView).updateData(characterList).register<LogCharacter>(R.layout.replay_drawer_player) { data, injector ->
            injector.text(R.id.playerName, data.name)

            val color = match.teamColors[data.teamId]

            val circle = injector.findViewById<ImageView>(R.id.playerColor)
            circle.setColorFilter(color ?: R.color.md_white_1000, PorterDuff.Mode.MULTIPLY)

            injector.clicked(R.id.playerLayout) {
                replay_map.zoomToPlayer(data, this)
            }
        }

        mRightDrawer.recyclerView?.adapter = mRightAdapter

        characterList = if (match.attributes?.gameMode?.contains("solo") == false) {
            match.logPlayerCreate.sortedWith(compareBy({ it.teamId }, { it.name.toLowerCase() })).toMutableList()
        } else {
            match.logPlayerCreate.sortedWith(compareBy { it.name.toLowerCase() }).toMutableList()
        }

        mRightAdapter.updateData(characterList)
    }

    private fun setupSpeedListeners() {
        speed1X?.setOnClickListener {
            stopwatch.speedMultiplier = 0
            replaySettings.speedMultiplier = 1
            speed1X?.textColor = resources.getColor(R.color.md_orange_500)
            speedLayout.forEachChildWithIndex { i, view ->
                if (i != speedLayout.indexOfChild(speed1X)) (view as TextView).textColor = resources.getColor(R.color.md_white_1000)
            }
        }

        speed2X?.setOnClickListener {
            stopwatch.speedMultiplier = 2
            replaySettings.speedMultiplier = 2
            speed2X?.textColor = resources.getColor(R.color.md_orange_500)
            speedLayout.forEachChildWithIndex { i, view ->
                if (i != speedLayout.indexOfChild(speed2X)) (view as TextView).textColor = resources.getColor(R.color.md_white_1000)
            }
        }

        speed5X?.setOnClickListener {
            stopwatch.speedMultiplier = 5
            replaySettings.speedMultiplier = 5
            speed5X?.textColor = resources.getColor(R.color.md_orange_500)
            speedLayout.forEachChildWithIndex { i, view ->
                if (i != speedLayout.indexOfChild(speed5X)) (view as TextView).textColor = resources.getColor(R.color.md_white_1000)
            }
        }

        speed10X?.setOnClickListener {
            stopwatch.speedMultiplier = 10
            replaySettings.speedMultiplier = 10
            speed10X?.textColor = resources.getColor(R.color.md_orange_500)
            speedLayout.forEachChildWithIndex { i, view ->
                if (i != speedLayout.indexOfChild(speed10X)) (view as TextView).textColor = resources.getColor(R.color.md_white_1000)
            }
        }

        speed20X?.setOnClickListener {
            stopwatch.speedMultiplier = 20
            replaySettings.speedMultiplier = 20
            speed20X?.textColor = resources.getColor(R.color.md_orange_500)
            speedLayout.forEachChildWithIndex { i, view ->
                if (i != speedLayout.indexOfChild(speed20X)) (view as TextView).textColor = resources.getColor(R.color.md_white_1000)
            }
        }
    }

    private fun setupSeeker() {
        stopwatch.setTextView(replay_seek_time)
        replay_seek?.max = match.attributes?.duration!!.toInt()
        replay_seek?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                stopwatch.setElapsedTime(progress * 1000.toLong(), true)
                currentProgress = progress
                if (fromUser) updateMap()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (stopwatch.isStarted && !stopwatch.isPaused)
                stopwatch.pause()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (isPlaying && stopwatch.isPaused) {
                    stopwatch.resume()
                }
            }
        })

        replay_playPause?.setOnClickListener {
            if (isPlaying) {
                stopwatch.pause()

                isPlaying = false
                replay_playPause?.setImageResource(R.drawable.ic_play_arrow_24dp)
            } else {
                if (stopwatch.isStarted) stopwatch.resume()
                else stopwatch.start(true)

                isPlaying = true
                replay_playPause?.setImageResource(R.drawable.ic_pause_24dp)
            }
        }

        stopwatch.setOnTickListener {
            updateMap()
            replay_seek?.progress = (it.elapsedTime / 1000).toInt()
            if (it.elapsedTime / 1000 >= match.attributes?.duration!!) {
                stopwatch.stop()
                stopwatch.setElapsedTime(0, false)
                isPlaying = false
                replay_playPause?.setImageResource(R.drawable.ic_play_arrow_24dp)
                replay_seek_time?.text = DateUtils.formatElapsedTime(match.attributes?.duration!!)
            }
        }
    }

    private fun updateMap() {
        replaySettings.elapsedSeconds = currentProgress.toLong()
        replay_map.updateMap(replaySettings)

        val gameState = match.gameStates.findLast { it.gameState.elapsedTime <= currentProgress.toLong() }

        if (gameState != null)
        replayAliveTV?.text = "${gameState!!.gameState.numAlivePlayers} Alive • ${gameState!!.gameState.numAliveTeams} Teams"
    }

    private fun setupMap() {
        var map: Map? = Map.ERANGEL_LOW
        for (item in Map.values()) {
            if (item.fileName == match.getMapAsset(this)) map = item
        }

        val offset = when (map) {
            Map.MIRAMAR_LOW -> 4.048
            Map.SANHOK_LOW -> 3.41333333
            Map.VIKENDI_LOW -> 3.072
            Map.SANHOK_HIGH -> 0.5
            else -> 1.0
        }

        replay_map.match = match
        replay_map.offset = offset.toFloat()
        replay_map.setImage(ImageSource.uri(Uri.fromFile(File(filesDir, match.getMapAsset(this)))))
    }

    private fun setupDrawer() {
        mDrawer = drawer {
            toolbar = replay_toolbar
            headerViewRes = R.layout.drawer_header
            headerDivider = false
            stickyFooterShadow = false
            selectedItem = -1
            switchItem("Show Red Zones") {
                icon = R.drawable.explosion_color
                selectable = false
                checked = true
                onSwitchChanged { drawerItem, button, isEnabled ->
                    replaySettings.circleSettings.showRedZones = isEnabled
                    updateMap()
                }
            }
            expandableItem("Bluezone") {
                icon = R.drawable.lightning
                selectable = false
                secondarySwitchItem("Show Safe Zones") {
                    selectable = false
                    checked = true
                    onSwitchChanged { drawerItem, button, isEnabled ->
                        replaySettings.circleSettings.showSafeZones = isEnabled
                        updateMap()
                    }
                }
                secondarySwitchItem("Show Blue Circle") {
                    selectable = false
                    checked = true
                    onSwitchChanged { drawerItem, button, isEnabled ->
                        replaySettings.circleSettings.showBlueCircle = isEnabled
                        updateMap()
                    }
                }
            }
            switchItem("Show Care Packages") {
                icon = R.drawable.carepackage_open
                selectable = false
                checked = true
                onSwitchChanged { drawerItem, button, isEnabled ->
                    replaySettings.showCarePackages = isEnabled
                    updateMap()
                }
            }
        }


        mDrawer.header.findViewById<TextView>(R.id.header_name)
        try {
            mDrawer.header.findViewById<TextView>(R.id.header_name)?.text = Telemetry().gameModes[match.attributes?.gameMode].toString()
        } catch (e: JSONException) {
            mDrawer.header.findViewById<TextView>(R.id.header_name)?.visibility = View.INVISIBLE
        }

        mDrawer.header.findViewById<TextView>(R.id.header_upgrade)?.text = DateUtils.formatElapsedTime(match.attributes?.duration!!)
        mDrawer.header.findViewById<TextView>(R.id.header_ingame_name)?.text = match.getFormattedCreatedAt()
        mDrawer.header.findViewById<TextView>(R.id.header_region)?.text = Telemetry().region[match.attributes?.shardId].toString()
        mDrawer.header.findViewById<ImageView>(R.id.header_icon)?.setImageDrawable(resources.getDrawable(match.getMapIcon()))

        replay_time?.text = DateUtils.formatElapsedTime(match.attributes?.duration!!)
    }
}