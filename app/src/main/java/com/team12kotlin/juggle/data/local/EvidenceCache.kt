package com.team12kotlin.juggle.data.local

import android.content.Context
import java.io.File

class EvidenceCache(
    context: Context,
    private val maxFiles: Int = 50
) {

    private val directory = File(context.filesDir, "evidence").apply { mkdirs() }

    fun get(taskId: Int): ByteArray? {
        val file = fileFor(taskId)
        if (!file.exists()) return null
        file.setLastModified(System.currentTimeMillis())
        return runCatching { file.readBytes() }.getOrNull()
    }

    fun put(taskId: Int, bytes: ByteArray) {
        runCatching {
            fileFor(taskId).writeBytes(bytes)
            evict()
        }
    }

    fun remove(taskId: Int) {
        fileFor(taskId).delete()
    }

    fun clear() {
        directory.listFiles()?.forEach { it.delete() }
    }

    private fun fileFor(taskId: Int) = File(directory, "$taskId.jpg")

    private fun evict() {
        val files = directory.listFiles()?.sortedBy { it.lastModified() } ?: return
        if (files.size <= maxFiles) return
        files.take(files.size - maxFiles).forEach { it.delete() }
    }
}
