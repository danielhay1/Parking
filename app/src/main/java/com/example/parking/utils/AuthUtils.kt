package com.example.parking.utils

import android.app.Activity
import android.content.Intent
import com.example.parking.activities.MainActivity
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class AuthUtils {


    companion object {
        fun  getCurrentUser(): String? {
            return FirebaseAuth.getInstance().currentUser?.uid

        }

        fun logout(activity: Activity) {
            FirebaseAuth.getInstance().signOut()
            activity.startActivity(Intent(activity, MainActivity::class.java))
            activity.finish()
        }

    }
}