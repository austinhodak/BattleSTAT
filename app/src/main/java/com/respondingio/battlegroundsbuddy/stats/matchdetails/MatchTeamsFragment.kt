package com.respondingio.battlegroundsbuddy.stats.matchdetails

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.models.MatchParticipant
import com.respondingio.battlegroundsbuddy.models.MatchRoster
import com.respondingio.battlegroundsbuddy.viewmodels.MatchDetailViewModel
import com.respondingio.battlegroundsbuddy.viewmodels.models.MatchModel
import kotlinx.android.synthetic.main.fragment_stats_teams.stats_teams_rv
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.backgroundColor

class MatchTeamsFragment : Fragment() {

    private val viewModel: MatchDetailViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(MatchDetailViewModel::class.java)
    }

    private lateinit var mAdapter: SlimAdapter
    lateinit var mActivity: MatchDetailActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mActivity = requireActivity() as MatchDetailActivity
        return inflater.inflate(R.layout.fragment_stats_teams, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.mMatchData.observe(this, Observer { match ->
            setupAdapter(match)
        })
    }

    private lateinit var sortedList: MutableList<MatchRoster>

    private fun setupAdapter(match: MatchModel) {
        if (this::sortedList.isInitialized) sortedList.clear()
        sortedList = match.rosterList.sortedWith(compareBy { it.attributes.stats.rank }).toMutableList()

        stats_teams_rv.layoutManager = LinearLayoutManager(mActivity)
        mAdapter = SlimAdapter.create().register(R.layout.match_roster_card) { data: MatchRoster, injector ->

            injector.text(R.id.roster_card_place, "#${data.attributes.stats.rank}/${sortedList.size}")

            //TEAM PARTICIPANT LIST
            val participants = ArrayList<MatchParticipant>()
            for (i in data.relationships.participants.data) {
                participants.add(match.participantHash[i.id]!!)
            }

            val participantRV = injector.findViewById<RecyclerView>(R.id.roster_card_list)
            participantRV.layoutManager = LinearLayoutManager(mActivity)

            SlimAdapter.create().register(R.layout.match_roster_player) { participant: MatchParticipant, participantInjector ->
                participantInjector.text(R.id.roster_player_name, participant.attributes.stats.name)

                if (participants.indexOf(participant) == (participants.size - 1)) {
                    //Last item so remove div.
                    participantInjector.gone(R.id.div)
                }

                var statsString = ""

                statsString += "${participant.attributes.stats.kills} Kills •"
                statsString += " ${Math.rint(participant.attributes.stats.damageDealt).toLong()} Damage •"
                statsString += " ${participant.attributes.stats.assists} Assists •"
                statsString += " ${participant.attributes.stats.DBNOs} DBNOs"

                participantInjector.text(R.id.roster_player_stats, statsString)

                if (participant.attributes.stats.playerId == mActivity.currentPlayerID) {
                    //This participant is the current selected player.
                    participantInjector.findViewById<CoordinatorLayout>(R.id.roster_player_top).backgroundColor = resources.getColor(R.color.md_grey_850)
                    participantInjector.textColor(R.id.roster_player_stats, resources.getColor(R.color.md_dark_secondary))
                    participantInjector.textColor(R.id.roster_player_name, resources.getColor(R.color.md_white_1000))
                } else {
                    participantInjector.findViewById<CoordinatorLayout>(R.id.roster_player_top).backgroundColor = 0
                    //participantInjector.background(R.id.roster_player_top, resources.getColor(R.color.md_white_1000))
                    participantInjector.textColor(R.id.roster_player_stats, resources.getColor(R.color.md_light_secondary))
                    participantInjector.textColor(R.id.roster_player_name, resources.getColor(R.color.md_light_primary_text))
                }

                participantInjector.clicked(R.id.roster_player_top) {
                    val intent = Intent(requireActivity(), SinglePlayerStatsActivity::class.java)
                    intent.putExtra("playerID", participant.id)
                    intent.putExtra("match", match)
                    intent.putExtra("player", participant)
                    startActivity(intent)
                }

            }.attachTo(injector.findViewById(R.id.roster_card_list)).updateData(participants)

            var teamKills = 0
            var teamDamage: Long = 0
            var teamAssists = 0
            var teamDBNOS = 0
            for (participant in participants) {
                teamKills += participant.attributes.stats.kills
                teamDamage += Math.rint(participant.attributes.stats.damageDealt).toLong()
                teamAssists += participant.attributes.stats.assists
                teamDBNOS += participant.attributes.stats.DBNOs
            }

            injector.text(R.id.roster_place_kills, teamKills.toString())
            injector.text(R.id.roster_place_damage, teamDamage.toString())
            injector.text(R.id.roster_place_assists, teamAssists.toString())
            injector.text(R.id.roster_place_dbnos, teamDBNOS.toString())

            if (data.attributes.stats.rank == 1) {
                injector.background(R.id.match_div2, R.color.md_green_A400)
            } else if (data.attributes.stats.rank <= 10) {
                injector.background(R.id.match_div2, R.color.md_orange_A400)
            } else {
                injector.background(R.id.match_div2, R.color.md_light_dividers)
            }

        }.attachTo(stats_teams_rv).updateData(sortedList)
    }
}