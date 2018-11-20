package com.respondingio.battlegroundsbuddy.weapons

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.models.Weapon
import com.respondingio.battlegroundsbuddy.utils.Ads
import com.respondingio.battlegroundsbuddy.weapondetail.WeaponDetailTimeline
import kotlinx.android.synthetic.main.home_weapons_list.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.support.v4.startActivity
import java.util.*

class MainWeaponsList : Fragment() {

    var mSharedPreferences: SharedPreferences? = null
    private var mAdapter: SlimAdapter? = null
    private var weaponClass: String = "assault_rifles"
    private var data: MutableList<Any> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.home_weapons_list, container, false)
    }

    override fun onStart() {
        super.onStart()
        pg?.visibility = View.VISIBLE
        mSharedPreferences = requireActivity().getSharedPreferences("com.austinhodak.pubgcenter", MODE_PRIVATE)
        setupAdapter(arguments?.getInt("pos")!!)
    }

    override fun onStop() {
        super.onStop()
        weaponListener?.remove()

    }

    private fun setupAdapter(int: Int) {
        val favoriteWeapons = mSharedPreferences?.getStringSet("favoriteWeapons", null)

        weapon_list_rv.layoutManager = LinearLayoutManager(activity ?: return)
        mAdapter = SlimAdapter.create().attachTo(weapon_list_rv).register(R.layout.testing) { doc: DocumentSnapshot, injector ->
            val data = doc.toObject(Weapon::class.java)!!
            val subtitle = injector.findViewById<TextView>(R.id.weapon_subtitle)
            subtitle.text = ""
            injector.text(R.id.weapon_title, data.weapon_name)

            if (data.icon.isNotEmpty()) {
                val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(data.icon)

                val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()

                Glide.with(this)
                        .asDrawable()
                        .load(gsReference)
                        .apply(RequestOptions().placeholder(R.drawable.icons8_rifle).override(150,150))
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

            if (data.range.isNotEmpty() && data.range != "--") {
                val range = data.range
                val split = range.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                injector.text(R.id.weapon_range, split[1] + "M")
                injector.visible(R.id.divider)
            } else {
                injector.text(R.id.weapon_range, "N/A")
                injector.gone(R.id.range_parent)
            }

            injector.clicked(R.id.card_top) {
                //startActivity<WeaponDetailsActivity>("weaponPath" to doc.reference.path, "weaponName" to data.weapon_name, "weaponKey" to doc.id, "weaponClass" to weaponClass)
                //startActivity<WeaponHome>("weaponPath" to doc.reference.path, "weaponClass" to weaponClass, "weaponName" to data.weapon_name, "weaponKey" to doc.id)
                startActivity<WeaponDetailTimeline>("weaponPath" to doc.reference.path, "weaponName" to data.weapon_name, "weaponKey" to doc.id, "weaponClass" to weaponClass)
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
                Glide.with(this).load(R.drawable.ic_parachute).into(injector.findViewById(R.id.weapon_parachute))
                injector.visible(R.id.weapon_parachute)
            }

            if (data.bestInClass) {
                Glide.with(this).load(R.drawable.icons8_trophy).into(injector.findViewById(R.id.weapon_trophy))
                injector.visible(R.id.weapon_trophy)
            }

            if (data.miramar_only) {
                Glide.with(this).load(R.drawable.cactu).into(injector.findViewById(R.id.weapon_miramar))
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
                adView.loadAd(Ads.getAdBuilder())
        }
        
        loadWeapons(int)
    }

    private var weaponListener: ListenerRegistration? = null

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
            else -> weaponClass = "assault_rifles"
        }

        if (arguments?.containsKey("isFavoriteTab")!! && arguments?.getBoolean("isFavoriteTab")!!) {
            val classes = arrayOf("assault_rifles", "sniper_rifles", "smgs", "shotguns", "pistols", "lmgs", "throwables", "melee", "misc")
            data.clear()

            for (i in 0 until classes.count()) {
                weaponListener = FirebaseFirestore.getInstance().collection("weapons").document(classes[i]).collection("weapons").orderBy("weapon_name")
                        .addSnapshotListener { it, _ ->
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
            weaponListener = FirebaseFirestore.getInstance().collection("weapons").document(weaponClass).collection("weapons").orderBy("weapon_name")
                    .addSnapshotListener { it, _ ->
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
                            //data.add("ca-app-pub-1946691221734928/6103601821")
                        }

                        mAdapter?.updateData(data)
                    }
        }
    }
}