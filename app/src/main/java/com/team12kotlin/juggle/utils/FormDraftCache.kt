package com.team12kotlin.juggle.utils

import java.util.concurrent.ConcurrentHashMap

class FormDraftCache {
    private val formDrafts = ConcurrentHashMap<String, Any>()

    @Suppress("UNCHECKED_CAST")
    fun <T: Any> get(key: String): T? = formDrafts[key] as? T

    fun put(key: String, draft: Any) {
        formDrafts[key] = draft
    }

    fun clear(key: String) {
        formDrafts.clear()
    }
}