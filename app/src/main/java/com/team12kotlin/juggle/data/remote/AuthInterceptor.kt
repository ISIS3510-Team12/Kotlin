package com.team12kotlin.juggle.data.remote

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Interceptor
import okhttp3.Response
import kotlin.coroutines.resume

class AuthInterceptor(
    private val firebaseAuth: FirebaseAuth
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val user = firebaseAuth.currentUser
            ?: return chain.proceed(chain.request())

        val token = runBlocking { awaitIdToken(user) }
            ?: return chain.proceed(chain.request())

        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        return chain.proceed(request)
    }

    private suspend fun awaitIdToken(user: FirebaseUser): String? =
        suspendCancellableCoroutine { continuation ->
            user.getIdToken(false).addOnCompleteListener { task: Task<GetTokenResult> ->
                if (task.isSuccessful) {
                    continuation.resume(task.result?.token)
                } else {
                    continuation.resume(null)
                }
            }
        }
}
