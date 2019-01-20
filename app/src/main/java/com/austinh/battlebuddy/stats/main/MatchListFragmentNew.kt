package com.austinh.battlebuddy.stats.main

import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinh.battlebuddy.R
import com.austinh.battlebuddy.models.*
import com.austinh.battlebuddy.snacky.Snacky
import com.austinh.battlebuddy.stats.matchdetails.MatchDetailActivity
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.android.synthetic.main.fragment_matches_list.*
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.animators.FadeInAnimator
import net.idik.lib.slimadapter.viewinjector.IViewInjector
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.startActivity
import java.util.*
import kotlin.concurrent.schedule

class MatchListFragmentNew : Fragment() {

    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mFunctions: FirebaseFunctions
    private lateinit var mFirestore: FirebaseFirestore
    private lateinit var mPlayer: PlayerListModel
    private lateinit var mAdapter: SlimAdapter
    private var matchList: MutableList<MatchTop> = ArrayList()

    private var sortID = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (arguments == null) return null
        return inflater.inflate(R.layout.fragment_matches_list, container, false)
    }

    private var timer: TimerTask? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        getMatches(mPlayer)

        val mActivity: StatsHome? = activity as StatsHome
        timer = Timer("Refreshing", false).schedule(250) {
            if (isAdded)
                runOnUiThread {
                    mActivity?.setRefreshing(true)
                }
        }

        matchListModeTV?.text = resources.getStringArray(R.array.match_mode_dialog)[mPlayer.selectedMatchModes.ordinal]
        when (mPlayer.selectedMatchModes.ordinal) {
            0 -> {
                (activity!! as StatsHome).setTabVisibility(View.VISIBLE)
            }
            1,
            2,
            3 -> {
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

                        getMatches(mPlayer)
                    }
                    .show()
        }

        matchListSort?.setOnClickListener {
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

    private var matchListener: ValueEventListener? = null
    private var matchRef: Query? = null

    private fun getMatches(player: PlayerListModel) {
        val activity = activity as StatsHome?

        matchList.clear()

        matchRef = if (player.selectedMatchModes == MatchModes.FAVORITES) {
            mDatabase.reference.child("user_stats/${player.playerID}/allMatches/${player.platform.id}/${player.selectedSeason.codeString.toLowerCase()}/matches").orderByChild("favorite").equalTo(true)
        } else
            mDatabase.reference.child("user_stats/${player.playerID}/allMatches/${player.platform.id}/${player.selectedSeason.codeString.toLowerCase()}/matches").orderByChild("matchType").equalTo(player.getGamemodeSearch())
        matchListener = matchRef!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                matchList.clear()
                if (!p0.exists()) {
                    timer?.cancel()
                    activity?.setRefreshing(false)
                    matches_empty?.visibility = View.VISIBLE
                    mAdapter.updateData(matchList)
                    return
                }

                matches_empty?.visibility = View.GONE

                timer?.cancel()
                activity?.setRefreshing(false)

                val children = p0.children.sortedWith(compareByDescending { it.child("createdAt").value.toString() })

                matchListModeTV?.text = "${resources.getStringArray(R.array.match_mode_dialog)[mPlayer.selectedMatchModes.ordinal]} (${children.size})"

                for (child in children) {
                    val matchTop = MatchTop(null, true, child.key!!, player.playerID, child.child("createdAt").value.toString(), season = player.selectedSeason.codeString)
                    if (child.hasChild("favorite") && child.child("favorite").value == true) {
                        matchTop.isFavorite = true
                    }
                    matchList.add(matchTop)
                }

                mAdapter.updateData(matchList)
                matches_RV?.scheduleLayoutAnimation()
            }
        })
    }

    private val listeners = ArrayList<ListenerRegistration>()

    private fun setupAdapter() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val gridLayoutManager = GridLayoutManager(requireContext(), 1)
            matches_RV?.layoutManager = gridLayoutManager
        } else {
            val gridLayoutManager = GridLayoutManager(requireContext(), 2)
            matches_RV?.layoutManager = gridLayoutManager
        }

        matches_RV?.itemAnimator = FadeInAnimator()
        matches_RV?.layoutAnimation = android.view.animation.AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_fall_down)
        mAdapter = SlimAdapter.create().register(R.layout.stats_match) { data: MatchTop, injector ->
            if (data.isLoading) {
                injector.visible(R.id.match_pg)
            } else {
                injector.invisible(R.id.match_pg)
            }

            if (data.match == null) {
                listeners.add(mFirestore.collection("matchData").document(data.matchKey).addSnapshotListener { matchData, _ ->
                    if (matchData == null || !matchData.exists()) {
                        matchList.remove(data)
                        mAdapter.notifyDataSetChanged()
                        return@addSnapshotListener
                    }

                    data.match = matchData.toObject(Match::class.java)
                    data.isLoading = false

                    injector.invisible(R.id.match_pg)

                    updateMatchCard(data, injector)
                })
            } else {
                updateMatchCard(data, injector)
            }
        }.attachTo(matches_RV).updateData(matchList)
    }

    private fun updateMatchCard(data: MatchTop, injector: IViewInjector<IViewInjector<*>>) {
        val activity = activity as StatsHome?
        activity?.setRefreshing(false)

        val card = injector.findViewById<View>(R.id.alert_background)

        if (data.match == null) {
            injector.text(R.id.matchKills, "0")
            injector.text(R.id.matchPlaceTV, "#--")
            injector.image(R.id.matchIcon, null)
            injector.text(R.id.matchTime, "")
            injector.text(R.id.matchDuration, "")
            injector.text(R.id.matchDamage, "0")
            injector.text(R.id.matchDistance, "0")
            return
        }

        fun setStats(player: ParticipantShort?) {
            if (!isAdded || player == null) return

            injector.text(R.id.matchKills, player.kills.toString())
            injector.text(R.id.matchPlaceTV, "#${player.winPlace}")
            injector.text(R.id.matchPlaceTotal, "/${data.match!!.participantCount}")
            injector.image(R.id.matchIcon, data.match!!.getMapIcon())
            injector.text(R.id.matchTime, data.match!!.getFormattedCreatedAt(true))
            injector.text(R.id.matchDuration, data.match!!.getMatchDuration())
            injector.text(R.id.matchDamage, Math.rint(player.damageDealt).toInt().toString())
            injector.text(R.id.matchDistance, "${Math.rint(player.totalDistance).toInt()}m")

            when {
                player.winPlace == 1 -> card.setBackgroundResource(R.drawable.alert_text_box)
                player.winPlace <= 10 -> card.setBackgroundResource(R.drawable.alert_text_box_blue)
                else -> card.setBackgroundResource(R.drawable.alert_text_box_grey)
            }
        }

        setStats(data.getPlayerWithID())

        injector.clicked(R.id.card) {
            val todayDate = Date()
            if (data.match!!.getCreatedAtDate() != null) {
                val diff = todayDate.time - data.match!!.getCreatedAtDate()!!.time
                if (diff / (1000 * 60 * 60 * 24) >= 14) {
                    val bottomNav = requireActivity().findViewById<CoordinatorLayout>(R.id.stats_home_coord)
                    Snacky.builder().setView(bottomNav).info().setText("Match data not available if older than 14 days.").show()
                } else {
                    startActivity<MatchDetailActivity>("matchID" to data.matchKey, "playerID" to data.currentPlayer, "regionID" to data.match!!.shardId, "match" to data.getSerializable())
                }
            } else {
                startActivity<MatchDetailActivity>("matchID" to data.matchKey, "playerID" to data.currentPlayer, "regionID" to data.match!!.shardId, "match" to data.getSerializable())
            }
        }

        if (data.isFavorite == true) {
            injector.visible(R.id.match_favorite_icon)
        } else {
            injector.gone(R.id.match_favorite_icon)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSharedPreferences = requireActivity().getSharedPreferences("com.austinh.battlebuddy", android.content.Context.MODE_PRIVATE)
        mDatabase = FirebaseDatabase.getInstance()
        mFunctions = FirebaseFunctions.getInstance()
        mFirestore = FirebaseFirestore.getInstance()
        if (arguments != null && arguments!!.containsKey("selectedPlayer")) {
            mPlayer = arguments!!.getSerializable("selectedPlayer") as PlayerListModel
        } else {
            return
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MATCHLIST", "onStop")
        matches_RV?.adapter = null
        if (matchListener != null && matchRef != null) {
            matchRef!!.removeEventListener(matchListener!!)
        }
        for (listener in listeners) {
            listener.remove()
        }
    }
}