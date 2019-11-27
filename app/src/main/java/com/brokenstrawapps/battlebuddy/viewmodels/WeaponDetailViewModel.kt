package com.brokenstrawapps.battlebuddy.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brokenstrawapps.battlebuddy.utils.Database
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ListenerRegistration

class WeaponDetailViewModel : ViewModel() {
    val weaponData = MutableLiveData<DataSnapshot>()
    var listener: ListenerRegistration? = null

    fun getWeaponData(ref: String) {
        Database.getNormalRef(ref).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                weaponData.value = p0
            }
        })
        /*listener = FirebaseFirestore.getInstance().document(ref).addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            weaponData.value = documentSnapshot
            listener!!.remove()
        }*/
    }
}