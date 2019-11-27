package com.brokenstrawapps.battlebuddy.stats.matchdetails

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.brokenstrawapps.battlebuddy.R
import com.brokenstrawapps.battlebuddy.map.MapDownloadActivity
import com.brokenstrawapps.battlebuddy.models.LogPlayerKill
import com.brokenstrawapps.battlebuddy.snacky.Snacky
import com.brokenstrawapps.battlebuddy.utils.Telemetry
import com.brokenstrawapps.battlebuddy.viewmodels.MatchDetailViewModel
import com.brokenstrawapps.battlebuddy.viewmodels.models.MatchModel
import kotlinx.android.synthetic.main.fragment_stats_kill_feed.*
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.SlimInjector
import org.jetbrains.anko.support.v4.startActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class KillFeedFragment: Fragment() {

    private val viewModel: MatchDetailViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(MatchDetailViewModel::class.java)
    }

    private var killFeedList: List<LogPlayerKill> = ArrayList()
    private var sortIndex = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_stats_kill_feed, container, false)
    }

    private lateinit var mAdapter: SlimAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.mMatchData.observe(this, Observer { match ->
            setupAdapter(match)
        })
    }

    private fun setupAdapter(matchModel: MatchModel?) {
        var match = matchModel
        if (arguments!= null &&  arguments!!.containsKey("match")) match = arguments!!.getSerializable("match") as MatchModel

        killFeedList = match!!.eventList.getKills().sortedBy { it._D }

        kill_feed_rv.layoutManager = LinearLayoutManager(activity)

        mAdapter  = SlimAdapter.create().attachTo(kill_feed_rv).register(R.layout.stats_kill_feed_item_card, SlimInjector<LogPlayerKill> { data, injector ->
            injector.text(R.id.kill_feed_killer, "")
            //injector.text(R.id.kill_feed_victim, "")

            val killerTV = injector.findViewById<TextView>(R.id.kill_feed_killer)
            if (data.killer?.name?.isEmpty() == true) {
                data.killer.name = Telemetry.getDamageTypes(requireContext())[data.damageTypeCategory].toString()

                //killerTV.setTypeface(null, Typeface.BOLD_ITALIC)
                //injector.typeface(R.id.kill_feed_killer, injector.findViewById<TextView>(R.id.kill_feed_killer).typeface, Typeface.ITALIC)
            } else {
                //killerTV.setTypeface(null, Typeface.BOLD)
                //injector.typeface(R.id.kill_feed_killer, injector.findViewById<TextView>(R.id.kill_feed_killer).typeface, Typeface.NORMAL)
            }

            injector.text(R.id.kill_feed_killer, "${data.killer?.name?.trim()}  \uf714  ${data.victim.name.trim()}")

            if (Telemetry.getDamageCausers(requireContext())[data.damageCauserName].toString() == "Player") {
                injector.text(R.id.kill_feed_cause, Telemetry.getDamageTypes(requireContext())[data.damageTypeCategory].toString().toUpperCase())
            } else {
                injector.text(R.id.kill_feed_cause, Telemetry.getDamageCausers(requireContext())[data.damageCauserName].toString().toUpperCase())
            }

            injector.text(R.id.textView9, (killFeedList.size - killFeedList.indexOf(data)).toString())

            if (match.currentPlayerID.isNotEmpty() && matchModel?.currentPlayerRoster != null) {
                when {
                    data.killer?.accountId == match.currentPlayerID -> injector.background(R.id.constraintLayout4, R.drawable.alert_text_box)
                    data.victim.accountId == match.currentPlayerID -> injector.background(R.id.constraintLayout4, R.drawable.alert_text_box_red)
                    else -> {
                        injector.background(R.id.constraintLayout4, R.drawable.alert_text_box_grey)
                        /*for (teammate in matchModel?.currentPlayerRoster?.relationships?.participants?.data!!) {
                            if (data.killer.accountId == matchModel.participantHash[teammate.id]!!.attributes.stats.playerId) {
                                injector.background(R.id.textView9, drawable.chip_green_outline_teammate)
                                break
                            } else if (data.victim.accountId == matchModel.participantHash[teammate.id]!!.attributes.stats.playerId) {
                                injector.background(R.id.textView9, drawable.chip_red_outline_teammate)
                                break
                            } else {
                                injector.background(R.id.textView9, drawable.chip_grey_outline)
                            }
                        }*/
                    }
                }
            }

            //Do date.
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            sdf.timeZone = TimeZone.getTimeZone("GMT")

            val sdf2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            sdf2.timeZone = TimeZone.getTimeZone("GMT")

            val matchStartDate = sdf.parse(match.attributes?.createdAt)
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
            injector.text(R.id.kill_feed_hp, "${data.killer?.health?.roundToInt()}HP")

            injector.clicked(R.id.kill_feed_top) {
                if (File(requireContext().filesDir, match.getMapAsset(requireContext())).exists()) {
                    startActivity<KillFeedListMap>("mapName" to match.getMapAsset(requireContext()), "killLog" to data, "killLogList" to killFeedList, "createdAt" to match.attributes?.createdAt, "safeZoneCircleList" to match.safeZoneList)
                } else {
                    Snacky.builder().setActivity(requireActivity()).warning().setDuration(Snacky.LENGTH_LONG).setText("Map not downloaded.").setAction("DOWNLOAD") {
                        startActivity<MapDownloadActivity>()
                    }.show()
                }
            }

            if (data.assistant?.name != data.killer?.name && data.assistant?.name?.isEmpty() == false && data.assistant.name != data.victim.name) {
                injector.text(R.id.kill_feed_assist_text, data.assistant.name)
                injector.visible(R.id.kill_feed_assist_layout)
            } else {
                injector.gone(R.id.kill_feed_assist_layout)
            }
        }).updateData(killFeedList)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.match_players, menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        kill_feed_rv?.adapter = null
        kill_feed_rv?.layoutManager = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.match_players_sort) {
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