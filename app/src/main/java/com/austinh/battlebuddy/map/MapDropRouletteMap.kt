package com.austinh.battlebuddy.map

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.input.input
import com.bumptech.glide.Glide
import com.austinh.battlebuddy.R
import com.austinh.battlebuddy.utils.Ads
import kotlinx.android.synthetic.main.map_drop_roulette_map.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.find
import kotlin.random.Random

class MapDropRouletteMap : Fragment() {

    lateinit var mSharedPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.map_drop_roulette_map, container, false)
        mSharedPreferences = requireContext().getSharedPreferences("com.austinh.battlebuddy", Context.MODE_PRIVATE)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapToolbar?.setNavigationIcon(R.drawable.ic_arrow_back_24dp)
        mapToolbar?.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        mapToolbar?.inflateMenu(R.menu.map_drop_roulette)
        mapToolbar?.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.roulette_settings -> {
                    MaterialDialog(requireActivity()).show {
                        title(text = "Roulette Settings")
                        customView(R.layout.roulette_settings_dialog)
                        noAutoDismiss()
                        positiveButton(text = "SAVE") { dialog ->
                            val view = dialog.getCustomView()
                            val rollEditText = view?.findViewById<EditText>(R.id.rouletteSettingsRoll)
                            val delayEditText = view?.findViewById<EditText>(R.id.rouletteSettingsDelay)

                            if (rollEditText!!.text.isEmpty() || rollEditText.text.toString().toLong() < 1) {
                                rollEditText.error = "Cannot be empty or negative."
                                return@positiveButton
                            }

                            if (delayEditText!!.text.isEmpty() || delayEditText.text.toString().toLong() < 1) {
                                delayEditText.error = "Cannot be empty or negative."
                                return@positiveButton
                            }

                            mSharedPreferences.edit().putLong("dropRoulette_rollTimes", rollEditText.text.toString().toLong()).apply()
                            mSharedPreferences.edit().putLong("dropRoulette_rollDelay", delayEditText.text.toString().toLong()).apply()

                            dialog.dismiss()
                        }
                        onShow {dialog ->
                            val view = dialog.getCustomView()
                            val rollEditText = view?.findViewById<EditText>(R.id.rouletteSettingsRoll)
                            val delayEditText = view?.findViewById<EditText>(R.id.rouletteSettingsDelay)

                            rollEditText!!.setText(mSharedPreferences.getLong("dropRoulette_rollTimes", 50).toString())
                            delayEditText!!.setText(mSharedPreferences.getLong("dropRoulette_rollDelay", 25).toString())
                        }
                    }
                }
            }
            true
        }

        when (arguments?.getString("mapName")) {
            "erangel" -> {
                mapToolbar?.title = "Erangel"

                Glide.with(this)
                        .load(R.drawable.erangel_shadow)
                        .into(dropMap)

                dropButton?.setOnClickListener {
                    GlobalScope.launch(Dispatchers.Main) {
                        dropButton?.isEnabled = false
                        dropButton?.text = "Rolling..."
                        var previousNum = -1
                        for (i in 1..mSharedPreferences.getLong("dropRoulette_rollTimes", 50)) {
                            if (!isAdded) return@launch
                            val random = java.util.Random().nextInt(MapHelper.getErangelPlaces(requireContext()).zones.size)
                            previousNum = if (random == previousNum) {
                                java.util.Random().nextInt(MapHelper.getErangelPlaces(requireContext()).zones.size)
                            } else {
                                random
                            }
                            var place = MapHelper.getErangelPlaces(requireContext()).zones[previousNum]
                            dropText?.text = place.name
                            dropMap?.updatePoint(place.x.toFloat(), place.y.toFloat())

                            delay(mSharedPreferences.getLong("dropRoulette_rollDelay", 25))
                        }
                        dropButton?.isEnabled = true
                        dropButton?.text = "ROLL AGAIN"
                    }
                }
            }
            "miramar" -> {
                mapToolbar?.title = "Miramar"
                Glide.with(this)
                        .load(R.drawable.miramar_shadow)
                        .into(dropMap)
                //dropMap?.setImageDrawable(resources.getDrawable(R.drawable.miramar_new))
                dropButton?.setOnClickListener {
                    GlobalScope.launch(Dispatchers.Main) {
                        dropButton?.isEnabled = false
                        dropButton?.text = "Rolling..."
                        var previousNum = -1
                        for (i in 1..mSharedPreferences.getLong("dropRoulette_rollTimes", 50)) {
                            if (!isAdded) return@launch
                            val random = java.util.Random().nextInt(MapHelper.getMiramarPlaces(requireContext()).zones.size)
                            previousNum = if (random == previousNum) {
                                java.util.Random().nextInt(MapHelper.getMiramarPlaces(requireContext()).zones.size)
                            } else {
                                random
                            }
                            var place = MapHelper.getMiramarPlaces(requireContext()).zones[previousNum]
                            dropText?.text = place.name
                            dropMap?.updatePoint(place.x.toFloat(), place.y.toFloat())

                            delay(mSharedPreferences.getLong("dropRoulette_rollDelay", 25))
                        }
                        dropButton?.isEnabled = true
                        dropButton?.text = "ROLL AGAIN"
                    }
                }
            }
            "sanhok" -> {
                mapToolbar?.title = "Sanhok"

                Glide.with(this)
                        .load(R.drawable.sanhok_new)
                        .into(dropMap)

                dropButton?.setOnClickListener {
                    GlobalScope.launch(Dispatchers.Main) {
                        dropButton?.isEnabled = false
                        dropButton?.text = "Rolling..."
                        var previousNum = -1
                        for (i in 1..mSharedPreferences.getLong("dropRoulette_rollTimes", 50)) {
                            if (!isAdded) return@launch
                            val random = java.util.Random().nextInt(MapHelper.getSanhokPlaces(requireContext()).zones.size)
                            previousNum = if (random == previousNum) {
                                java.util.Random().nextInt(MapHelper.getSanhokPlaces(requireContext()).zones.size)
                            } else {
                                random
                            }
                            var place = MapHelper.getSanhokPlaces(requireContext()).zones[previousNum]
                            dropText?.text = place.name
                            dropMap?.updatePoint(place.x.toFloat(), place.y.toFloat())

                            delay(mSharedPreferences.getLong("dropRoulette_rollDelay", 25))
                        }
                        dropButton?.isEnabled = true
                        dropButton?.text = "ROLL AGAIN"
                    }
                }
            }
            "vikendi" -> {
                mapToolbar?.title = "Vikendi"

                Glide.with(this)
                        .load(R.drawable.vikendi_shadow)
                        .into(dropMap)

                dropButton?.setOnClickListener {
                    GlobalScope.launch(Dispatchers.Main) {
                        dropButton?.isEnabled = false
                        dropButton?.text = "Rolling..."
                        var previousNum = -1
                        for (i in 1..mSharedPreferences.getLong("dropRoulette_rollTimes", 50)) {
                            if (!isAdded) return@launch
                            val random = java.util.Random().nextInt(MapHelper.getVikendiPlaces(requireContext()).zones.size)
                            previousNum = if (random == previousNum) {
                                java.util.Random().nextInt(MapHelper.getVikendiPlaces(requireContext()).zones.size)
                            } else {
                                random
                            }
                            var place = MapHelper.getVikendiPlaces(requireContext()).zones[previousNum]
                            dropText?.text = place.name
                            dropMap?.updatePoint(place.x.toFloat(), place.y.toFloat())

                            delay(mSharedPreferences.getLong("dropRoulette_rollDelay", 25))
                        }
                        dropButton?.isEnabled = true
                        dropButton?.text = "ROLL AGAIN"
                    }
                }
            }
        }

        dropButton?.setOnLongClickListener {
            dropMap?.updatePoint(Random.nextInt(100,900).toFloat(), Random.nextInt(100,900).toFloat())
            dropText?.text = "Random Spot"

            mSharedPreferences.edit().putBoolean("dropRoulette_hasLongPressed", true).apply()
            true
        }

        if (!mSharedPreferences.getBoolean("dropRoulette_hasLongPressed", false)) {
            mapDropHint?.visibility = View.VISIBLE
        }
    }
}