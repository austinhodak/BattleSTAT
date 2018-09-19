package com.respondingio.battlegroundsbuddy.stats

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.R.drawable
import com.respondingio.battlegroundsbuddy.Telemetry
import com.respondingio.battlegroundsbuddy.models.LogPlayerKill
import kotlinx.android.synthetic.main.fragment_stats_kill_feed.kill_feed_rv
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.SlimInjector
import org.jetbrains.anko.support.v4.startActivity
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.TimeZone

class KillFeedFragment: Fragment() {

    private var killFeedList: List<LogPlayerKill> = ArrayList()
    private var sortIndex = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_stats_kill_feed, container, false)
    }

    private lateinit var mAdapter: SlimAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity: MatchDetailActivity = activity as MatchDetailActivity
        killFeedList = activity.killFeedList

        kill_feed_rv.layoutManager = LinearLayoutManager(activity)

        mAdapter  = SlimAdapter.create().attachTo(kill_feed_rv).register(R.layout.stats_kill_feed_item, SlimInjector<LogPlayerKill> { data, injector ->
            injector.text(R.id.kill_feed_killer, "")
            injector.text(R.id.kill_feed_victim, "")

            if (data.killer.name.isEmpty()) {
                data.killer.name = Telemetry().damageTypeCategory[data.damageTypeCategory].toString()
                injector.typeface(R.id.kill_feed_killer, injector.findViewById<TextView>(R.id.kill_feed_killer).typeface, Typeface.ITALIC)
            } else {
                injector.typeface(R.id.kill_feed_killer, injector.findViewById<TextView>(R.id.kill_feed_killer).typeface, Typeface.NORMAL)
            }

            injector.text(R.id.kill_feed_killer, data.killer.name.trim())
            injector.text(R.id.kill_feed_victim, data.victim.name.trim())

            if (Telemetry().damageCauserName[data.damageCauserName].toString() == "Player") {
                injector.text(R.id.kill_feed_cause, Telemetry().damageTypeCategory[data.damageTypeCategory].toString())
            } else {
                injector.text(R.id.kill_feed_cause, Telemetry().damageCauserName[data.damageCauserName].toString())
            }

            injector.text(R.id.textView9, (killFeedList.size - killFeedList.indexOf(data)).toString())

            Log.d("MATCH", "${activity.currentPlayerID} - ${data.killer.accountId}")

            when {
                data.killer.accountId == activity.currentPlayerID -> injector.background(R.id.textView9, drawable.chip_green_outline)
                data.victim.accountId == activity.currentPlayerID -> injector.background(R.id.textView9, drawable.chip_red_outline)
                else -> injector.background(R.id.textView9, drawable.chip_grey_outline)
            }

            //Do date.
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            sdf.timeZone = TimeZone.getTimeZone("GMT")

            val sdf2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            sdf2.timeZone = TimeZone.getTimeZone("GMT")

            val matchStartDate = sdf.parse(activity.matchData?.getJSONObject("data")?.getJSONObject("attributes")?.getString("createdAt"))
            val killTime = sdf2.parse(data._D)

            var difference = killTime.time - matchStartDate.time

            val secondsInMilli: Long = 1000
            val minutesInMilli = secondsInMilli * 60
            val hoursInMilli = minutesInMilli * 60
            val daysInMilli = hoursInMilli * 24

            difference %= daysInMilli

            difference %= hoursInMilli

            val elapsedMinutes = difference / minutesInMilli
            difference %= minutesInMilli

            val elapsedSeconds = difference / secondsInMilli

            injector.text(R.id.kill_feed_time, String.format("%02d:%02d", elapsedMinutes, elapsedSeconds))

            injector.text(R.id.kill_feed_distance, "${String.format("%.0f", Math.rint(data.distance/100))}m")

            injector.clicked(R.id.kill_feed_top) {
                startActivity<KillFeedListMap>("mapName" to activity.getMapAsset(), "killLog" to data, "killLogList" to killFeedList, "createdAt" to activity.matchData?.getJSONObject("data")?.getJSONObject("attributes")?.getString("createdAt")!!)
            }
        }).updateData(killFeedList)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.match_players, menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        kill_feed_rv?.adapter = null
        kill_feed_rv?.layoutManager = null
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.match_players_sort) {
//            MaterialDialog.Builder(requireActivity())
//                    .title("Sort Kills")
//                    .items(R.array.kill_feed_sort)
//                    .itemsCallbackSingleChoice(sortIndex) { dialog, itemView, position, text ->
//                        sortIndex = position
//                        when (position) {
//                            0 -> {
//                                killFeedList = killFeedList.sortedWith(compareByDescending { it._D })
//                                mAdapter.updateData(killFeedList)
//                            }
//                            1 -> {
//                                killFeedList = killFeedList.sortedWith(compareBy { it._D })
//                                mAdapter.updateData(killFeedList)
//                            }
//                            2 -> {
//                                killFeedList = killFeedList.sortedWith(compareBy { it.killer.name })
//                                mAdapter.updateData(killFeedList)
//                            }
//                            3 -> {
//                                killFeedList = killFeedList.sortedWith(compareByDescending { it.victim.name })
//                                mAdapter.updateData(killFeedList)
//                            }
//                            4 -> {
//                                killFeedList = killFeedList.sortedWith(compareBy { it._D })
//                                mAdapter.updateData(killFeedList)
//                            }
//                        }
//                        true
//                    }.show()
        }
        return super.onOptionsItemSelected(item)
    }
}