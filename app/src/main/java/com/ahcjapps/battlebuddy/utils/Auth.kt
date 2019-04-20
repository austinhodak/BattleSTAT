package com.ahcjapps.battlebuddy.utils

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.*

object Auth {
    var mAuth = FirebaseAuth.getInstance()
    var mDatabase = FirebaseDatabase.getInstance()


    init {
        if (mAuth.currentUser == null) {
            mAuth.signInAnonymously()
        }
    }

    fun isUserLoggedIn(): Boolean {
        return mAuth.currentUser != null
    }

    fun isUserAnon(): Boolean {
        checkLoginStatus()
        return mAuth.currentUser?.isAnonymous!!
    }

    fun getUID(): String {
        checkLoginStatus()
        return mAuth.currentUser?.uid!!
    }

    fun getUser(): FirebaseUser {
        checkLoginStatus()
        return mAuth.currentUser!!
    }

    /**
     * Checks to see if user is null, if null logs in anonymously
     */
    private fun checkLoginStatus(): Task<AuthResult>? {
        return if (mAuth.currentUser == null) {
            mAuth.signInAnonymously()
        } else {
            null
        }
    }

    fun setupAccount(currentUser: FirebaseUser?) {
        val userRef = mDatabase.getReference("users/" + currentUser?.uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(userSnapshot: DataSnapshot) {
                if (!userSnapshot.exists()) {
                    Log.d("USER", "User not created, creating...")

                    val childUpdates = HashMap<String, Any?>()
                    childUpdates["users/" + currentUser?.uid + "/last_logon"] = ServerValue.TIMESTAMP
                    childUpdates["users/" + currentUser?.uid + "/email"] = null
                    childUpdates["users/" + currentUser?.uid + "/phone"] = null
                    childUpdates["users/" + currentUser?.uid + "/display_name"] = currentUser?.displayName.toString()
                    mDatabase.reference.updateChildren(childUpdates)
                    return
                }

                Log.d("USER", "User found, updating account info." + currentUser?.email)

                val childUpdates = HashMap<String, Any?>()
                childUpdates["users/" + currentUser?.uid + "/last_logon"] = ServerValue.TIMESTAMP
                childUpdates["users/" + currentUser?.uid + "/email"] = null
                childUpdates["users/" + currentUser?.uid + "/phone"] = null
                childUpdates["users/" + currentUser?.uid + "/display_name"] = currentUser?.displayName.toString()
                mDatabase.reference.updateChildren(childUpdates)
            }

        })
    }
}