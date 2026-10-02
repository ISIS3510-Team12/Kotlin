package com.team12kotlin.juggle.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firebaseAuth: FirebaseAuth
) {
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    suspend fun signIn(email: String, password: String): FirebaseUser {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        return result.user ?: error("Sign in succeeded but returned no user")
    }

    suspend fun signUp(email: String, password: String): FirebaseUser {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        return result.user ?: error("Sign up succeeded but returned no user")
    }

    suspend fun signInWithGoogle(idToken: String): FirebaseUser {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        return result.user ?: error("Google sign in succeeded but returned no user")
    }

    suspend fun updateDisplayName(displayName: String): FirebaseUser? {
        val user = firebaseAuth.currentUser ?: return null
        val name = displayName.trim()
        if (name.isEmpty()) return user
        val request = UserProfileChangeRequest.Builder().setDisplayName(name).build()
        user.updateProfile(request).await()
        return firebaseAuth.currentUser
    }
    
    fun signOut() {
        firebaseAuth.signOut()
    }
}
