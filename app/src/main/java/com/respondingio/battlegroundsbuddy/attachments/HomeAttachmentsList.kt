package com.respondingio.battlegroundsbuddy.attachments


import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory.Builder
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.respondingio.battlegroundsbuddy.R
import kotlinx.android.synthetic.main.fragment_attachments_list.weapon_list_rv
import kotlinx.android.synthetic.main.home_weapons_list.pg
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.SlimInjector
import java.util.ArrayList

class HomeAttachmentsList : Fragment() {

    internal var data: MutableList<Any> = ArrayList()

    internal var db = FirebaseFirestore.getInstance()

    private var slimAdapter: SlimAdapter? = null

    private val storage = FirebaseStorage.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_attachments_list, container, false)
    }

    override fun onStart() {
        super.onStart()
        val mSharedPreferences = requireActivity().getSharedPreferences("com.austinhodak.pubgcenter", MODE_PRIVATE)

        if (arguments != null) {
            pg?.visibility = View.VISIBLE
            val position = arguments!!.getInt("pos")
            setupAdapter(position)
        }

        if (mSharedPreferences != null && !mSharedPreferences.getBoolean("removeAds", false)) {
            val adView = AdView(context?.applicationContext)
            if (adView.adUnitId == null && adView.adSize == null) {
                adView.adSize = AdSize.BANNER
                adView.adUnitId = "ca-app-pub-1946691221734928/5235698124"
            }

            val linearLayout = weapon_list_rv.parent as LinearLayout
            if (linearLayout.childCount == 1) {
                linearLayout.addView(adView)
            } else {
                return
            }

            if (!adView.isLoading)
                adView.loadAd(AdRequest.Builder().build())
        }
    }

    override fun onStop() {
        super.onStop()
        attachmentListener?.remove()
        weapon_list_rv?.adapter = null
    }

    private var attachmentListener: ListenerRegistration? = null

    private fun loadWeapons(position: Int) {

        var doc = ""

        when (position) {
            0 -> doc = "Muzzle"
            1 -> doc = "Upper Rail"
            2 -> doc = "Lower Rail"
            3 -> doc = "Magazine"
            4 -> doc = "Stock"
        }

        attachmentListener = db.collection("attachments").document("muzzle").collection("attachments")
                .whereEqualTo("location", doc)
                .addSnapshotListener { documentSnapshots, e ->

                    data.clear()
                    try {
                        for (document in documentSnapshots!!) {
                            data.add(document)

                            pg?.visibility = View.GONE
                        }
                    } catch (e1: Exception) {
                        e1.printStackTrace()
                    }

                    slimAdapter?.updateData(data)
                }
    }

    private fun setupAdapter(position: Int) {
        weapon_list_rv.layoutManager = LinearLayoutManager(activity ?: return)
        slimAdapter = SlimAdapter.create()
                .register(R.layout.weapon_list_item_card,
                        SlimInjector<DocumentSnapshot> { data, injector ->
                            val subtitle = injector
                                    .findViewById(R.id.weaponItemSubtitle) as TextView

                            val icon = injector
                                    .findViewById(R.id.helmetItem64) as ImageView

                            injector.text(R.id.weaponItemName,
                                    data.getString("name"))

                            subtitle.maxLines = 10

                            if (data.get("icon") != null) {
                                try {
                                    val gsReference = storage
                                            .getReferenceFromUrl(data.getString("icon")!!)

                                    val factory = Builder().setCrossFadeEnabled(true).build()

                                    Glide.with(this)
                                            .load(gsReference)
                                            .transition(withCrossFade(factory))
                                            .into(icon)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                            if (data.contains("PCOnly")) {
                                if (data.getBoolean("PCOnly")!!) {
                                    injector.visible(R.id.windows_icon)
                                } else {
                                    injector.gone(R.id.windows_icon)
                                }
                            } else {
                                injector.gone(R.id.windows_icon)
                            }

                            injector.text(R.id.weaponItemSubtitle, data.getString("weapons"))

                            injector.clicked(R.id.top_layout) {
                                if (data.contains("stats")) {
                                    var stats: String = data.getString("stats")!!.replace("<br>".toRegex(), "")
                                    stats = stats.replace(" +", "\n+")
                                    stats = stats.replace(" -", "\n-")
                                    val materialDialog = MaterialDialog.Builder(activity ?: return@clicked)
                                            .title(data.getString("name")!!)
                                            .content(stats)
                                            .contentColorRes(R.color.md_white_1000)
                                            .positiveText("OK")
                                            .build()

                                    materialDialog.show()
                                }
                            }
                        }).updateData(data).attachTo(weapon_list_rv)

        loadWeapons(position)
    }
}
