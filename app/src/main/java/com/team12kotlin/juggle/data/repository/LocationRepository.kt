package com.team12kotlin.juggle.data.repository

import com.team12kotlin.juggle.data.remote.JuggleApi
import com.team12kotlin.juggle.ui.dto.UserLocation
import retrofit2.HttpException

class LocationRepository(
    private val api: JuggleApi
) {
    /** Returns the saved location, or null if there is none. */
    suspend fun getLocation(): UserLocation? = try {
        api.getLocation()
    } catch (error: HttpException) {
        if (error.code() == 404) null else throw error
    }

    suspend fun saveLocation(location: UserLocation): UserLocation = api.saveLocation(location)

    suspend fun deleteLocation() {
        api.deleteLocation()
    }
}
