package com.respondingio.battlegroundsbuddy.stats

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.Telemetry
import kotlinx.android.synthetic.main.match_weaponstats_fragment.weapon_mostKills
import kotlinx.android.synthetic.main.match_weaponstats_fragment.weapon_mostKillsTitle
import kotlinx.android.synthetic.main.match_weaponstats_fragment.weapon_mostPickup
import kotlinx.android.synthetic.main.match_weaponstats_fragment.weapon_mostPickupTitle

class MatchWeaponStatsFragment : Fragment() {

    var mActivity: MatchDetailActivity? = null
    var mWeaponKills = LinkedHashMap<String, Int>()
    var mWeaponPickups = LinkedHashMap<String, Int>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.match_weaponstats_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (activity != null && activity is MatchDetailActivity) {
            mActivity = activity as MatchDetailActivity
        }
    }

    override fun onStart() {
        super.onStart()
        doMostKills()
        doMostPickedUp()
    }

    private fun doMostPickedUp() {
        for (i in 0 until mActivity?.telemetryJson!!.length()) {
            var item = mActivity?.telemetryJson!!.getJSONObject(i)

            when (item["_T"]) {
                "LogItemPickup" -> {
                    if (item.getJSONObject("item").getString("category") == "Weapon") {
                        val itemName = item.getJSONObject("item").getString("itemId")
                        if (mWeaponPickups.containsKey(itemName)) {
                            val count = mWeaponPickups[itemName]
                            mWeaponPickups[itemName] = (count!! + 1)
                        } else {
                            mWeaponPickups[itemName] = 1
                        }
                    }
                }
            }
        }

        val result = mWeaponPickups.toList().sortedByDescending { (_, value) -> value }.toMap()
        val topResult = result.entries.iterator().next()

        Log.d("TOPWEAPON", topResult.toString())

        weapon_mostPickup.text = Telemetry().itemId.getString(topResult.key)
        weapon_mostPickupTitle.text = "MOST PICKED UP (${topResult.value})"

//        Glide.with(this)
//                .load(FirebaseStorage.getInstance().getReferenceFromUrl("gs://pubg-center.appspot.com/itemIdIcons/${topResult.key}.png"))
//                .into(weapon_mostPickupIcon)
    }



    private fun doMostKills() {
        val killFeedList = mActivity?.killFeedList
        for (kill in killFeedList!!) {
            if (kill.damageTypeCategory != "Damage_Gun") continue
            val damageCauser = kill.damageCauserName
            if (mWeaponKills.containsKey(damageCauser)) {
                val weaponCount = mWeaponKills[damageCauser]
                mWeaponKills[damageCauser] = (weaponCount!! + 1)
            } else {
                mWeaponKills[damageCauser] = 1
            }
        }

        val result = mWeaponKills.toList().sortedByDescending { (_, value) -> value }.toMap()
        val topResult = result.entries.iterator().next()

        weapon_mostKills.text = Telemetry().damageCauserName.getString(topResult.key)
        weapon_mostKillsTitle.text = "MOST KILLS (${topResult.value})"

//        for (i in Telemetry().itemId.keys()) {
//            if (Telemetry().itemId.getString(i) == Telemetry().damageCauserName.getString(topResult.key)) {
//                Glide.with(this)
//                        .load(FirebaseStorage.getInstance().getReferenceFromUrl("gs://pubg-center.appspot.com/itemIdIcons/${i}.png"))
//                        .into(weapon_mostKillsIcon)
//            }
//        }
    }
}