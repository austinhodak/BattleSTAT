package com.austinh.battlebuddy.stats

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.functions.FirebaseFunctions
import com.austinh.battlebuddy.R
import com.austinh.battlebuddy.models.Match
import com.austinh.battlebuddy.models.MatchTop
import com.austinh.battlebuddy.models.ParticipantShort
import com.austinh.battlebuddy.models.PrefPlayer
import com.austinh.battlebuddy.snacky.Snacky
import com.austinh.battlebuddy.stats.matchdetails.MatchDetailActivity
import kotlinx.android.synthetic.main.activity_stats_main_new.*
import kotlinx.android.synthetic.main.fragment_matches_list.*
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.animators.LandingAnimator
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.uiThread
import java.util.*

class MatchListFragment : Fragment() {

    private var gamemodeMatches = arrayOf("matchesSolo", "matchesSoloFPP", "matchesDuo", "matchesDuoFPP", "matchesSquad", "matchesSquadFPP")
    private var modesList = arrayOf("solo", "solo-fpp", "duo", "duo-fpp", "squad", "squad-fpp")

    private lateinit var mSharedPreferences: SharedPreferences
    lateinit var mDatabase: FirebaseDatabase
    lateinit var mFunctions: FirebaseFunctions
    lateinit var mFirestore: FirebaseFirestore
    private lateinit var mPlayer: PrefPlayer
    private lateinit var mAdapter: SlimAdapter
    private var matchList: MutableList<MatchTop> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (arguments == null) return null
        return inflater.inflate(R.layout.fragment_matches_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        loadMatches(mPlayer.playerID, mPlayer.selectedShardID, mPlayer.selectedSeason, mPlayer.selectedGamemode)
        activity?.mainStatsRefreshLayout?.isRefreshing = true
    }

    private var matchListener: ChildEventListener? = null

    private var matchRef: DatabaseReference? = null

    private fun loadMatches(playerID: String, selectedShardID: String, selectedSeason: String?, selectedGamemode: String?) {
        val gameModeMatch = gamemodeMatches[modesList.indexOf(selectedGamemode)]
        val activity = activity as MainStatsActivity?

        matchList.clear()

        matchRef = mDatabase.reference.child("user_stats/$playerID/matches/$selectedShardID/$selectedSeason/$gameModeMatch")
        matchListener = mDatabase.reference.child("user_stats/$playerID/matches/$selectedShardID/$selectedSeason/$gameModeMatch").orderByChild("createdAt").limitToLast(25).addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                loadMatchData(p0.key!!, selectedShardID, playerID)
                matches_empty?.visibility = View.GONE
            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })
        mDatabase.reference.child("user_stats/$playerID/matches/$selectedShardID/$selectedSeason/$gameModeMatch").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (!p0.exists()) {
                    activity?.mainStatsRefreshLayout?.isRefreshing = false
                    matches_empty?.visibility = View.VISIBLE
                }
            }
        })
    }

    private val listeners = ArrayList<ListenerRegistration>()

    private fun loadMatchData(matchKey: String, selectedShardID: String, playerID: String) {
        val matchTop = MatchTop(null, true, matchKey, playerID, "")
        matchList.add(matchTop)
        mAdapter.notifyItemInserted(matchList.indexOf(matchTop))
        listeners.add(mFirestore.collection("matchData").document(matchKey).addSnapshotListener { matchData, e ->
            var index = -1
            if (matchData == null || !matchData.exists()) {
                activity?.mainStatsRefreshLayout?.isRefreshing = true
                //Match doesn't exist, trigger function to get it.
                addMatchData(matchKey, selectedShardID).addOnSuccessListener {

                }.addOnFailureListener {
                    /*var intIndex = -1
                    if (matchList.contains(matchTop)) {
                        intIndex = matchList.indexOf(matchTop)
                    }

                    if (intIndex != -1) {
                        matchList.remove(matchTop)
                        mAdapter.notifyItemRemoved(intIndex)
                    }*/
                }

                return@addSnapshotListener
            }

            var intIndex = 0

            if (matchList.contains(matchTop)) {
                intIndex = matchList.indexOf(matchTop)
            }

            matchTop.match = matchData.toObject(Match::class.java)
            matchTop.isLoading = false

            matchList[intIndex] = matchTop
            mAdapter.notifyItemChanged(intIndex)

            val activity = activity as MainStatsActivity?
            activity?.mainStatsRefreshLayout?.isRefreshing = false
        })
    }

    private fun setupAdapter() {
        val layoutManager = LinearLayoutManager(activity ?: return)
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = true
        matches_RV?.layoutManager = layoutManager
        matches_RV?.itemAnimator = LandingAnimator()
        mAdapter = SlimAdapter.create().register(R.layout.stats_match_item) { data: MatchTop, injector ->
            if (data.isLoading) {
                injector.visible(R.id.match_pg)
            } else {
                injector.invisible(R.id.match_pg)
            }

            if (data.match == null) {
                injector.text(R.id.match_kills, "0")
                injector.text(R.id.match_place, "#--/--")
                injector.image(R.id.map_icon, null)
                injector.text(R.id.match_time, "--:--")
                injector.text(R.id.match_duration, "--:--")
                injector.text(R.id.match_damage, "0")
                injector.text(R.id.match_distance, "0")
                injector.background(R.id.match_div, R.color.md_light_dividers)
                return@register
            }

            fun setStats (player: ParticipantShort) {
                injector.text(R.id.match_kills, player.kills.toString())
                injector.text(R.id.match_place, "#${player.winPlace}/${data.match?.participantCount}")
                injector.image(R.id.map_icon, data.match!!.getMapIcon())
                injector.text(R.id.match_time, data.match!!.getFormattedCreatedAt())
                injector.text(R.id.match_duration, data.match!!.getMatchDuration())
                injector.text(R.id.match_damage, Math.rint(player.damageDealt).toInt().toString())
                injector.text(R.id.match_distance, "${Math.rint(player.totalDistance).toInt()}m")

                when {
                    player.winPlace == 1 -> injector.background(R.id.match_div, R.color.md_green_A400)
                    player.winPlace <= 10 -> injector.background(R.id.match_div, R.color.md_orange_A400)
                    else -> injector.background(R.id.match_div, R.color.md_light_dividers)
                }
            }

            doAsync {
                data.match!!.participants.forEach { (key, participant) ->
                    if (participant.playerId == "account.${data.currentPlayer}") {
                        uiThread {
                            setStats(participant)
                        }
                        return@forEach
                    }
                }
            }

            injector.clicked(R.id.match_card) {
                val todayDate = Date()
                if (data.match!!.getCreatedAtDate() != null) {
                    val diff = todayDate.time - data.match!!.getCreatedAtDate()!!.time
                    if (diff / (1000 * 60 * 60 * 24) >= 14) {
                        val bottomNav = requireActivity().findViewById<CoordinatorLayout>(R.id.stats_coord_layout)
                        Snacky.builder().setView(bottomNav).info().setText("Match data not available if older than 14 days.").show()
                    } else {
                        startActivity<MatchDetailActivity>("matchID" to data.matchKey, "playerID" to data.currentPlayer, "regionID" to data.match!!.shardId)
                    }
                } else {
                    startActivity<MatchDetailActivity>("matchID" to data.matchKey, "playerID" to data.currentPlayer, "regionID" to data.match!!.shardId)
                }
            }

        }.registerDefault(R.layout.match_list_empty) { data, injector ->

        }.attachTo(matches_RV).updateData(matchList)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSharedPreferences = requireActivity().getSharedPreferences("com.austinh.battlebuddy", android.content.Context.MODE_PRIVATE)
        mDatabase = FirebaseDatabase.getInstance()
        mFunctions = FirebaseFunctions.getInstance()
        mFirestore = FirebaseFirestore.getInstance()
        if (arguments != null && arguments!!.containsKey("player")) {
            mPlayer = arguments!!.getSerializable("player") as PrefPlayer
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

    private fun addMatchData(matchID: String, shardID: String): Task<Map<String, Any>> {
        val data = HashMap<String, Any>()
        data["matchID"] = matchID
        data["shardID"] = shardID

        return mFunctions.getHttpsCallable("addMatchData").call(data).continueWith { task ->
            val result = task.result?.data as Map<String, Any>
            Log.d("REQUEST", result.toString())
            result
        }
    }
}