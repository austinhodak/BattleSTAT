package com.respondingio.battlegroundsbuddy.weapons

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.WeaponDetailActivity
import com.respondingio.battlegroundsbuddy.models.Weapon
import kotlinx.android.synthetic.main.home_weapons_list.pg
import kotlinx.android.synthetic.main.home_weapons_list.weapon_list_rv
import net.idik.lib.slimadapter.SlimAdapter
import java.util.ArrayList

class MainWeaponsList : Fragment() {

    var mSharedPreferences: SharedPreferences? = null
    private var mAdapter: SlimAdapter? = null
    private var weaponClass: String = ""
    private var data: MutableList<Any> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.home_weapons_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mSharedPreferences = requireActivity().getSharedPreferences("com.austinhodak.pubgcenter", MODE_PRIVATE)
        setupAdapter(arguments?.getInt("pos")!!)
    }

    private fun setupAdapter(int: Int) {
        val favoriteWeapons = mSharedPreferences?.getStringSet("favoriteWeapons", null)

        weapon_list_rv.layoutManager = LinearLayoutManager(requireActivity())
        mAdapter = SlimAdapter.create().attachTo(weapon_list_rv).register(R.layout.testing) { doc: DocumentSnapshot, injector ->
            val data = doc.toObject(Weapon::class.java)!!
            val subtitle = injector.findViewById<TextView>(R.id.weapon_subtitle)
            subtitle.text = ""
            injector.text(R.id.weapon_title, data.weapon_name)

            if (data.icon.isNotEmpty()) {
                val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(data.icon)

                val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()

                Glide.with(requireActivity())
                        .asDrawable()
                        .load(gsReference)
                        .apply(RequestOptions().placeholder(R.drawable.icons8_rifle).override(100,100))
                        .transition(DrawableTransitionOptions.withCrossFade(factory))
                        .into(injector.findViewById(R.id.weapon_icon))
            }

            injector.text(R.id.weapon_subtitle, data.ammo)
            if (data.ammo.isEmpty()) injector.gone(R.id.weapon_subtitle)

            if (data.damageBody0.isNotEmpty()) {
                injector.text(R.id.weapon_body_dmg, data.damageBody0)
                injector.visible(R.id.divider)
            } else {
                injector.text(R.id.weapon_body_dmg, "N/A")
                injector.gone(R.id.body_parent)
            }

            if (data.damageHead0.isNotEmpty()) {
                injector.text(R.id.weapon_head_dmg, data.damageHead0)
                injector.visible(R.id.divider)
            } else {
                injector.text(R.id.weapon_head_dmg, "N/A")
                injector.gone(R.id.head_parent)
            }

            if (data.range.isNotEmpty()) {
                val range = data.range
                val split = range.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                injector.text(R.id.weapon_range, split[1] + "M")
                injector.visible(R.id.divider)
            } else {
                injector.text(R.id.weapon_range, "N/A")
                injector.gone(R.id.range_parent)
            }

            injector.clicked(R.id.card_top) {
                val intent = Intent(activity,
                        WeaponDetailActivity::class.java)
                intent.putExtra("weaponPath", doc.reference.path)
                intent.putExtra("weaponName",
                        data.weapon_name)
                intent.putExtra("weaponKey", doc.id)
                intent.putExtra("weaponClass", weaponClass)
                startActivity(intent)
            }

            injector.gone(R.id.weapon_fav)
            if (favoriteWeapons != null && favoriteWeapons.contains(doc.reference.path)) {
                injector.visible(R.id.weapon_fav)
            }

            injector.gone(R.id.weapon_like)
            if (mSharedPreferences?.contains("${doc.reference.path}-like")!! && mSharedPreferences!!.getBoolean("${doc.reference.path}-like", false)) {
                injector.visible(R.id.weapon_like)
            }

            injector.gone(R.id.weapon_parachute)
            injector.gone(R.id.weapon_trophy)
            injector.gone(R.id.weapon_miramar)
            injector.gone(R.id.weapon_sanhok)

            if (data.airDropOnly) {
                Glide.with(requireActivity()).load(R.drawable.ic_parachute).into(injector.findViewById(R.id.weapon_parachute))
                injector.visible(R.id.weapon_parachute)
            }

            if (data.bestInClass) {
                Glide.with(requireActivity()).load(R.drawable.icons8_trophy).into(injector.findViewById(R.id.weapon_trophy))
                injector.visible(R.id.weapon_trophy)
            }

            if (data.miramar_only) {
                Glide.with(requireActivity()).load(R.drawable.cactu).into(injector.findViewById(R.id.weapon_miramar))
                injector.visible(R.id.weapon_miramar)
            }

            if (data.sanhok_only) {
               injector.visible(R.id.weapon_sanhok)
            }
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

            if (!adView.isLoading)
                adView.loadAd(AdRequest.Builder().build())
        }
        
        loadWeapons(int)
    }

    private fun loadWeapons(int: Int) {
        when (int) {
            0 -> weaponClass = "assault_rifles"
            1 -> weaponClass = "sniper_rifles"
            2 -> weaponClass = "smgs"
            3 -> weaponClass = "shotguns"
            4 -> weaponClass = "pistols"
            5 -> weaponClass = "lmgs"
            6 -> weaponClass = "throwables"
            7 -> weaponClass = "melee"
            8 -> weaponClass = "misc"
        }

        if (arguments?.containsKey("isFavoriteTab")!! && arguments?.getBoolean("isFavoriteTab")!!) {
            val classes = arrayOf("assault_rifles", "sniper_rifles", "smgs", "shotguns", "pistols", "lmgs", "throwables", "melee", "misc")
            data.clear()

            for (i in 0 until classes.count()) {
                FirebaseFirestore.getInstance().collection("weapons").document(classes[i]).collection("weapons").orderBy("weapon_name")
                        .addSnapshotListener(requireActivity()) { it, _ ->
                            data.clear()

                            val favs = mSharedPreferences?.getStringSet("favoriteWeapons", null)

                            if (it != null) {
                                for (document in it) {

                                    if (favs != null && favs.contains(document.reference.path)) {
                                        data.add(document)
                                    }

                                    pg?.visibility = View.GONE
                                }
                            }
                            mAdapter?.updateData(data)
                        }
            }
        } else {
            FirebaseFirestore.getInstance().collection("weapons").document(weaponClass).collection("weapons").orderBy("weapon_name")
                    .addSnapshotListener(requireActivity()) { it, _ ->
                        data.clear()

                        if (it != null) {
                            for (document in it) {
                                if (document.contains("live")) {
                                    if (document.getBoolean("live")!!)
                                    // Weapon is not ready to be live yet.
                                        continue
                                }
                                data.add(document)
                                pg?.visibility = View.GONE
                            }
                        }

                        if (mSharedPreferences != null && !mSharedPreferences!!.getBoolean("removeAds", false)) {
                            data.add("ca-app-pub-1946691221734928/6103601821")
                        }

                        mAdapter?.updateData(data)
                    }
        }

        pg.visibility = View.VISIBLE
    }
}