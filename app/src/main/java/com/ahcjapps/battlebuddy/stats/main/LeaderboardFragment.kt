package com.ahcjapps.battlebuddy.stats.main

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.ahcjapps.battlebuddy.R
import com.ahcjapps.battlebuddy.models.PlayerListModel
import com.ahcjapps.battlebuddy.viewmodels.PlayerStatsViewModel
import com.ahcjapps.battlebuddy.viewmodels.models.LeaderboardPlayer
import kotlinx.android.synthetic.main.fragment_matches_list.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.support.v4.runOnUiThread
import java.util.*
import kotlin.concurrent.schedule

class LeaderboardFragment : Fragment() {

    private val viewModel: PlayerStatsViewModel by lazy {
        ViewModelProviders.of(this).get(PlayerStatsViewModel::class.java)
    }

    private lateinit var mPlayer: PlayerListModel
    private lateinit var mAdapter: SlimAdapter

    var isLoaded = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (arguments == null) return null
        return inflater.inflate(R.layout.fragment_leaderboards_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()

        val mActivity: StatsHome? = activity as StatsHome

        var timer: TimerTask? = null
        timer = Timer("Refreshing", false).schedule(250) {
            if (isAdded)
                runOnUiThread {
                    mActivity?.setRefreshing(true)
                }
        }

        viewModel.getLeaderboards(mPlayer, mActivity?.application
                ?: requireContext().applicationContext)
        viewModel.leaderboardData.observe(this, Observer {
            mAdapter.updateData(it.playerList)

            if (!isLoaded)
                matches_RV?.scheduleLayoutAnimation()

            mActivity?.setRefreshing(false)

            isLoaded = true

            if (isAdded)
                timer.cancel()
        })
    }

    fun setupAdapter() {
        val gridLayoutManager: GridLayoutManager
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridLayoutManager = GridLayoutManager(requireContext(), 1)
            //gridLayoutManager.stackFromEnd = true
            //gridLayoutManager.reverseLayout = true
            matches_RV?.layoutManager = gridLayoutManager
        } else {
            gridLayoutManager = GridLayoutManager(requireContext(), 2)
            //gridLayoutManager.stackFromEnd = true
            //gridLayoutManager.reverseLayout = true
            matches_RV?.layoutManager = gridLayoutManager
        }

        matches_RV?.layoutAnimation = android.view.animation.AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down)

        mAdapter = SlimAdapter.create().attachTo(matches_RV).register(R.layout.leaderboard_player_item) { data: LeaderboardPlayer, injector ->
            injector.text(R.id.leaderboardRank, "Points: ${data.attributes.stats.rankPoints} • Wins: ${data.attributes.stats.wins} • Kills: ${data.attributes.stats.kills}")
            injector.text(R.id.leaderboardName, "#${data.attributes.rank} ${data.attributes.name}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null && arguments!!.containsKey("selectedPlayer")) {
            mPlayer = arguments!!.getSerializable("selectedPlayer") as PlayerListModel
        } else {
            return
        }
    }
}