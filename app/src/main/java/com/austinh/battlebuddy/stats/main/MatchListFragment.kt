package com.austinh.battlebuddy.stats.main

import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinh.battlebuddy.R
import com.austinh.battlebuddy.models.*
import com.austinh.battlebuddy.snacky.Snacky
import com.austinh.battlebuddy.stats.matchdetails.MatchDetailActivity
import com.austinh.battlebuddy.utils.Auth.getUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.android.synthetic.main.fragment_matches_list.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import java.util.*

class MatchListFragment : Fragment() {

    private lateinit var mSharedPreferences: SharedPreferences
    private var mDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var mFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var mPlayer: PlayerListModel
    private lateinit var mAdapter: SlimAdapter
    private var matchList: MutableList<MatchTop> = ArrayList()
    private var sortID = 1
    private val listeners = ArrayList<ListenerRegistration>()

    private var matchRef: Query? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSharedPreferences = requireActivity().getSharedPreferences("com.austinh.battlebuddy", android.content.Context.MODE_PRIVATE)
        if (arguments != null && arguments!!.containsKey("selectedPlayer")) {
            mPlayer = arguments!!.getSerializable("selectedPlayer") as PlayerListModel
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()

        matchListModeTV?.text = resources.getStringArray(R.array.match_mode_dialog)[mPlayer.selectedMatchModes.ordinal]
        when (mPlayer.selectedMatchModes.ordinal) {
            0 -> {
                (activity!! as StatsHome).setTabVisibility(View.VISIBLE)
            }
            1, 2, 3 -> {
                (activity!! as StatsHome).setTabVisibility(View.GONE)
            }
        }

        matchListModeCard?.setOnClickListener {
            MaterialDialog(requireActivity())
                    .title(text = "Select Match Mode")
                    .listItemsSingleChoice(R.array.match_mode_dialog, initialSelection = mPlayer.selectedMatchModes.ordinal) { _, index, text ->
                        when (index) {
                            0 -> {
                                mPlayer.selectedMatchModes = MatchModes.NORMAL
                                (activity!! as StatsHome).setTabVisibility(View.VISIBLE)
                            }
                            1 -> {
                                mPlayer.selectedMatchModes = MatchModes.EVENT
                                (activity!! as StatsHome).setTabVisibility(View.GONE)
                            }
                            2 -> {
                                mPlayer.selectedMatchModes = MatchModes.CUSTOM
                                (activity!! as StatsHome).setTabVisibility(View.GONE)
                            }
                            3 -> {
                                mPlayer.selectedMatchModes = MatchModes.FAVORITES
                                (activity!! as StatsHome).setTabVisibility(View.GONE)
                            }
                        }

                        matchListModeTV?.text = text

                        getMatches()
                    }
                    .show()
        }

        matchListSort?.setOnClickListener { v ->
            MaterialDialog(requireActivity())
                    .title(text = "Sort Matches")
                    .listItemsSingleChoice(R.array.matches_sort, initialSelection = sortID) { _, index, _ ->
                        sortID = index

                        when (index) {
                            0 -> mAdapter.updateData(matchList.sortedBy { it.getPlayerWithID()?.winPlace })
                            1 -> mAdapter.updateData(matchList.sortedByDescending { it.match?.createdAt })
                            2 -> mAdapter.updateData(matchList.sortedByDescending { it.getPlayerWithID()?.kills })
                            3 -> mAdapter.updateData(matchList.sortedByDescending { it.getPlayerWithID()?.damageDealt })
                            4 -> mAdapter.updateData(matchList.sortedByDescending { it.getPlayerWithID()?.totalDistance })
                            5 -> mAdapter.updateData(matchList.sortedBy { it.match?.mapName })
                        }
                    }.show()
        }
    }

    private fun setupAdapter() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val gridLayoutManager = GridLayoutManager(requireContext(), 1)
            matches_RV?.layoutManager = gridLayoutManager
        } else {
            val gridLayoutManager = GridLayoutManager(requireContext(), 2)
            matches_RV?.layoutManager = gridLayoutManager
        }

        mAdapter = SlimAdapter.create().register(R.layout.stats_match) { match: MatchTop, injector ->
            if (match.isLoading) {
                injector.visible(R.id.match_pg)
            } else {
                injector.invisible(R.id.match_pg)
            }

            val card = injector.findViewById<View>(R.id.alert_background)

            if (match.match == null) {
                injector.text(R.id.matchKills, "0")
                injector.text(R.id.matchPlaceTV, "#--")
                injector.image(R.id.matchIcon, null)
                injector.text(R.id.matchTime, "")
                injector.text(R.id.matchDuration, "")
                injector.text(R.id.matchDamage, "0")
                injector.text(R.id.matchDistance, "0")
                return@register
            }

            fun setStats(player: ParticipantShort?) {
                if (!isAdded || player == null) return

                injector.text(R.id.matchKills, player.kills.toString())
                injector.text(R.id.matchPlaceTV, "#${player.winPlace}")
                injector.text(R.id.matchPlaceTotal, "/${match.match!!.participantCount}")
                injector.image(R.id.matchIcon, match.match!!.getMapIcon())
                injector.text(R.id.matchTime, match.match!!.getFormattedCreatedAt(true))
                injector.text(R.id.matchDuration, match.match!!.getMatchDuration())
                injector.text(R.id.matchDamage, Math.rint(player.damageDealt).toInt().toString())
                injector.text(R.id.matchDistance, "${Math.rint(player.totalDistance).toInt()}m")

                when {
                    player.winPlace == 1 -> card.setBackgroundResource(R.drawable.alert_text_box)
                    player.winPlace <= 10 -> card.setBackgroundResource(R.drawable.alert_text_box_blue)
                    else -> card.setBackgroundResource(R.drawable.alert_text_box_grey)
                }
            }

            setStats(match.getPlayerWithID())

            injector.clicked(R.id.card) {
                val todayDate = Date()
                if (match.match!!.getCreatedAtDate() != null) {
                    val diff = todayDate.time - match.match!!.getCreatedAtDate()!!.time
                    if (diff / (1000 * 60 * 60 * 24) >= 14) {
                        val bottomNav = requireActivity().findViewById<CoordinatorLayout>(R.id.stats_home_coord)
                        Snacky.builder().setView(bottomNav).info().setText("Match data not available if older than 14 days.").show()
                    } else {
                        startActivity<MatchDetailActivity>("matchID" to match.matchKey, "playerID" to match.currentPlayer, "regionID" to match.match!!.shardId, "match" to match.getSerializable())
                    }
                } else {
                    startActivity<MatchDetailActivity>("matchID" to match.matchKey, "playerID" to match.currentPlayer, "regionID" to match.match!!.shardId, "match" to match.getSerializable())
                }
            }

            if (match.isFavorite == true) {
                injector.visible(R.id.match_favorite_icon)
            } else {
                injector.gone(R.id.match_favorite_icon)
            }

        }.attachTo(matches_RV).updateData(matchList)

        getMatches()
    }

    private fun getMatches() {
        val mActivity = activity as StatsHome?
        matchList.clear()
        mAdapter.notifyDataSetChanged()

        matchRef = if (mPlayer.selectedMatchModes == MatchModes.FAVORITES) {
            mDatabase.reference.child("user_stats/${mPlayer.playerID}/allMatches/${mPlayer.platform.id}/${mPlayer.selectedSeason.codeString.toLowerCase()}/matches").orderByChild("favorites/${getUser().uid}").equalTo(true)
        } else {
            mDatabase.reference.child("user_stats/${mPlayer.playerID}/allMatches/${mPlayer.platform.id}/${mPlayer.selectedSeason.codeString.toLowerCase()}/matches").orderByChild("matchType").equalTo(mPlayer.getGamemodeSearch())
        }
        matchRef?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                toast("Error loading matches.")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (!p0.exists()) {
                    //No matches found for selected mode
                    mActivity?.setRefreshing(false)
                    matches_empty?.visibility = View.VISIBLE
                    mAdapter.updateData(matchList)
                    return
                }

                val children = p0.children.sortedWith(compareByDescending { it.child("createdAt").value.toString() })
                matchListModeTV?.text = resources.getStringArray(R.array.match_mode_dialog)[mPlayer.selectedMatchModes.ordinal].plus(" (${children.size})")
                //matchListModeTV?.text = "${resources.getStringArray(R.array.match_mode_dialog)[mPlayer.selectedMatchModes.ordinal]} (${children.size})"

                val addingAdditional = matchList.isNotEmpty()

                for (match in children) {
                    if (matchList.find { it.matchKey == match.key } != null) {
                        //Match already exists in list, continue
                        continue
                    }
                    val matchTop = MatchTop (
                            match = null,
                            isLoading = true,
                            matchKey = match.key!!,
                            currentPlayer = mPlayer.playerID,
                            createdAt = match.child("createdAt").value.toString(),
                            season = mPlayer.selectedSeason.codeString
                    )
                    if (match.hasChild("favorites/${getUser().uid}") && match.child("favorites/${getUser().uid}").value == true) {
                        matchTop.isFavorite = true
                    }

                    matchList.add(matchTop)
                    if (addingAdditional) matchList.sortByDescending { it.createdAt }
                    mAdapter.notifyItemInserted(matchList.indexOf(matchTop))
                    loadMatch(matchTop)
                }
            }
        })
    }

    private fun loadMatch(matchTop: MatchTop) {
        listeners.add(mFirestore.collection("matchData").document(matchTop.matchKey).addSnapshotListener { matchData, _ ->
            if (matchData == null || !matchData.exists()) {
                if (matchList.contains(matchTop)) {
                    matchList.remove(matchTop)
                    mAdapter.notifyDataSetChanged()
                }
                return@addSnapshotListener
            }

            if (matchList.contains(matchTop)) {
                val index = matchList.indexOf(matchTop)
                matchTop.match = matchData.toObject(Match::class.java)
                matchTop.isLoading = false
                matchList[index] = matchTop
                mAdapter.notifyItemChanged(index)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_matches_list, container, false)
    }
}
