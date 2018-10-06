package com.respondingio.battlegroundsbuddy.weapondetail

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.models.Weapon
import com.respondingio.battlegroundsbuddy.viewmodels.WeaponDetailViewModel
import kotlinx.android.synthetic.main.fragment_matches_player_stats.*
import kotlinx.android.synthetic.main.fragment_weapon_timeline_stats.*
import kotlinx.android.synthetic.main.update_rss_card.view.*
import kotlinx.android.synthetic.main.weapon_home_fragment.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.backgroundResource
import pl.hypeapp.materialtimelineview.MaterialTimelineView

class WeaponDetailTimelineStats : Fragment() {

    private val viewModel: WeaponDetailViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(WeaponDetailViewModel::class.java)
    }

    lateinit var mAdapter: SlimAdapter

    var mData = ArrayList<Any>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_weapon_timeline_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timelineStatsRV.layoutManager = LinearLayoutManager(requireContext())

        setupAdapter()
        weaponTimelineToolbarWaterfall.recyclerView = timelineStatsRV

        viewModel.weaponData.observe(this, Observer {
            weaponLoaded(it)
        })

        weaponTimelineToolbar.title = requireActivity().intent.getStringExtra("weaponName")
        weaponTimelineToolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp)
        weaponTimelineToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun weaponLoaded(it: DocumentSnapshot) {
        mData.clear()
        val weapon = it.toObject(Weapon::class.java)!!
        var weaponClass: String
        weaponClass = requireActivity().intent.getStringExtra("weaponClass")?.replace("_", " ")!!.toUpperCase()
        if (weaponClass.endsWith("S", true)) {
            weaponClass = weaponClass.substring(0, weaponClass.length - 1)
        }

        mData.add(TopSection(weapon.icon, weapon.weapon_name, weaponClass, weapon.ammo, weapon.speed, weapon.range, weapon.ammoPerMag, weapon.power))

        if (weapon.damageBody0 != "--" && weapon.damageHead0 != "--") {
            mData.add(LineSection("Damage Stats", "At 10 Meters"))

            mData.add(DamageItem(R.drawable.vest_white, "No Vest", damage = weapon.damageBody0))
            mData.add(DamageItem(R.drawable.helmet_white, "No Helmet", damage = weapon.damageHead0))
        }

        mData.add(LineSection("Attachments", "${weapon.attachments.size} Total"))

        val attachmentsList = ArrayList<Attachment>()

        for (snap in weapon.attachments) {
            snap.get().addOnSuccessListener { attachment ->
                mData.add(mData.indexOf(LineSection("Attachments", "${weapon.attachments.size} Total")) + 1, attachment?.toObject(Attachment::class.java)!!)

                try {
                    attachmentsList.sortWith(Comparator { s1, s2 -> s1.name.compareTo(s2.name, ignoreCase = true) })
                } catch (e1: Exception) {
                    e1.printStackTrace()
                }

                mAdapter.updateData(mData)
            }
        }

        mData.add(LineSection("Sounds", "Test"))

        mAdapter.updateData(mData)
    }

    fun setupAdapter() {
        mAdapter = SlimAdapter.create().attachTo(timelineStatsRV).register<TopSection>(R.layout.weapon_timeline_top) { data, injector ->
            Glide.with(this).load(FirebaseStorage.getInstance().getReferenceFromUrl(data.icon!!)).into(injector.findViewById(R.id.timelineIcon))
            injector.text(R.id.timelineTitle, data.title)
            injector.text(R.id.timelineSubtitle, data.subtitle)
            injector.text(R.id.timelineAmmo, data.ammo)
            injector.text(R.id.timelineSpeed, data.speed)
            injector.text(R.id.timelineMag, data.mag)
            injector.text(R.id.timelinePower, data.power)

            if (data.range.isNotEmpty() && data.range != "--") {
                val range = data.range
                val split = range.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                injector.text(R.id.timelineRange, split[1] + "M")
            } else {
                injector.text(R.id.timelineRange, "--")
            }

            injector.clicked(R.id.timeline_top_card) {
                var div = injector.findViewById<View>(R.id.top_div)
                if (div.visibility == View.VISIBLE) {
                    div.visibility = View.GONE
                    injector.findViewById<LinearLayout>(R.id.top_extras).visibility = View.GONE
                } else {
                    div.visibility = View.VISIBLE
                    injector.findViewById<LinearLayout>(R.id.top_extras).visibility = View.VISIBLE
                }
            }
        }.register<LineSection>(R.layout.weapon_timeline_line) { data, injector ->
            injector.text(R.id.line_title, data.title)
            injector.text(R.id.line_subtitle, data.subTitle)
        }.register<DamageItem>(R.layout.timeline_small_top) { data, injector ->
            val index = mData.indexOf(data)
            val timeline = injector.findViewById<MaterialTimelineView>(R.id.timeline_small_view)

            val damageD = Math.ceil((100 / data.damage.toDouble()))
            Log.d("TIMELINE", "$damageD")
            if (damageD >= 5) {
                data.tintColor = R.color.timelineLightBlue
            } else if (damageD >= 4) {
                data.tintColor = R.color.timelineGreen
            } else if (damageD >= 3) {
                data.tintColor = R.color.timelineYellow
            } else if (damageD >= 2) {
                data.tintColor = R.color.timelineOrange
            } else if (damageD >= 1) {
                data.tintColor = R.color.timelineRed
            } else {
                data.tintColor = R.color.timelineRed
            }

            injector.text(R.id.damageSubtitle, "${Math.ceil(damageD).toInt()} Hits to Kill")

            if (mData[index - 1] is LineSection) {
                //First one, do nothing
                injector.gone(R.id.div2)
            } else {
                //Last one
                timeline.backgroundResource = R.drawable.timeline_round_bottom_only
                timeline.position = MaterialTimelineView.POSITION_FIRST
                injector.gone(R.id.div)
                injector.image(R.id.damageIcon, R.drawable.helmet_white)
            }

            timeline.backgroundTintList = ColorStateList.valueOf(resources.getColor(data.tintColor))

            injector.text(R.id.damageTitle, data.title)
            injector.text(R.id.damageVal, data.damage)

            val icon = injector.findViewById<ImageView>(R.id.damageIcon)
        }.register<Attachment>(R.layout.timeline_small_top) { data, injector ->
            val index = mData.indexOf(data)
            val timeline = injector.findViewById<MaterialTimelineView>(R.id.timeline_small_view)

            Glide.with(this).load(FirebaseStorage.getInstance().getReferenceFromUrl(data.icon)).into(injector.findViewById(R.id.damageIcon))

            if ((index % 2) == 0) {
                timeline.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.timelineGrey))
            } else {
                timeline.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.timelineGrey))
            }

            try {
                if (mData[index - 1] is LineSection) {
                    //First one, do nothing
                    injector.gone(R.id.div2)
                } else if (mData[index + 1] is LineSection) {
                    timeline.backgroundResource = R.drawable.timeline_round_bottom_only
                    timeline.position = MaterialTimelineView.POSITION_FIRST
                } else if (mData[index - 1] is Attachment && mData[index + 1] is Attachment) {
                    //MIDDLE
                    timeline.backgroundColorResource = R.color.timelineBlue

                    timeline.position = -1
                } else {
                    //Last one
                    timeline.backgroundResource = R.drawable.timeline_round_bottom_only
                    timeline.position = MaterialTimelineView.POSITION_FIRST
                    injector.gone(R.id.div)
                    injector.image(R.id.damageIcon, R.drawable.helmet_white)
                }
            } catch (e: Exception) {
                timeline.backgroundColorResource = R.color.timelineBlue
                timeline.position = -1
            }
        }
    }

    data class LineSection(
            var title: String = "",
            var subTitle: String = ""
    )

    data class TopSection (
            var icon: String?,
            var title: String = "--",
            val subtitle: String = "--",
            val ammo: String = "--",
            val speed: String = "--",
            val range: String = "--",
            val mag: String = "--",
            val power: String = "--"
    )

    data class DamageItem(
            var icon: Int?,
            var title: String = "",
            var subTitle: String = "",
            var damage: String = "",
            var tintColor: Int = R.color.timelineGrey
    )

    data class Attachment (
            val icon: String = "",
            val location: String = "",
            val name: String = "",
            val stats: String = "",
            val weapons: String = ""
    )
}