package com.team12kotlin.juggle.data.auth

import com.google.firebase.auth.FirebaseAuth

class UsserSession(private val auth: FirebaseAuth) {
    fun currentUserId(): String =
        auth.currentUser?.uid ?: error("No active session")
}