package com.example.parking.objects

import android.util.Log
import com.example.parking.utils.AuthUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DBManager {
    private val db = Firebase.firestore


    companion object {
        private const val USERS = "users"
    }


    fun saveNewUserToFireStore(user: User) {

        val newUser = AuthUtils.getCurrentUser()?.let { db.collection(USERS).document(it) }

        newUser?.set(user)?.addOnCompleteListener {
            if (it.isSuccessful) {

            }
        }


    }

}