package com.ahcjapps.battlebuddy.ammo


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
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.ahcjapps.battlebuddy.R
import com.ahcjapps.battlebuddy.R.layout
import com.ahcjapps.battlebuddy.utils.Database
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.home_weapons_list.*
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.SlimInjector
import java.util.*

class HomeAmmoList : Fragment() {

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
        ammoListener?.remove()
    }

    private var ammoListener: ListenerRegistration? = null

    private fun loadWeapons() {
        Database.getNormalRef().child("info/ammo").orderByChild("name").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                data.clear()
                try {
                    for (document in p0.children) {
                        data.add(document)

                        pg?.visibility = View.GONE
                    }
                } catch (e1: Exception) {
                    e1.printStackTrace()
                }

                slimAdapter?.updateData(data)
            }

        })

        /*ammoListener = db.collection("ammo").orderBy("name")
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
                }*/
    }

    private fun setupAdapter() {
        weapon_list_rv.layoutManager = LinearLayoutManager(activity ?: return)
        slimAdapter = SlimAdapter.create()
                .register(R.layout.attachment_item,
                        SlimInjector<DataSnapshot> { data, injector ->
                            val subtitle = injector
                                    .findViewById(R.id.weaponItemSubtitle) as TextView

                            val icon = injector
                                    .findViewById(R.id.helmetItem64) as ImageView

                            injector.text(R.id.weaponItemName,
                                    data.child("name").value.toString())

                            subtitle.maxLines = 10

                            if (data.hasChild("icon")) {
                                try {
                                    val gsReference = storage
                                            .getReferenceFromUrl(data.child("icon").value.toString().replace("pubg-center", "battlegrounds-battle-buddy"))

                                    val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()

                                    Glide.with(this)
                                            .load(gsReference)
                                            .transition(withCrossFade(factory))
                                            .into(icon)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                            }

                            injector.text(R.id.weaponItemSubtitle, data.child("weapons").value.toString())
                        }).updateData(data).attachTo(weapon_list_rv)

        loadWeapons()
    }


}
