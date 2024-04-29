package com.example.musclepump.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthUtil {
    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }

    fun getUser(): FirebaseUser? {
        return Firebase.auth.currentUser
    }
}