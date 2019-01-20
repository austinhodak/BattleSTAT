package com.austinh.battlebuddy


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory.Builder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import com.austinh.battlebuddy.R.layout
import kotlinx.android.synthetic.main.home_weapons_list.pg
import kotlinx.android.synthetic.main.home_weapons_list.weapon_list_rv
import net.idik.lib.slimadapter.SlimAdapter
import java.util.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class HomeEquipmentList : Fragment() {

    internal var data: MutableList<Any> = ArrayList()

    internal var db = FirebaseFirestore.getInstance()

    private var slimAdapter: SlimAdapter? = null

    private val storage = FirebaseStorage.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(layout.home_weapons_list, container, false)
    }

    override fun onStart() {
        super.onStart()
        pg?.visibility = View.VISIBLE
        setupAdapter()
    }

    override fun onStop() {
        super.onStop()
        equipmentListener?.remove()
    }

    private var equipmentListener: ListenerRegistration? = null

    private fun loadWeapons() {
        equipmentListener = db.collection("equipment").orderBy("type")
                .addSnapshotListener { documentSnapshots, e ->
                    if (documentSnapshots == null) return@addSnapshotListener
                    data.clear()
                    try {
                        for (document in documentSnapshots) {
                            data.add(document)
                            pg?.visibility = View.GONE
                        }
                    } catch (e1: Exception) {
                        e1.printStackTrace()
                    }

                    slimAdapter?.updateData(data)

                }

    }

    private fun setupAdapter() {
        weapon_list_rv.layoutManager = LinearLayoutManager(activity ?: return)
        slimAdapter = SlimAdapter.create()
                .register(R.layout.weapon_list_item_card) { data: QueryDocumentSnapshot, injector ->

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

                    if (data.getString("type") == "backpack") {
                        injector.text(R.id.weaponItemSubtitle, "Capacity: " + data.getString("capacity")!!)
                    }

                    if (data.getString("type") == "helmet") {
                        injector.text(R.id.weaponItemSubtitle,
                                "Armor: " + data.getString("armor") + " • Damage Reduction: " + data
                                        .getString("damageReduction"))
                    }

                    if (data.getString("type") == "vest") {
                        injector.text(R.id.weaponItemSubtitle, "Capacity: " + data.getString("capacity") +
                                " • Armor: " + data.getString("armor") + " • Damage Reduction: " + data
                                .getString("damageReduction"))
                    }
                }.updateData(data).attachTo(weapon_list_rv)

        loadWeapons()
    }

}// Required empty public constructor
