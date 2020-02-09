package com.brokenstrawapps.battlebuddy.info


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.brokenstrawapps.battlebuddy.R
import com.brokenstrawapps.battlebuddy.utils.Database
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
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

    private fun loadWeapons() {
        Database.getNormalRef().child("info/consumables").orderByChild("name").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                data.clear()
                for (document in p0.children) {
                    data.add(document)
                }
                pg?.visibility = View.GONE
                mAdapter.updateData(data)
            }
        })
    }

    private fun setupAdapter() {
        val linearLayoutManager = LinearLayoutManager(activity ?: return)
        weapon_list_rv.layoutManager = linearLayoutManager
        mAdapter = SlimAdapter.create()
                .register(R.layout.weapon_list_item_card,
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
                                            .getReferenceFromUrl(data.child("icon").value.toString().replace("pubg-center", "battlegrounds-buddy-2fe99"))

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

                            if (data.hasChild("cast_time")) {
                                injector.text(R.id.weaponItemSubtitle, data.child("cast_time").value.toString() + " Seconds Cast Time")
                            } else {
                                injector.gone(R.id.weaponItemSubtitle)
                            }

                            injector.clicked(R.id.top_layout) {
                                if (data.hasChild("desc")) {
                                    var stats: String? = data.child("desc").value.toString()
                                    stats = stats!!.replace("<br>", "\n")
                                    val materialDialog = MaterialDialog(activity!!)
                                            .title(text = data.child("name").value.toString())
                                            .message(text = stats)
                                            .positiveButton(text = "OK").show()
                                }
                            }
                        }).updateData(data).attachTo(weapon_list_rv)

        loadWeapons()
    }
}
