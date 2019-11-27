package com.brokenstrawapps.battlebuddy.stats.matchdetails

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.brokenstrawapps.battlebuddy.R
import com.brokenstrawapps.battlebuddy.viewmodels.MatchDetailViewModel
import com.brokenstrawapps.battlebuddy.viewmodels.models.MatchModel

class MatchWeaponStatsFragment : Fragment() {

    private val viewModel: MatchDetailViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(MatchDetailViewModel::class.java)
    }

    var mActivity: MatchDetailActivity? = null
    var mWeaponKills = LinkedHashMap<String, Int>()
    var mWeaponPickups = LinkedHashMap<String, Int>()
    var mAmmoPickups = LinkedHashMap<String, Int>()

    var match: MatchModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.match_weaponstats_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (activity != null && activity is MatchDetailActivity) {
            mActivity = activity as MatchDetailActivity
        }

        viewModel.mMatchData.observe(this, Observer { match ->
            this.match = match
            start()
        })
    }

    fun start() {
        doMostKills()
        doMostPickedUp()
    }

    private fun doMostPickedUp() {
        Log.d("MATCH", "${match?.logItemPickup!!.size}")
        for (i in 0 until match?.logItemPickup!!.size) {
            val logItem = match?.logItemPickup!![i]
            if (logItem.item.category == "Weapon") {
                if (mWeaponPickups.containsKey(logItem.item.itemId)) {
                    mWeaponPickups[logItem.item.itemId] = (mWeaponPickups[logItem.item.itemId]!! + 1)
                } else {
                    mWeaponPickups[logItem.item.itemId] = 1
                }
            } else if (logItem.item.category == "Ammunition") {

            }
        }

        if (mWeaponPickups.isNotEmpty()) {
            val result = mWeaponPickups.toList().sortedByDescending { (_, value) -> value }.toMap()
            val topResult = result.entries.iterator().next()

            Log.d("TOPWEAPON", topResult.toString())

            //weapon_mostPickup.text = Telemetry().itemId.getString(topResult.key)
            //weapon_mostPickupTitle.text = "MOST PICKED UP (${topResult.value})"
        }
    }


    private fun doMostKills() {
        Log.d("MATCH", "${match?.killFeedList!!.size}")

        for (i in 0 until match?.killFeedList!!.size) {
            val killItem = match?.killFeedList!![i]
            if (killItem.damageTypeCategory != "Damage_Gun") continue
            val damageCauser = killItem.damageCauserName
            if (mWeaponKills.containsKey(damageCauser)) {
                mWeaponKills[damageCauser] = (mWeaponKills[damageCauser]!! + 1)
            } else {
                mWeaponKills[damageCauser] = 1
            }
        }

        if (mWeaponKills.isNotEmpty()) {
            val result = mWeaponKills.toList().sortedByDescending { (_, value) -> value }.toMap()
            val topResult = result.entries.iterator().next()

            //weapon_mostKills.text = Telemetry().damageCauserName.getString(topResult.key)
            //weapon_mostKillsTitle.text = "MOST KILLS (${topResult.value})"
        }
    }
}