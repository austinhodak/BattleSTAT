package com.respondingio.battlegroundsbuddy.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.facebook.internal.Mutable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.respondingio.battlegroundsbuddy.R
import kotlinx.android.synthetic.main.fragment_about.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.util.*
import kotlin.collections.ArrayList

class ProfileMain : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadProfile()
    }

    private var mSelectedList: IntArray? = null

    private fun loadProfile() {
        val mUser = FirebaseAuth.getInstance().currentUser ?: return

        val displayName = mUser.displayName
        val email = mUser.email
        val phone = mUser.phoneNumber

        if (!displayName.isNullOrEmpty()) {
            activity?.title = displayName
            profile_name_tv?.text = displayName
        } else {
            activity?.title = "My Profile"
        }

        if (!email.isNullOrEmpty()) {
            profile_email_tv?.text = email
        }

        if (!phone.isNullOrEmpty()) {
            profile_phone_tv?.text = phone
        }

        setupListeners(mUser)

        FirebaseDatabase.getInstance().getReference("/users/${mUser.uid}").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(snap: DataSnapshot) {
                if (!snap.hasChild("game_versions")) {
                    profile_game_versions?.text = "None"
                    return
                }

                val intList = ArrayList<Int>()

                if (snap.hasChild("game_versions/pc")) intList.add(0)
                if (snap.hasChild("game_versions/xbox")) intList.add(1)
                if (snap.hasChild("game_versions/mobile")) intList.add(2)

                mSelectedList = IntArray(intList.size)

                var mVersionString = ""

                if (intList.contains(0)) {
                    mVersionString = "PC"

                    mSelectedList!![0] = 0
                }

                if (intList.contains(1)) {
                    if (mVersionString.isEmpty()) {
                        mVersionString = "Xbox"
                        mSelectedList!![0] = 1
                    } else {
                        mVersionString += ", Xbox"
                        mSelectedList!![1] = 1
                    }
                }

                if (intList.contains(2)) {
                    if (mVersionString.isEmpty()) {
                        mVersionString = "Mobile"
                        mSelectedList!![0] = 2
                    }else {
                        //mSelectedList!![2] = 2
                        mVersionString += ", Mobile"
                    }

                    if (intList.contains(0) && intList.contains(1)) {
                        mSelectedList!![2] = 2
                    } else if (intList.size == 2) {
                        mSelectedList!![1] = 2
                    }
                }

                profile_game_versions?.text = mVersionString
            }

        })
    }

    private fun setupListeners(mUser: FirebaseUser) {
        (profile_game_versions?.parent?.parent as LinearLayout).onClick { it ->
            MaterialDialog(requireActivity())
                    .title(text = "Which version(s) do you own?")
                    .positiveButton(text = "Done")
                    .listItemsMultiChoice(R.array.profile_game_versions, initialSelection = mSelectedList!!) { _, which, items ->
                        val childUpdates = HashMap<String, Any?>()

                        if (items.contains("PC")) {
                            childUpdates["/users/${mUser.uid}/game_versions/pc"] = true
                        } else {
                            childUpdates["/users/${mUser.uid}/game_versions/pc"] = null
                        }
                        if (items.contains("Xbox")) {
                            childUpdates["/users/${mUser.uid}/game_versions/xbox"] = true
                        } else {
                            childUpdates["/users/${mUser.uid}/game_versions/xbox"] = null
                        }
                        if (items.contains("Mobile")) {
                            childUpdates["/users/${mUser.uid}/game_versions/mobile"] = true
                        } else {
                            childUpdates["/users/${mUser.uid}/game_versions/mobile"] = null
                        }

                        FirebaseDatabase.getInstance().reference.updateChildren(childUpdates)
                    }
                    .show()
        }
    }
}