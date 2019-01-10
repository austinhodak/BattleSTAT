package com.austinh.battlebuddy.stats.matchdetails

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinh.battlebuddy.R
import com.austinh.battlebuddy.models.MatchParticipant
import com.austinh.battlebuddy.viewmodels.MatchDetailViewModel
import com.austinh.battlebuddy.viewmodels.models.MatchModel
import kotlinx.android.synthetic.main.activity_match_detail.*
import kotlinx.android.synthetic.main.match_players_fragment.*
import net.idik.lib.slimadapter.SlimAdapter

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
            when (data.attributes.stats.deathType) {
                "alive" -> injector.text(R.id.match_player_rating, "Alive")
                "byplayer" -> injector.text(R.id.match_player_rating, "Killed By Player")
                "suicide" -> injector.text(R.id.match_player_rating, "Suicided")
                "logout" -> injector.text(R.id.match_player_rating, "Left Match")
            }
            injector.text(R.id.match_player_place, "#${data.attributes.stats.winPlace}")
            injector.text(R.id.match_player_name, data.attributes.stats.name)
            injector.text(R.id.match_player_kills, data.attributes.stats.kills.toString())
            injector.text(R.id.match_player_damage, String.format("%.0f", Math.rint(data.attributes.stats.damageDealt)))
            injector.text(R.id.match_player_distance, getTotalDistanceTravelled(data))

            injector.text(R.id.match_player_longestkill, "${String.format("%.0f", Math.rint(data.attributes.stats.longestKill))}m")

            if (mActivity.currentPlayerID == data.attributes.stats.playerId) {
                //injector.background(R.id.match_player_place, R.drawable.chip_green_outline)
                injector.textColor(R.id.match_player_place, resources.getColor(R.color.md_white_1000))
            } else {
                //injector.background(R.id.match_player_place, R.drawable.chip_grey_outline)
                injector.textColor(R.id.match_player_place, resources.getColor(R.color.md_white_1000))
            }

            injector.clicked(R.id.card) {
                val bundle = Bundle()
                bundle.putString("playerID", data.id)
                requireActivity().toolbar_title.text = data.attributes.stats.name

                val activity: MatchDetailActivity = requireActivity() as MatchDetailActivity
                activity.updateToolbarElevation(0f)
                activity.updateToolbarFlags(false)
                activity.showPlayerStatsFragment(bundle)
            }

        }.updateData(sortedList)
    }

    private fun getTotalDistanceTravelled(data: MatchParticipant): String {
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.match_players, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("OPTIONS", "SELECTED")
        if (item.itemId == R.id.match_players_sort) {
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
                                sortedList = sortedList.sortedWith(compareByDescending { it.attributes.stats.kills })
                                mAdapter.updateData(sortedList)
                            }
                            3 -> {
                                sortedList = sortedList.sortedWith(compareByDescending { it.attributes.stats.damageDealt })
                                mAdapter.updateData(sortedList)
                            }
                            4 -> {
                                sortedList = sortedList.sortedWith(compareByDescending { getTotalDistanceTravelledInM(it) })
                                mAdapter.updateData(sortedList)
                            }
                            5 -> {
                                sortedList = sortedList.sortedWith(compareByDescending { it.attributes.stats.longestKill })
                                mAdapter.updateData(sortedList)
                            }
                        }
                    }.show()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}