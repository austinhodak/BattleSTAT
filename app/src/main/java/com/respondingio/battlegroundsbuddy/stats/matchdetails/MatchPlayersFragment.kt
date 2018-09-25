package com.respondingio.battlegroundsbuddy.stats.matchdetails

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.models.MatchParticipant
import com.respondingio.battlegroundsbuddy.viewmodels.MatchDetailViewModel
import com.respondingio.battlegroundsbuddy.viewmodels.models.MatchModel
import kotlinx.android.synthetic.main.match_players_fragment.match_players_rv
import net.idik.lib.slimadapter.SlimAdapter
import java.text.DecimalFormat

class MatchPlayersFragment : Fragment() {

    private val viewModel: MatchDetailViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(MatchDetailViewModel::class.java)
    }

    private lateinit var mAdapter: SlimAdapter
    private lateinit var mActivity: MatchDetailActivity
    var sortIndex = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.match_players_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity = requireActivity() as MatchDetailActivity

        viewModel.mMatchData.observe(this, Observer { match ->
            setupAdapter(match)
        })

    }

    private lateinit var sortedList: List<MatchParticipant>

    private fun setupAdapter(matchModel: MatchModel) {
        var match = matchModel
        if (arguments!= null && arguments!!.containsKey("match")) match = arguments!!.getSerializable("match") as MatchModel

        sortedList = match.participantList.sortedWith(compareBy { it.attributes.stats.winPlace })

        match_players_rv.layoutManager = LinearLayoutManager(mActivity)
        mAdapter = SlimAdapter.create().attachTo(match_players_rv).register(R.layout.match_players_listitem) { data: MatchParticipant, injector ->
            injector.text(R.id.match_player_place, "#${data.attributes.stats.winPlace}")
            injector.text(R.id.match_player_name, data.attributes.stats.name)
            injector.text(R.id.match_player_kills, data.attributes.stats.kills.toString())
            injector.text(R.id.match_player_damage, String.format("%.0f", Math.rint(data.attributes.stats.damageDealt)))
            injector.text(R.id.match_player_distance, getTotalDistanceTravelled(data))

            injector.text(R.id.match_player_longestkill, "${String.format("%.0f", Math.rint(data.attributes.stats.longestKill))}m")

            val formatter = DecimalFormat("#,###,###")
            injector.text(R.id.match_player_rating, "Rating: ${formatter.format(data.attributes.stats.winPoints)}")

            if (mActivity.currentPlayerID == data.attributes.stats.playerId) {
                injector.background(R.id.match_player_place, R.drawable.chip_green_outline)
                injector.textColor(R.id.match_player_place, resources.getColor(R.color.md_white_1000))
            } else {
                injector.background(R.id.match_player_place, R.drawable.chip_white_outline)
                injector.textColor(R.id.match_player_place, resources.getColor(R.color.md_light_primary_text))
            }

            injector.clicked(R.id.card) {
                val intent = Intent(requireActivity(), SinglePlayerStatsActivity::class.java)
                intent.putExtra("playerID", data.attributes.stats.playerId)
                intent.putExtra("match", match)
                intent.putExtra("player", data)
                startActivity(intent)
            }

        }.updateData(sortedList)
    }

    fun getTotalDistanceTravelled(data: MatchParticipant): String {
        val distance: String
        var distanceLong: Long = 0

        distanceLong += data.attributes.stats.rideDistance.toLong()
        distanceLong += data.attributes.stats.walkDistance.toLong()
        distanceLong += data.attributes.stats.swimDistance.toLong()

        distance = String.format("%.0f", Math.rint(distanceLong.toDouble())) + "m"

        return distance
    }

    fun getTotalDistanceTravelledInM(data: MatchParticipant): Long {
        var distanceLong: Long = 0

        distanceLong += data.attributes.stats.rideDistance.toLong()
        distanceLong += data.attributes.stats.walkDistance.toLong()
        distanceLong += data.attributes.stats.swimDistance.toLong()

        return distanceLong
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.match_players, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.match_players_sort) {
            MaterialDialog(requireActivity())
                    .title(text = "Sort By")
                    .listItemsSingleChoice(R.array.players_sort, initialSelection = sortIndex) { _, position, text ->
                        sortIndex = position
                        when (position) {
                            0 -> {
                                sortedList = sortedList.sortedWith(compareBy { it.attributes.stats.winPlace })
                                mAdapter.updateData(sortedList)
                            }
                            1 -> {
                                sortedList = sortedList.sortedWith(compareBy { it.attributes.stats.name })
                                mAdapter.updateData(sortedList)
                            }
                            2 -> {
                                sortedList = sortedList.sortedWith(compareByDescending { it.attributes.stats.winPoints })
                                mAdapter.updateData(sortedList)
                            }
                            3 -> {
                                sortedList = sortedList.sortedWith(compareByDescending { it.attributes.stats.kills })
                                mAdapter.updateData(sortedList)
                            }
                            4 -> {
                                sortedList = sortedList.sortedWith(compareByDescending { it.attributes.stats.damageDealt })
                                mAdapter.updateData(sortedList)
                            }
                            5 -> {
                                sortedList = sortedList.sortedWith(compareByDescending { getTotalDistanceTravelledInM(it) })
                                mAdapter.updateData(sortedList)
                            }
                            6 -> {
                                sortedList = sortedList.sortedWith(compareByDescending { it.attributes.stats.longestKill })
                                mAdapter.updateData(sortedList)
                            }
                        }
                    }.show()
        }
        return super.onOptionsItemSelected(item)
    }
}