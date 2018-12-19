package com.austinh.battlebuddy.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class WeaponDetailViewModel : ViewModel() {
    val weaponData = MutableLiveData<DocumentSnapshot>()
    var listener: ListenerRegistration? = null

    fun getWeaponData(ref: String) {
        listener = FirebaseFirestore.getInstance().document(ref).addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            weaponData.value = documentSnapshot
            listener!!.remove()
        }
    }
}