package com.team12kotlin.juggle

import android.app.Application
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.local.LastSync
import com.team12kotlin.juggle.data.local.UserCache

class JuggleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Dependencies.appContext = applicationContext
        LastSync.init(this)
        UserCache.init(this)
    }
}
