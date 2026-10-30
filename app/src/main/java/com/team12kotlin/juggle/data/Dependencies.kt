package com.team12kotlin.juggle.data

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.team12kotlin.juggle.BuildConfig
import com.team12kotlin.juggle.data.local.AppDatabase
import com.team12kotlin.juggle.data.local.CacheConverter
import com.team12kotlin.juggle.data.local.NetworkFirstCache
import com.team12kotlin.juggle.data.local.mappers.GroupCacheMapper
import com.team12kotlin.juggle.data.local.mappers.ProjectCacheMapper
import com.team12kotlin.juggle.data.local.mappers.TaskCacheMapper
import com.team12kotlin.juggle.data.remote.AuthInterceptor
import com.team12kotlin.juggle.data.remote.JuggleApi
import com.team12kotlin.juggle.data.repository.AuthRepository
import com.team12kotlin.juggle.data.repository.GroupRepository
import com.team12kotlin.juggle.data.repository.LocationRepository
import com.team12kotlin.juggle.data.repository.NotificationRepository
import com.team12kotlin.juggle.data.repository.ProjectRepository
import com.team12kotlin.juggle.data.repository.SmartRepository
import com.team12kotlin.juggle.data.repository.TaskRepository
import com.team12kotlin.juggle.data.repository.TelemetryRepository
import com.team12kotlin.juggle.data.repository.UserRepository
import com.team12kotlin.juggle.sensors.ShakeEffectStore
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object Dependencies {

    lateinit var appContext: Context

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

    private val cacheJson: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(appContext, AppDatabase::class.java, "juggle.db")
            .build()
    }

    private val networkFirstCache: NetworkFirstCache by lazy {
        NetworkFirstCache(database, database.cachedMetadataDao())
    }

    private val groupCacheMapper by lazy { GroupCacheMapper(cacheConverter) }
    private val projectCacheMapper by lazy { ProjectCacheMapper() }
    private val taskCacheMapper by lazy { TaskCacheMapper(cacheConverter) }

    val authRepository: AuthRepository by lazy { AuthRepository(firebaseAuth) }
    val taskRepository: TaskRepository by lazy {
        TaskRepository(
            api,
            database.cachedTaskDao(),
            taskCacheMapper,
            networkFirstCache,
            authRepository.currentUserId()
        )
    }
    val groupRepository: GroupRepository by lazy {
        GroupRepository(
            api,
            database.cachedGroupDao(),
            database.cachedProjectDao(),
            database.cachedTaskDao(),
            groupCacheMapper,
            networkFirstCache,
            authRepository.currentUserId()
            )
    }
    val projectRepository: ProjectRepository by lazy {
        ProjectRepository(
            api,
            database.cachedProjectDao(),
            projectCacheMapper,
            networkFirstCache,
            authRepository.currentUserId()
        )
    }
    val userRepository: UserRepository by lazy { UserRepository(api) }
    val locationRepository: LocationRepository by lazy { LocationRepository(api) }
    val notificationRepository: NotificationRepository by lazy { NotificationRepository(api) }
    val telemetryRepository: TelemetryRepository by lazy { TelemetryRepository(api) }
    val smartRepository: SmartRepository by lazy { SmartRepository(api) }
    val shakeEffectStore: ShakeEffectStore by lazy { ShakeEffectStore(appContext) }

    val cacheConverter: CacheConverter by lazy {
        CacheConverter(cacheJson)
    }
}
