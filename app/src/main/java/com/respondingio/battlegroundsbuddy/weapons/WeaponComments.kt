package com.respondingio.battlegroundsbuddy.weapons


import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.R.layout
import de.mateware.snacky.Snacky
import kotlinx.android.synthetic.main.fragment_weapon_comments.comment_edittext
import kotlinx.android.synthetic.main.fragment_weapon_comments.comment_rv
import kotlinx.android.synthetic.main.fragment_weapon_comments.comment_send
import kotlinx.android.synthetic.main.fragment_weapon_comments.comments_pg
import kotlinx.android.synthetic.main.fragment_weapon_comments.empty_tv
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.SlimInjector
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.HashMap

/**
 * A simple [Fragment] subclass.
 */
class WeaponComments : Fragment() {

    private var commentRef: DatabaseReference? = null
    private var commentListener: ChildEventListener? = null

    internal lateinit var mSlimAdapter: SlimAdapter

    internal lateinit var weaponPath: String
    internal lateinit var weaponKey: String

    internal var listData: MutableList<Any> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(layout.fragment_weapon_comments, container, false)
    }

    override fun onStart() {
        super.onStart()

        if (arguments != null) {
            weaponPath = arguments!!.getString("weaponPath")!!
            weaponKey = arguments!!.getString("weaponKey")!!
        }

        if (FirebaseAuth.getInstance().currentUser == null) {
            comment_send?.isEnabled = false
            comment_edittext?.setText("You must be signed in.")
            comment_edittext?.isEnabled = false
        }

        comment_send?.setOnClickListener { saveComment() }

        setupAdapter()
    }

    override fun onStop() {
        super.onStop()
        commentRef?.removeEventListener(commentListener!!)
    }

    private fun setupAdapter() {
        val linearLayoutManager = LinearLayoutManager(activity)
        comment_rv?.layoutManager = linearLayoutManager
        mSlimAdapter = SlimAdapter.create().register(R.layout.comments_weapons_item, SlimInjector<DataSnapshot> { data, injector ->
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                var time: Long = 0
                try {
                    time = sdf.parse(data.child("timestamp").value!!.toString()).time
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val now = System.currentTimeMillis()

                val ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS)

                injector.text(R.id.comment_time, ago)

                if (data.child("user_name").value != null)
                    injector.text(R.id.comment_user, data.child("user_name").value!!.toString())

                if (data.child("comment").value != null)
                    injector.text(R.id.comment_text, data.child("comment").value!!.toString())

                FirebaseDatabase.getInstance().getReference("/users/").child(data.child("user").value!!.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            return
                        }

                        if (dataSnapshot.hasChild("display_name") && dataSnapshot.child("display_name").value!!.toString() != data.child("user_name").value!!.toString()) {
                            injector.text(R.id.comment_user, dataSnapshot.child("display_name").value!!.toString())
                        }

                        var game_versions = ""

                        if (dataSnapshot.hasChild("game_versions/pc")) {
                            game_versions = "PC"
                        }
                        if (dataSnapshot.hasChild("game_versions/xbox")) {
                            if (game_versions.isEmpty()) {
                                game_versions = "Xbox"
                            } else {
                                game_versions = "$game_versions, Xbox"
                            }
                        }
                        if (dataSnapshot.hasChild("game_versions/mobile")) {
                            if (game_versions.isEmpty()) {
                                game_versions = "Mobile"
                            } else {
                                game_versions = "$game_versions, Mobile"
                            }
                        }

                        Log.d("VERSIONS", game_versions)

                        injector.text(R.id.comment_gameV, game_versions)

                        if (game_versions.isEmpty()) {
                            injector.gone(R.id.comment_gameV)
                        }

                        if (dataSnapshot.hasChild("dev")) {
                            injector.textColor(R.id.comment_user, resources.getColor(R.color.md_red_500))
                            injector.text(R.id.comment_user, dataSnapshot.child("display_name").value!!.toString() + " (DEV)")
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })

                injector.longClicked(R.id.top_layout, OnLongClickListener {
                    if (FirebaseAuth.getInstance().currentUser == null) return@OnLongClickListener false
                    FirebaseDatabase.getInstance().getReference("/users/" + FirebaseAuth.getInstance().currentUser!!.uid + "/weapon_comments/" + weaponKey + "/" + data.key).addListenerForSingleValueEvent(
                            object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    if (true && dataSnapshot.exists() && dataSnapshot.value as Boolean) {
                                        MaterialDialog.Builder(activity!!)
                                                .title("Delete Post?")
                                                .content("This cannot be undone.")
                                                .positiveText("DELETE")
                                                .negativeText("CANCEL")
                                                .onPositive { dialog, which ->
                                                    FirebaseDatabase.getInstance().getReference("/users/" + FirebaseAuth.getInstance().currentUser!!.uid
                                                            + "/weapon_comments/" + weaponKey + "/" + data.key).removeValue()

                                                    FirebaseDatabase.getInstance().getReference("/comments_weapons/" + weaponKey + "/" + data.key).removeValue()

                                                    listData.remove(data)
                                                    mSlimAdapter.updateData(listData)

                                                    loadData()
                                                }
                                                .show()
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {

                                }
                            })
                    false
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).attachTo(comment_rv).updateData(listData)

        loadData()
    }

    private fun loadData() {
        listData.clear()
        comments_pg?.visibility = View.VISIBLE

        commentRef = FirebaseDatabase.getInstance().getReference("/comments_weapons/$weaponKey")
        commentListener = FirebaseDatabase.getInstance().getReference("/comments_weapons/$weaponKey").orderByChild("timestamp").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                listData.add(0, dataSnapshot)
                mSlimAdapter.updateData(listData)
                empty_tv?.visibility = View.GONE
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {

            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
        FirebaseDatabase.getInstance().getReference("/comments_weapons/$weaponKey").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    empty_tv?.visibility = View.VISIBLE
                    comments_pg?.visibility = View.GONE



                    mSlimAdapter.updateData(listData)
                } else {
                    comments_pg?.visibility = View.GONE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun saveComment() {
        comment_send?.isEnabled = false
        comment_edittext?.isEnabled = false
        val comment = comment_edittext?.text.toString()
        if (!comment.isEmpty()) {
            val UID = FirebaseAuth.getInstance().currentUser!!.uid
            val key = FirebaseDatabase.getInstance().getReference("/comments_weapons/$weaponKey").push().key
            val path = "/comments_weapons/$weaponKey/$key"

            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val date = Date()

            val childUpdates = HashMap<String, Any>()
            childUpdates["$path/comment"] = comment
            childUpdates["$path/user"] = UID
            if (FirebaseAuth.getInstance().currentUser!!.displayName!!.isEmpty()) {
                childUpdates["$path/user_name"] = UID
            } else {
                childUpdates["$path/user_name"] = FirebaseAuth.getInstance().currentUser?.displayName.toString()
            }
            childUpdates["$path/timestamp"] = dateFormat.format(date)
            childUpdates["$path/weapon_path"] = weaponPath

            childUpdates["/users/$UID/weapon_comments/$weaponKey/$key"] = true

            FirebaseDatabase.getInstance().reference.updateChildren(childUpdates).addOnCompleteListener(OnCompleteListener { task ->
                comment_send?.isEnabled = true
                comment_edittext?.isEnabled = true
                if (!task.isSuccessful) {
                    Snacky.builder().setActivity(requireActivity()).error().setText("Error posting.").show()
                    return@OnCompleteListener
                }

                comment_edittext?.text?.clear()

                if (isAdded) {
                    Snacky.builder().setActivity(requireActivity()).setActionTextColor(Color.WHITE).setActionText("UNDO")
                            .setActionClickListener {
                                FirebaseDatabase.getInstance().getReference("/users/" + FirebaseAuth.getInstance().currentUser!!.uid
                                        + "/weapon_comments/" + weaponKey + "/" + key).removeValue()

                                FirebaseDatabase.getInstance().getReference("/comments_weapons/$weaponKey/$key").removeValue()

                                commentRef?.removeEventListener(commentListener!!)

                                mSlimAdapter.updateData(listData)

                                loadData()
                            }.success().setText("Posted!").show()
                }
            })
        } else {
            comment_send?.isEnabled = true
            comment_edittext?.isEnabled = true
            comment_edittext?.error = "Cannot be empty."
        }
    }
}
