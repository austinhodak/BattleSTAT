package com.respondingio.battlegroundsbuddy.stats.main

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
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.functions.FirebaseFunctions
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.models.Match
import com.respondingio.battlegroundsbuddy.models.MatchTop
import com.respondingio.battlegroundsbuddy.models.ParticipantShort
import com.respondingio.battlegroundsbuddy.models.PrefPlayer
import com.respondingio.battlegroundsbuddy.snacky.Snacky
import com.respondingio.battlegroundsbuddy.stats.matchdetails.MatchDetailActivity
import kotlinx.android.synthetic.main.fragment_matches_list.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.uiThread
import java.util.*
import java.util.regex.Pattern
import kotlin.concurrent.schedule

class MatchListFragmentNew : Fragment() {

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

    private var timer: TimerTask? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        loadMatches(mPlayer.playerID, mPlayer.selectedShardID, mPlayer.selectedSeason, mPlayer.selectedGamemode)

        val mActivity: StatsHome? = activity as StatsHome
        timer = Timer("Refreshing", false).schedule(250) {
            if (isAdded)
                runOnUiThread {
                    mActivity?.setRefreshing(true)
                }
        }
    }

    private var matchListener: ValueEventListener? = null
    private var matchRef: DatabaseReference? = null

    private fun loadMatches(playerID: String, selectedShardID: String, selectedSeason: String?, selectedGamemode: String?) {
        val gameModeMatch = gamemodeMatches[modesList.indexOf(selectedGamemode)]
        val activity = activity as StatsHome?

        matchList.clear()

        matchRef = mDatabase.reference.child("user_stats/$playerID/matches/${selectedShardID.toLowerCase()}/${selectedSeason?.toLowerCase()}/$gameModeMatch")
        matchListener = mDatabase.reference.child("user_stats/$playerID/matches/${selectedShardID.toLowerCase()}/${selectedSeason?.toLowerCase()}/$gameModeMatch").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                matchList.clear()
                if (!p0.exists()) {
                    timer?.cancel()
                    activity?.setRefreshing(false)
                    matches_empty?.visibility = View.VISIBLE
                    return
                }

                timer?.cancel()
                activity?.setRefreshing(false)

                val children = p0.children.sortedWith(compareByDescending { it.child("createdAt").value.toString() })

                for (child in children) {
                    val matchTop = MatchTop(null, true, child.key!!, playerID, child.child("createdAt").value.toString())
                    matchList.add(matchTop)
                    loadMatchData(matchTop, selectedShardID)
                }

                mAdapter.updateData(matchList)
            }
        })
    }

    private val listeners = ArrayList<ListenerRegistration>()

    private fun loadMatchData(matchTop: MatchTop, selectedShardID: String) {

        listeners.add(mFirestore.collection("matchData").document(matchTop.matchKey).addSnapshotListener { matchData, e ->
            var index = -1
            if (matchData == null || !matchData.exists()) {
                val activity = activity as StatsHome?
                activity?.setRefreshing(true)
                //Match doesn't exist, trigger function to get it.
                addMatchData(matchTop.matchKey, selectedShardID).addOnSuccessListener {

                }.addOnFailureListener {
                    var intIndex = -1
                    if (matchList.contains(matchTop)) {
                        intIndex = matchList.indexOf(matchTop)
                    }

                    if (intIndex != -1) {
                        matchList.remove(matchTop)
                        mAdapter.notifyItemRemoved(intIndex)
                    }
                    activity?.setRefreshing(false)
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

            val activity = activity as StatsHome?
            activity?.setRefreshing(false)
        })
    }

    private fun setupAdapter() {

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val gridLayoutManager = GridLayoutManager(requireContext(), 1)
            //gridLayoutManager.stackFromEnd = true
            //gridLayoutManager.reverseLayout = true
            matches_RV?.layoutManager = gridLayoutManager
        } else {
            val gridLayoutManager = GridLayoutManager(requireContext(), 2)
            //gridLayoutManager.stackFromEnd = true
            //gridLayoutManager.reverseLayout = true
            matches_RV?.layoutManager = gridLayoutManager
        }

        //matches_RV?.itemAnimator = LandingAnimator()
        mAdapter = SlimAdapter.create().register(R.layout.stats_match) { data: MatchTop, injector ->
            if (data.isLoading) {
                injector.visible(R.id.match_pg)
            } else {
                injector.invisible(R.id.match_pg)
            }

            var card = injector.findViewById<View>(R.id.alert_background)

            if (data.match == null) {
                injector.text(R.id.matchKills, "0")
                injector.text(R.id.matchPlaceTV, "#--")
                injector.image(R.id.matchIcon, null)
                injector.text(R.id.matchTime, "")
                injector.text(R.id.matchDuration, "")
                injector.text(R.id.matchDamage, "0")
                injector.text(R.id.matchDistance, "0")
                return@register
            }

            fun setStats(player: ParticipantShort) {
                if (!isAdded) return

                injector.text(R.id.matchKills, player.kills.toString())
                injector.text(R.id.matchPlaceTV, "#${player.winPlace}")
                injector.text(R.id.matchPlaceTotal, "/${data.match!!.participantCount}")
                injector.image(R.id.matchIcon, data.match!!.getMapIcon())
                injector.text(R.id.matchTime, "${capitalize(data.match!!.getFormattedCreatedAt())}")
                injector.text(R.id.matchDuration, "${data.match!!.getMatchDuration()}")
                injector.text(R.id.matchDamage, Math.rint(player.damageDealt).toInt().toString())
                injector.text(R.id.matchDistance, "${Math.rint(player.totalDistance).toInt()}m")

                when {
                    player.winPlace == 1 -> card.setBackgroundResource(R.drawable.alert_text_box)
                    player.winPlace <= 10 -> card.setBackgroundResource(R.drawable.alert_text_box_blue)
                    else -> card.setBackgroundResource(R.drawable.alert_text_box_grey)
                }
            }

            doAsync {
                data.match!!.participants.forEach { (key, participant) ->
                    if (participant.playerId == "account.${data.currentPlayer}") {
                        uiThread {
                            if (!isAdded || activity == null) return@uiThread
                            setStats(participant)
                        }
                        return@forEach
                    }
                }
            }

            injector.clicked(R.id.card) {
                val todayDate = Date()
                if (data.match!!.getCreatedAtDate() != null) {
                    val diff = todayDate.time - data.match!!.getCreatedAtDate()!!.time
                    if (diff / (1000 * 60 * 60 * 24) >= 14) {
                        val bottomNav = requireActivity().findViewById<CoordinatorLayout>(R.id.stats_home_coord)
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
        mSharedPreferences = requireActivity().getSharedPreferences("com.respondingio.battlegroundsbuddy", android.content.Context.MODE_PRIVATE)
        mDatabase = FirebaseDatabase.getInstance()
        mFunctions = FirebaseFunctions.getInstance()
        mFirestore = FirebaseFirestore.getInstance()
        if (arguments != null && arguments!!.containsKey("selectedPlayer")) {
            mPlayer = arguments!!.getSerializable("selectedPlayer") as PrefPlayer
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

    private fun capitalize(capString: String): String {
        val capBuffer = StringBuffer()
        val capMatcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString)
        while (capMatcher.find()) {
            capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase())
        }

        return capMatcher.appendTail(capBuffer).toString()
    }
}