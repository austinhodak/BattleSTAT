package com.brokenstrawapps.battlebuddy.stats.mastery

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast.LENGTH_LONG
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brokenstrawapps.battlebuddy.R
import com.brokenstrawapps.battlebuddy.models.PlayerListModel
import com.brokenstrawapps.battlebuddy.utils.Database
import com.brokenstrawapps.battlebuddy.utils.Telemetry
import com.brokenstrawapps.battlebuddy.utils.Weapons
import com.brokenstrawapps.battlebuddy.viewmodels.PlayerStatsViewModel
import com.brokenstrawapps.battlebuddy.viewmodels.models.MasteryModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.home_weapons_list.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.support.v4.longToast
import org.jetbrains.anko.support.v4.toast
import java.util.ArrayList
import kotlin.math.roundToLong

class MasteryWeaponListFragment  : Fragment() {

    private val viewModel: PlayerStatsViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(PlayerStatsViewModel::class.java)
    }

    private var mAdapter: SlimAdapter? = null
    private var category: Weapons.CATEGORY? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.home_weapons_list, container, false)
    }

    override fun onStart() {
        super.onStart()
        pg?.visibility = View.VISIBLE
        category = arguments?.getSerializable("category") as Weapons.CATEGORY?

        setupAdapter(category)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.masteryData.observe(viewLifecycleOwner, Observer { model ->
            val weapons = ArrayList<MasteryModel.MasteryModelV.Attributes.WeaponSummaries>()
            model.weaponMaster?.attributes?.weaponSummaries?.forEach {
                if (Weapons().getCategory(it.key) == category) {
                    val weapon = it.value
                    weapon.weaponId = it.key
                    weapon.weaponName = Telemetry.getItemIds(requireContext()).getString(it.key)
                    weapons.add(weapon)
                }
            }
            weapons.sortBy { it.weaponName }
            mAdapter?.updateData(weapons)
            pg?.visibility = View.GONE
        })
    }

    private fun setupAdapter(category: Weapons.CATEGORY?) {
        weapon_list_rv.layoutManager = LinearLayoutManager(activity ?: return)
        mAdapter = SlimAdapter.create().attachTo(weapon_list_rv).register(R.layout.mastery_weapon_item) { weapon: MasteryModel.MasteryModelV.Attributes.WeaponSummaries, injector ->
            injector.text(R.id.weapon_title, weapon.weaponName)

            val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://battlegrounds-buddy-2fe99.appspot.com/itemIdIcons/${weapon.weaponId}.png")

            val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()

            Glide.with(this)
                    .asDrawable()
                    .load(gsReference)
                    .apply(RequestOptions().placeholder(R.drawable.icons8_rifle).override(150,150))
                    .transition(DrawableTransitionOptions.withCrossFade(factory))
                    .into(injector.findViewById(R.id.weapon_icon))


            injector.image(R.id.mastery_icon, weapon.getEmblem())

            val bar = injector.findViewById<View>(R.id.divider_color)
            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT)
            lp.weight = (weapon.LevelCurrent ?: 0).toFloat()
            bar.layoutParams = lp

            injector.text(R.id.weapon_subtitle, "${weapon.getLevelName()} Level ${weapon.LevelCurrent}")
            injector.text(R.id.killsTV, weapon.StatsTotal?.Kills.toString())
            injector.text(R.id.knocksTV, weapon.StatsTotal?.Groggies.toString())
            injector.text(R.id.damageTV, weapon.StatsTotal?.DamagePlayer?.roundToLong().toString())
            injector.text(R.id.headshotsTV, weapon.StatsTotal?.HeadShots.toString())
            injector.text(R.id.defeatsTV, weapon.StatsTotal?.Defeats.toString())
            injector.text(R.id.longestDefeatTV, "${weapon.StatsTotal?.LongestDefeat?.roundToLong().toString()}m")

            val medalRV = injector.findViewById<RecyclerView>(R.id.medalRV)
            medalRV.layoutManager = GridLayoutManager(requireContext(), 2)
            val medalAdapter = SlimAdapter.create().attachTo(medalRV).register(R.layout.mastery_medal_layout) { medal: MasteryModel.MasteryModelV.Attributes.WeaponSummaries.MedalsD, medalInjector ->
                //Log.d("MEDAL", medal.getMedalIcon(requireContext()).toString())
                medalInjector.image(R.id.medalImage, medal.getMedalIcon(requireContext()))
                medalInjector.text(R.id.medalName, Telemetry.getMedals(requireContext()).getString(medal.MedalId))
                medalInjector.text(R.id.medalQty, "x${medal.Count}")

                medalInjector.clicked(R.id.top) {
                    longToast(medal.getMedalExplaination())
                }
            }.updateData(weapon.Medals?.toMutableList())

        }.register(R.layout.list_adview) { adUnitId: String, injector ->
            val adView = AdView(requireActivity())
            if (adView.adUnitId == null && adView.adSize == null) {
                adView.adSize = AdSize.BANNER
                adView.adUnitId = adUnitId
            }

            val linearLayout = injector.findViewById(R.id.list_adview_layout) as LinearLayout
            if (linearLayout.childCount == 0) {
                linearLayout.addView(adView)
            } else {
                return@register
            }

            if (!adView.isLoading) {}
            //adView.loadAd(Ads.getAdBuilder())
        }

        //loadWeapons(int)
    }

    fun getDp(dp: Float): Int {
        val r = requireContext().resources
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.displayMetrics
        ).toInt()
    }
}