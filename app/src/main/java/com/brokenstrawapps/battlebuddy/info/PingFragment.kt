package com.brokenstrawapps.battlebuddy.info


import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.brokenstrawapps.battlebuddy.R
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.stealthcopter.networktools.Ping
import com.stealthcopter.networktools.ping.PingResult
import kotlinx.android.synthetic.main.dashboard_fragment.*
import kotlinx.coroutines.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.support.v4.runOnUiThread
import kotlin.collections.ArrayList
import kotlin.math.roundToLong

class PingFragment : Fragment() {

    internal var data: MutableList<Server> = ArrayList()

    internal var db = FirebaseFirestore.getInstance()

    private lateinit var mAdapter: SlimAdapter

    private var doPingLoop = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dashboard_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        updateList(Platform.PCPS4)

        pingTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {

            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {

            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                if (p0?.text == "Xbox") updateList(Platform.XBOX)
                else updateList(Platform.PCPS4)
            }

        })

        pingFAB.setOnClickListener {
            if (doPingLoop) {
                doPingLoop = false
                pingFAB.setImageResource(R.drawable.ic_play_arrow_24dp)
            } else {
                doPingLoop = true
                pingFAB.setImageResource(R.drawable.ic_pause_24dp)
                doPings()
            }
        }
    }

    private fun setupAdapter() {
        val linearLayoutManager = LinearLayoutManager(requireActivity())
        pingRV.layoutManager = linearLayoutManager
        mAdapter = SlimAdapter.create().attachTo(pingRV).register<Server>(R.layout.ping_server_item) { data, injector ->
            val pingText = injector.findViewById<TextView>(R.id.serverPing)
            val pingTextAvg = injector.findViewById<TextView>(R.id.serverPingAvg)

            injector.text(R.id.serverName, data.name)

            if (data.inProgress) {
                injector.typeface(R.id.serverPing, null, Typeface.BOLD)
            } else {
                injector.typeface(R.id.serverPing, null, Typeface.NORMAL)
            }

            if (data.ping != null) {
                injector.text(R.id.serverPing, "${data.ping!!.timeTaken.roundToLong()}ms")
                when {
                    data.ping!!.timeTaken>= 500 -> pingText.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.md_red_500))
                    data.ping!!.timeTaken>= 400 -> pingText.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.md_deep_orange_500))
                    data.ping!!.timeTaken>= 300 -> pingText.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.md_orange_500))
                    data.ping!!.timeTaken>= 200 -> pingText.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.md_amber_500))
                    data.ping!!.timeTaken>= 100 -> pingText.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.md_light_green_400))
                    data.ping!!.timeTaken>= 80 -> pingText.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.md_green_400))
                    data.ping!!.timeTaken>= 0 -> pingText.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.md_green_600))
                }
            } else {
                injector.text(R.id.serverPing, "--")
                pingText.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.md_grey_800))
            }

            if (data.pings.isNotEmpty()) {
                var avg: Long = 0
                for (ping in data.pings) {
                    avg += ping
                }
                avg /= data.pings.size
                injector.text(R.id.serverPingAvg, "${avg}ms")
                when {
                    avg>= 500 -> pingTextAvg.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.md_red_500))
                    avg>= 400 -> pingTextAvg.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.md_deep_orange_500))
                    avg>= 300 -> pingTextAvg.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.md_orange_500))
                    avg>= 200 -> pingTextAvg.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.md_amber_500))
                    avg>= 100 -> pingTextAvg.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.md_light_green_400))
                    avg>= 80 -> pingTextAvg.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.md_green_400))
                    avg>= 0 -> pingTextAvg.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.md_green_600))
                }
            } else {
                injector.text(R.id.serverPingAvg, "--")
                pingTextAvg.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.md_grey_800))
            }
        }.updateData(data)
    }

    private fun updateList(platform: Platform) {
        data.clear()
        if (platform == Platform.PCPS4) {
            data.add(Server("North America", "dynamodb.us-east-2.amazonaws.com", platform))
            data.add(Server("Ireland", "dynamodb.eu-west-1.amazonaws.com", platform))
            data.add(Server("London", "dynamodb.eu-west-2.amazonaws.com", platform))
            data.add(Server("Europe", "dynamodb.eu-central-1.amazonaws.com", platform))
            data.add(Server("Korea", "dynamodb.ap-northeast-2.amazonaws.com", platform))
            data.add(Server("Japan", "dynamodb.ap-northeast-1.amazonaws.com", platform))
            data.add(Server("South-East Asia", "dynamodb.ap-southeast-1.amazonaws.com", platform))
            data.add(Server("Oceania", "dynamodb.ap-southeast-2.amazonaws.com", platform))
            data.add(Server("South America", "dynamodb.sa-east-1.amazonaws.com", platform))
            data.add(Server("China", "dynamodb.cn-north-1.amazonaws.com.cn", platform))

            mAdapter.notifyDataSetChanged()

            doPings()
        } else {
            //data.add(Server("(US) Iowa", "azspdcentralus", platform))
            //data.add(Server("(US) Wyoming", "azspdwestcentralus", platform))
            data.add(Server("(US) Texas", "speedtestscus", platform))
            data.add(Server("(US) California", "speedtestwus", platform))
            data.add(Server("(US) Illinois", "speedtestnsus", platform))
            //data.add(Server("(US) Washington", "azspdwestus2", platform))
            //data.add(Server("Brazil South", "azspdbrazilsouth", platform))
            data.add(Server("Netherlands", "speedtestwe", platform))
            data.add(Server("Ireland", "speedtestne", platform))
            //data.add(Server("England", "azspduksouth", platform))
            //data.add(Server("Korea", "azspdkoreacentral", platform))

            mAdapter.notifyDataSetChanged()

            doPings()
        }
    }

    private fun doPings() {
        if (!doPingLoop) return
        val iterator = data.iterator()
        GlobalScope.launch(Dispatchers.IO, CoroutineStart.DEFAULT) {
            while (doPingLoop && iterator.hasNext() && isAdded) {
                val server = iterator.next()
                if (!server.enabled) continue
                runBlocking {
                    runOnUiThread {
                        val m = data[data.indexOf(server)]
                        m.inProgress = true
                        data[data.indexOf(server)] = m
                        mAdapter.notifyDataSetChanged()
                    }

                    data[data.indexOf(server)] = data[data.indexOf(server)].ping()

                    if (!isAdded) return@runBlocking
                    runOnUiThread {
                        mAdapter.notifyDataSetChanged()
                    }
                }
            }

            doPings()
        }
    }

    data class Server (
            var name: String,
            var URL: String,
            var platform: Platform,
            var ping: PingResult? = null,
            var enabled: Boolean = true,
            var inProgress: Boolean = false,
            var pings: ArrayList<Long> = ArrayList()
    ) {
        fun ping(): Server {
            if (platform == Platform.XBOX) URL = "http://$URL.blob.core.windows.net/cb.json"
            ping = Ping.onAddress(URL).setTimeOutMillis(2000).doPing()
            inProgress = false
            if (ping != null) {
                if (pings.size == 10) pings.removeAt(0)
                pings.add(ping!!.timeTaken.roundToLong())
            }
            return this
        }
    }

    enum class Platform {
        PCPS4,
        XBOX
    }
}