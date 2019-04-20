package com.ahcjapps.battlebuddy.weapons


import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.input
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ahcjapps.battlebuddy.R
import com.ahcjapps.battlebuddy.snacky.Snacky
import com.ahcjapps.battlebuddy.viewmodels.WeaponDetailViewModel
import com.ahcjapps.battlebuddy.weapondetail.WeaponDetailTimelineStats
import kotlinx.android.synthetic.main.fragment_weapon_comments.*
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.SlimInjector
import net.idik.lib.slimadapter.animators.FadeInAnimator
import org.jetbrains.anko.support.v4.longToast
import pl.hypeapp.materialtimelineview.MaterialTimelineView
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class WeaponComments : Fragment() {

    private val viewModel: WeaponDetailViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(WeaponDetailViewModel::class.java)
    }

    private var commentRef: DatabaseReference? = null
    private var commentListener: ChildEventListener? = null

    internal lateinit var mSlimAdapter: SlimAdapter

    internal lateinit var weaponPath: String
    internal lateinit var weaponKey: String

    internal var listData: MutableList<Any> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_weapon_comments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //activity?.findViewById<WaterfallToolbar>(R.id.weaponTimelineToolbarWaterfall)?.recyclerView = comment_rv
    }

    override fun onStart() {
        super.onStart()

        viewModel.weaponData.observe(requireActivity(), Observer {
            weaponPath = it.ref.toString()
            weaponKey = it.key.toString()
            setupAdapter()
        })

        Log.d("WEAPON", "$weaponPath - $weaponKey")

        if (FirebaseAuth.getInstance().currentUser == null) {
            if (FirebaseAuth.getInstance().currentUser?.isAnonymous == false) {
                comment_send?.isEnabled = false
                comment_edittext?.setText("You must be signed in.")
                comment_edittext?.isEnabled = false
            }
        }

        comment_rv?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 15) {
                    commentAddFAB?.hide()
                }
                else if (dy < -15)
                    commentAddFAB?.show()
            }
        })


        commentAddFAB?.setOnClickListener {
            if (FirebaseAuth.getInstance().currentUser == null) {
                longToast("You must be logged in to comment.")
                return@setOnClickListener
            }

            MaterialDialog(requireActivity())
                    .title(text = "Add a Comment")
                    .input(hint = "Your Comment") { dialog, text ->
                        dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
                        val UID = FirebaseAuth.getInstance().currentUser?.uid ?: "Unknown User"
                        val key = FirebaseDatabase.getInstance().getReference("/comments_weapons/$weaponKey").push().key
                        val path = "/comments_weapons/$weaponKey/$key"

                        Log.d("COMMENT", "$path -- $key -- $UID")

                        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                        val date = Date()

                        val childUpdates = HashMap<String, Any>()
                        childUpdates["$path/comment"] = text.toString()
                        childUpdates["$path/user"] = UID
                        if (FirebaseAuth.getInstance().currentUser!!.displayName!!.isEmpty()) {
                            childUpdates["$path/user_name"] = "PUBG Player"
                        } else {
                            childUpdates["$path/user_name"] = FirebaseAuth.getInstance().currentUser?.displayName.toString()
                        }
                        childUpdates["$path/timestamp"] = dateFormat.format(date)
                        childUpdates["$path/weapon_path"] = weaponPath

                        childUpdates["/users/$UID/weapon_comments/$weaponKey/$key"] = true

                        FirebaseDatabase.getInstance().reference.updateChildren(childUpdates).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Snacky.builder().setActivity(requireActivity()).success().setText("Comment Posted.").show()
                                dialog.dismiss()
                            } else {
                                Snacky.builder().setActivity(requireActivity()).error().setText("Error posting.").show()
                                dialog.setActionButtonEnabled(WhichButton.POSITIVE, true)
                            }
                        }
                    }
                    .positiveButton(text = "Post")
                    .negativeButton(text = "Cancel")
                    .show()
        }
    }

    override fun onStop() {
        super.onStop()
        commentRef?.removeEventListener(commentListener!!)
    }

    private fun setupAdapter() {
        if (comment_rv == null) return

        val linearLayoutManager = LinearLayoutManager(activity)
        comment_rv?.layoutManager = linearLayoutManager
        comment_rv?.itemAnimator = FadeInAnimator()
        mSlimAdapter = SlimAdapter.create().register(R.layout.comments_weapons_item, SlimInjector<DataSnapshot> { data, injector ->
            val timeline = injector.findViewById<MaterialTimelineView>(R.id.commentTimeline)

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

                        if (dataSnapshot.hasChild("isPaid/adFree")) {
                            timeline.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.timelineGreen))
                        } else if (dataSnapshot.hasChild("isPaid/premiumLevel3")) {
                            timeline.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.timelineBlue))
                        } else if (dataSnapshot.hasChild("dev")) {
                            timeline.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.timelineRed))
                            injector.text(R.id.comment_user, dataSnapshot.child("display_name").value!!.toString() + " (DEV)")
                        } else {
                            timeline.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.timelineGrey))
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
                                        MaterialDialog(activity!!)
                                                .title(text = "Delete Post?")
                                                .message( text = "This cannot be undone.")
                                                .positiveButton(text = "DELETE") { dialog ->
                                                    FirebaseDatabase.getInstance().getReference("/users/" + FirebaseAuth.getInstance().currentUser!!.uid
                                                            + "/weapon_comments/" + weaponKey + "/" + data.key).removeValue()

                                                    FirebaseDatabase.getInstance().getReference("/comments_weapons/" + weaponKey + "/" + data.key).removeValue().addOnSuccessListener {
                                                        loadData()
                                                    }
                                                }
                                                .negativeButton( text ="CANCEL")
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
        }).register<String>(R.layout.weapon_timeline_line_only) { data, injector ->

        }.register<WeaponDetailTimelineStats.LineSection>(R.layout.weapon_timeline_line) { data, injector ->
            injector.text(R.id.line_title, data.title)
            injector.text(R.id.line_subtitle, data.subTitle)
        }.attachTo(comment_rv).updateData(listData)

        loadData()
    }

    private fun loadData() {
        listData.clear()
        listData.add("")
        comments_pg?.visibility = View.VISIBLE

        commentRef = FirebaseDatabase.getInstance().getReference("/comments_weapons/$weaponKey")
        commentListener = FirebaseDatabase.getInstance().getReference("/comments_weapons/$weaponKey").orderByChild("timestamp").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                try {
                    listData.add(1, dataSnapshot)
                } catch (e: Exception) {
                    listData.add(dataSnapshot)
                }
                mSlimAdapter.notifyItemInserted(listData.indexOf(dataSnapshot))
                empty_tv?.visibility = View.GONE
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                listData.remove(dataSnapshot)
                mSlimAdapter.updateData(listData)
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
                    listData.clear()

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
