package com.team12kotlin.juggle.ui.utils

import retrofit2.HttpException
import java.io.IOException

fun Throwable.toUserMessage(): String = when (this) {
    is IOException ->
        "Couldn't reach the server. Check your connection."
    is HttpException -> when {
        code() >= 500 -> "The service is unavailable right now. Try again later."
        code() == 401 -> "Your session has expired. Sign in again."
        code() == 403 -> "You do not have permission to do that."
        code() == 404 -> "We could not find what you were looking for."
        else -> "Something went wrong. Try again."
    }
    else -> "Something went wrong. Try again."
}
