package com.austinh.battlebuddy.weapondetail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import com.austinh.battlebuddy.R
import com.austinh.battlebuddy.models.Weapon
import com.austinh.battlebuddy.models.WeaponStat
import com.austinh.battlebuddy.viewmodels.WeaponDetailViewModel
import kotlinx.android.synthetic.main.new_weapon_home.lottie_star
import kotlinx.android.synthetic.main.weapon_home_fragment.*
import net.idik.lib.slimadapter.SlimAdapter

class WeaponHomeFragment : Fragment() {

    private val viewModel: WeaponDetailViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(WeaponDetailViewModel::class.java)
    }

    var isStarred = false
    var weaponData: DocumentSnapshot? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.weapon_home_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lottie_star.setOnClickListener {
            if (!isStarred) {
                lottie_star.playAnimation()
                isStarred = true
            } else {
                lottie_star.cancelAnimation()
                lottie_star.progress = 0f
                isStarred = false
            }
        }

        setupStatsRV()

        viewModel.weaponData.observe(this, Observer {
            weaponData = it
            updateWeaponData(it)
        })

        var isBasicExpanded = true
        basicDropTop.setOnClickListener {
            if (isBasicExpanded) {
                weaponBasicStatsRV?.visibility = View.GONE
                isBasicExpanded = false
            } else {
                isBasicExpanded = true
                weaponBasicStatsRV?.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (weaponData != null) updateWeaponData(weaponData!!)
    }

    private var basicStatAdapter: SlimAdapter? = null
    private var basicStatList = ArrayList<WeaponStat>()

    private fun setupStatsRV() {
        weaponBasicStatsRV?.layoutManager = LinearLayoutManager(requireContext())
        basicStatAdapter = SlimAdapter.create().register<WeaponStat>(R.layout.weapon_stat_item) { stats, injector ->
            injector.text(R.id.statTitle, stats.statName)
            injector.text(R.id.statVal, stats.statValue)
        }.attachTo(weaponBasicStatsRV).updateData(basicStatList)
    }

    private fun updateWeaponData(it: DocumentSnapshot) {
        val weapon = it.toObject(Weapon::class.java)!!
        basicStatList.clear()

        Log.d("WEAPON", weapon.damageBody0)

        val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
        Glide.with(this)
                .load(FirebaseStorage.getInstance().getReferenceFromUrl(weapon.icon))
                .transition(DrawableTransitionOptions.withCrossFade(factory))
                .into(weaponIcon)
        weaponName?.text = weapon.weapon_name.toUpperCase()
        ammoChip?.text = weapon.ammo
        speedChip?.text = "${weapon.speed} m/s"

        if (weapon.range.contains("- ")) {
            rangeChip?.text = "${weapon.range.substring(weapon.range.indexOf("- ") + 2)}m"
        } else {
            rangeChip?.text = "${weapon.range.substring(weapon.range.indexOf("-") + 1)}m"
        }

        damageChip?.text = weapon.damageBody0
        magChip?.text = weapon.ammoPerMag

        weaponType?.text = arguments!!.getString("weaponClass").replace("_", " ").toUpperCase()
        if (weaponType?.text!!.endsWith("S", true)) {
            weaponType?.text = weaponType?.text!!.substring(0, weaponType?.text!!.length - 1)
        }

        //progress_stack?.models = models

        basicStatList.add(WeaponStat("Time Between Shots", weapon.TBS))
        basicStatList.add(WeaponStat("Hit Impact Power", weapon.power))
        basicStatList.add(WeaponStat("Firing Modes", weapon.firingModes))
        basicStatList.add(WeaponStat("Burst Shots", weapon.burstShots))
        basicStatList.add(WeaponStat("Burst Delay", weapon.burstDelay))
        basicStatList.add(WeaponStat("Pickup Delay", weapon.pickupDelay))
        basicStatList.add(WeaponStat("Ready Delay", weapon.readyDelay))
        basicStatList.add(WeaponStat("Reload Method", weapon.reloadMethod))
        basicStatList.add(WeaponStat("Reload Duration (Full)", weapon.reloadDurationFull))
        basicStatList.add(WeaponStat("Reload Duration (Tac)", weapon.reloadDurationTac.replace("\n", " ")))

        basicStatAdapter?.notifyDataSetChanged()

    }
}