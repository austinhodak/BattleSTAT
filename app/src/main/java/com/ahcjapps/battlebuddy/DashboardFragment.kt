package com.ahcjapps.battlebuddy


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.home_weapons_list.*
import net.idik.lib.slimadapter.SlimAdapter
import java.util.*

class DashboardFragment : Fragment() {

    internal var data: MutableList<Any> = ArrayList()

    internal var db = FirebaseFirestore.getInstance()

    private lateinit var mAdapter: SlimAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dashboard_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()

        data.add(StatsShortcutCard())
        mAdapter.notifyDataSetChanged()
    }

    private fun setupAdapter() {
        val linearLayoutManager = LinearLayoutManager(requireActivity())
        weapon_list_rv.layoutManager = linearLayoutManager
        mAdapter = SlimAdapter.create().attachTo(weapon_list_rv).register<StatsShortcutCard>(R.layout.dashboard_stats) { data, injector ->
            val playerData: MutableList<Player> = ArrayList()
            val rv = injector.findViewById<RecyclerView>(R.id.stats_rv)
            rv.layoutManager = LinearLayoutManager(requireActivity())
            val playerAdapter = SlimAdapter.create().attachTo(rv).register<Player>(R.layout.dashboard_player_item) { player, inj ->

            }.updateData(playerData)

            playerData.add(Player())
            playerAdapter.notifyDataSetChanged()
        }.updateData(data)
    }

    data class StatsShortcutCard (
            var id: Int = 1
    )

    data class Player (
            var id: Int = 1
    )
}