package com.brokenstrawapps.battlebuddy.info


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
import com.afollestad.materialdialogs.MaterialDialog
import com.brokenstrawapps.battlebuddy.R
import com.brokenstrawapps.battlebuddy.utils.Database
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory.Builder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_attachments_list.weapon_list_rv
import kotlinx.android.synthetic.main.home_weapons_list.pg
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.SlimInjector
import java.util.*

class HomeAttachmentsList : Fragment() {

    //TODO Add attachment effects.

    internal var data: MutableList<DataSnapshot> = ArrayList()

    private var slimAdapter: SlimAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_attachments_list, container, false)
    }

    override fun onStart() {
        super.onStart()

        if (arguments != null) {
            pg?.visibility = View.VISIBLE
            val position = arguments!!.getInt("pos")
            setupAdapter(position)
        }
    }
    private fun loadWeapons(position: Int) {

        var doc = ""

        when (position) {
            0 -> doc = "Muzzle"
            1 -> doc = "Upper Rail"
            2 -> doc = "Lower Rail"
            3 -> doc = "Magazine"
            4 -> doc = "Stock"
        }

        Database.getNormalRef().child("info/attachments").addListenerForSingleValueEvent(object : ValueEventListener {
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

                data = data.filter { it.child("location").value.toString().equals(doc, true) }.toMutableList()
                data.sortBy { it.child("name").value.toString() }

                slimAdapter?.updateData(data)
            }
        })
    }

    private fun setupAdapter(position: Int) {
        weapon_list_rv.layoutManager = StaggeredGridLayoutManager(1, VERTICAL)
        slimAdapter = SlimAdapter.create()
                .register(R.layout.attachment_item,
                        SlimInjector<DataSnapshot> { data, injector ->
                            val subtitle = injector.findViewById(R.id.weaponItemSubtitle) as TextView

                            val icon = injector
                                    .findViewById(R.id.helmetItem64) as ImageView

                            subtitle.maxLines = 10

                            if (data.hasChild("icon")) {
                                try {
                                    val gsReference = Database.getStorage()
                                            .getReferenceFromUrl(data.child("icon").value.toString().replace("pubg-center", "battlegrounds-buddy-2fe99"))

                                    val factory = Builder().setCrossFadeEnabled(true).build()

                                    Glide.with(this)
                                            .load(gsReference)
                                            .transition(withCrossFade(factory))
                                            .into(icon)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

//                            if (data.hasChild("attributes")) {
//                                val flexBox = injector.findViewById<FlexboxLayout>(R.id.attributesList)
//                                for (attr in data.child("attributes").children) {
//                                    val attribute = attr.getValue(AttachmentAttribute::class.java)
//                                    val attributeView = layoutInflater.inflate(R.layout.attachment_attribute_item, flexBox, false)
//                                    val textView = attributeView.findViewById(R.id.attribute_text) as TextView
//                                    val linearLayout = attributeView.findViewById(R.id.attribute_top) as LinearLayout
//                                    textView.text = attribute?.desc
//                                    linearLayout.backgroundTintList = ColorStateList.valueOf(Color.parseColor(attribute?.colorHex ?: "#FFFFFF"))
//                                    if (attribute!!.textColorHex != null) textView.textColor = Color.parseColor(attribute.textColorHex)
//                                    flexBox.addView(attributeView)
//                                }
//                            }

                            injector.text(R.id.weaponItemName, data.child("name").value.toString())

                            injector.text(R.id.weaponItemSubtitle, data.child("weapons").value.toString())

                            injector.clicked(R.id.top_layout) {
                                if (data.hasChild("stats")) {
                                    var stats: String = data.child("stats").value.toString().replace("<br>".toRegex(), "")
                                    stats = stats.replace(" +", "\n+")
                                    stats = stats.replace(" -", "\n-")
                                    val materialDialog = MaterialDialog(activity ?: return@clicked)
                                            .title(text = data.child("name").value.toString())
                                            .message(text = stats)
                                            .positiveButton(text = "OK")
                                            .show()
                                }
                            }
                        }).updateData(data).attachTo(weapon_list_rv)

        loadWeapons(position)
    }
}

data class AttachmentAttribute (
        var desc: String? = null,
        var colorHex: String? = null,
        var color: String? = null,
        var textColorHex: String? = null
)
