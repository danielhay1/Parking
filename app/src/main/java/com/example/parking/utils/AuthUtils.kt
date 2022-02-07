package com.example.parking.utils

import com.google.firebase.auth.FirebaseAuth
import java.util.*

class AuthUtils {


    companion object {
        fun  getCurrentUser(): String? {
            return FirebaseAuth.getInstance().currentUser?.uid

        }
    }
}