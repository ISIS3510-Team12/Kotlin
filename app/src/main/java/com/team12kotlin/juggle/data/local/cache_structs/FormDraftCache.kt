package com.team12kotlin.juggle.data.local.cache_structs

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

    /** Drops every draft, so the next session does not find what the previous one left half written. */
    fun clearAll() {
        formDrafts.clear()
    }
}