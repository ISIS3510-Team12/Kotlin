package com.team12kotlin.juggle.data

import com.google.firebase.auth.FirebaseAuth
import com.team12kotlin.juggle.BuildConfig
import com.team12kotlin.juggle.data.remote.AuthInterceptor
import com.team12kotlin.juggle.data.remote.JuggleApi
import com.team12kotlin.juggle.data.repository.AuthRepository
import com.team12kotlin.juggle.data.repository.GroupRepository
import com.team12kotlin.juggle.data.repository.TaskRepository
import com.team12kotlin.juggle.data.repository.UserRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object Dependencies {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance().apply {
            if (BuildConfig.USE_FIREBASE_EMULATOR) {
                useEmulator(
                    BuildConfig.FIREBASE_EMULATOR_HOST,
                    BuildConfig.FIREBASE_AUTH_EMULATOR_PORT
                )
            }
        }
    }

    private val json: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            coerceInputValues = true
        }
    }

    private val api: JuggleApi by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(firebaseAuth))
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            )
            .build()

        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(JuggleApi::class.java)
    }

    val authRepository: AuthRepository by lazy { AuthRepository(firebaseAuth) }
    val taskRepository: TaskRepository by lazy { TaskRepository(api) }
    val groupRepository: GroupRepository by lazy { GroupRepository(api) }
    val userRepository: UserRepository by lazy { UserRepository(api) }
}
