package com.respondingio.battlegroundsbuddy.weapondetail

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
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.models.Weapon
import com.respondingio.battlegroundsbuddy.models.WeaponStat
import com.respondingio.battlegroundsbuddy.viewmodels.WeaponDetailViewModel
import devlight.io.library.ArcProgressStackView
import kotlinx.android.synthetic.main.new_weapon_home.lottie_star
import kotlinx.android.synthetic.main.new_weapon_home.progress_stack
import kotlinx.android.synthetic.main.weapon_home_fragment.ammoChip
import kotlinx.android.synthetic.main.weapon_home_fragment.basicDropTop
import kotlinx.android.synthetic.main.weapon_home_fragment.damageChip
import kotlinx.android.synthetic.main.weapon_home_fragment.magChip
import kotlinx.android.synthetic.main.weapon_home_fragment.rangeChip
import kotlinx.android.synthetic.main.weapon_home_fragment.speedChip
import kotlinx.android.synthetic.main.weapon_home_fragment.weaponBasicStatsRV
import kotlinx.android.synthetic.main.weapon_home_fragment.weaponIcon
import kotlinx.android.synthetic.main.weapon_home_fragment.weaponName
import kotlinx.android.synthetic.main.weapon_home_fragment.weaponType
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.sdk27.coroutines.onClick

class WeaponHomeFragment : Fragment() {

    private val viewModel: WeaponDetailViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(WeaponDetailViewModel::class.java)
    }

    var isStarred = false
    val models = ArrayList<ArcProgressStackView.Model>()
    var weaponData: DocumentSnapshot? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.weapon_home_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lottie_star.onClick {
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
        basicDropTop.onClick {
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
        models.clear()

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

        if (weapon.damageBody0 != "--" && weapon.damageBody0.toDouble() >= 100) {
            models.add(ArcProgressStackView.Model("BODY", ((weapon.damageBody0.toFloat() * 100f) / 225), resources.getColor(R.color.md_grey_200), resources.getColor(R.color.md_red_500), weapon.damageBody0))
        } else if (weapon.damageBody0 != "--" && weapon.damageBody0.toDouble() > 50) {
            models.add(ArcProgressStackView.Model("BODY", ((weapon.damageBody0.toFloat() * 100f) / 225), resources.getColor(R.color.md_grey_200), resources.getColor(R.color.md_orange_500), weapon.damageBody0))
        } else if (weapon.damageBody0 != "--") {
            models.add(ArcProgressStackView.Model("BODY", ((weapon.damageBody0.toFloat() * 100f) / 225), resources.getColor(R.color.md_grey_200), resources.getColor(R.color.md_green_500), weapon.damageBody0))
        }

        if (weapon.damageHead0 != "--" && weapon.damageHead0.toDouble() >= 100) {
            models.add(ArcProgressStackView.Model("HEAD", ((weapon.damageHead0.toFloat() * 100f) / 300), resources.getColor(R.color.md_grey_300), resources.getColor(R.color.md_red_700), weapon.damageHead0))
        } else if (weapon.damageHead0 != "--" && weapon.damageBody0.toDouble() > 50) {
            models.add(ArcProgressStackView.Model("HEAD", ((weapon.damageHead0.toFloat() * 100f) / 300), resources.getColor(R.color.md_grey_300), resources.getColor(R.color.md_orange_700), weapon.damageHead0))
        } else if (weapon.damageHead0 != "--") {
            models.add(ArcProgressStackView.Model("HEAD", ((weapon.damageHead0.toFloat() * 100f) / 300), resources.getColor(R.color.md_grey_300), resources.getColor(R.color.md_green_700), weapon.damageHead0))
        }

        progress_stack?.models = models

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