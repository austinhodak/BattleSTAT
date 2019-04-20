package com.ahcjapps.battlebuddy.utils

import com.google.firebase.database.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object Database {

    fun getTelemetryRef(): DatabaseReference {
        return FirebaseDatabase.getInstance("https://pubg-telemetry.firebaseio.com").reference
    }

    fun getNormalRef(url: String? = null): DatabaseReference {
        if (url != null) return FirebaseDatabase.getInstance().getReferenceFromUrl(url.replace("pubg-center", "battlegrounds-battle-buddy"))
        return FirebaseDatabase.getInstance().reference
    }

    suspend fun getPUBGPlayerID(): String {
        return suspendCoroutine {
            getNormalRef().child("users").child(Auth.getUID()).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (!p0.exists()) {
                        return
                    }

                    if (p0.hasChild("pubgAccountID/accountID")) {
                        it.resume(p0.child("pubgAccountID/accountID").value.toString())
                    }
                }
            })
        }
    }
}