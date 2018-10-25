package com.respondingio.battlegroundsbuddy.stats

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.FirebaseFunctionsException.Code
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.snacky.Snacky
import kotlinx.android.synthetic.main.stats_addplayer_bottom.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.browse
import java.util.*

class AddPlayerBottomSheet : BottomSheetDialogFragment() {

    private var mFunctions: FirebaseFunctions? = null
    private var region = "XBOX-AS"
    var regionList = arrayOf("XBOX-AS", "XBOX-EU", "XBOX-NA", "XBOX-OC", "XBOX-SA", "PC-KRJP", "PC-JP", "PC-NA", "PC-EU", "PC-RU", "PC-OC", "PC-KAKAO", "PC-SEA", "PC-SA", "PC-AS")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.stats_addplayer_bottom, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mFunctions = FirebaseFunctions.getInstance()

        add_button?.setOnClickListener { startAdd() }

        region_spinner?.setItems("Xbox Asia", "Xbox Europe", "Xbox North America", "Xbox Oceania", "Xbox South America", "PC Korea", "PC Japan", "PC North America", "PC Europe", "PC Russia", "PC Oceania", "PC Kakao", "PC South East Asia",
                "PC South and Central America", "PC Asia")
        region_spinner?.setOnItemSelectedListener { view, position, id, item ->
            region = regionList[position]
        }

        mobile_info?.onClick {
            browse("https://twitter.com/buddy_pubg/status/1055261520429506560")
        }
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme)
    }

    private fun startAdd() {
        if (add_username?.text.toString().isEmpty()) {
            add_username?.error = "Cannot be empty."
            return
        }

        add_button?.isEnabled = false
        add_progress?.visibility = View.VISIBLE
        add_username?.isEnabled = false
        val userName = add_username?.text.toString()

        loadPlayerStats(userName, regionList[region_spinner.selectedIndex].toLowerCase()).addOnCompleteListener(
                OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        val e = task.exception
                        if (e is FirebaseFunctionsException) {
                            val ffe = e as FirebaseFunctionsException?
                            val code = ffe!!.code

                            try {
                                when (code) {
                                    Code.NOT_FOUND -> Toast.makeText(activity, "Player not found, try again.", Toast.LENGTH_LONG).show()
                                    Code.RESOURCE_EXHAUSTED -> Toast.makeText(activity, "API limit reached, try again in a minute.", Toast.LENGTH_LONG).show()
                                    else -> Toast.makeText(activity, "Unknown error.", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                            }
                        }

                        divider2?.setBackgroundColor(resources.getColor(R.color.md_red_A400))

                        add_button?.isEnabled = true
                        add_progress?.visibility = View.GONE
                        add_username?.isEnabled = true
                        return@OnCompleteListener
                    }

                    val statusCode = task.result?.get("statusCode") as Int
                    if (statusCode == 200) {
                        if (activity != null)
                            if (activity is MainStatsActivity) {
                                val activity = activity as MainStatsActivity
                                if (activity.playersMap.containsKey(userName)) {
                                    //Player is in list. Switch to them.
                                    activity.setPlayerSelected(activity.playersMap[userName]!!)
                                }
                            }
                        Snacky.builder().setActivity(activity
                                ?: return@OnCompleteListener).success().setText("Player found and added!").setDuration(
                                Snacky.LENGTH_SHORT).show()
                        dismiss()
                    }
                })
    }


    private fun loadPlayerStats(playerName: String, shardID: String): Task<Map<String, Any>> {
        val data = HashMap<String, Any>()
        data["playerName"] = playerName
        data["shardID"] = shardID

        return mFunctions!!.getHttpsCallable("addPlayerByName").call(data).continueWith { task ->
            val result = task.result
                    ?.data as Map<String, Any>
            result
        }
    }
}
