package com.respondingio.battlegroundsbuddy


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.home_weapons_list.pg
import kotlinx.android.synthetic.main.home_weapons_list.weapon_list_rv
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.SlimInjector
import java.util.ArrayList

class HomeConsumablesList : Fragment() {

    internal var data: MutableList<Any> = ArrayList()

    internal var db = FirebaseFirestore.getInstance()

    private lateinit var mAdapter: SlimAdapter

    private val storage = FirebaseStorage.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.home_weapons_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pg?.visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        setupAdapter()
    }

    override fun onStop() {
        super.onStop()
        consumableListener?.remove()
    }

    private var consumableListener: ListenerRegistration? = null

    private fun loadWeapons() {
        consumableListener = db.collection("consumables").orderBy("name")
                .addSnapshotListener { documentSnapshots, _ ->
                    data.clear()
                    if (documentSnapshots == null) return@addSnapshotListener
                    for (document in documentSnapshots) {
                        data.add(document)
                    }
                    pg?.visibility = View.GONE
                    mAdapter.updateData(data)
                }
    }

    private fun setupAdapter() {
        val linearLayoutManager = LinearLayoutManager(activity ?: return)
        weapon_list_rv.layoutManager = linearLayoutManager
        mAdapter = SlimAdapter.create()
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

                                    val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()

                                    Glide.with(this)
                                            .load(gsReference)
                                            .transition(withCrossFade(factory))
                                            .apply(RequestOptions().override(100,100))
                                            .into(icon)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                            }

                            if (data.contains("cast_time") && data.get("cast_time") != null) {
                                injector.text(R.id.weaponItemSubtitle, data.getString("cast_time")!! + " Seconds Cast Time")
                            } else {
                                injector.gone(R.id.weaponItemSubtitle)
                            }

                            injector.clicked(R.id.top_layout) {
                                if (data.contains("desc")) {
                                    var stats: String? = data.getString("desc")
                                    stats = stats!!.replace("<br>", "\n")
                                    val materialDialog = MaterialDialog.Builder(activity!!)
                                            .title(data.getString("name")!!)
                                            .content(stats)
                                            .contentColorRes(R.color.md_white_1000)
                                            .positiveText("OK")
                                            .build()

                                    materialDialog.show()
                                }
                            }
                        }).updateData(data).attachTo(weapon_list_rv)

        loadWeapons()
    }
}
