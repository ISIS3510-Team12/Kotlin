package com.team12kotlin.juggle.data.telemetry

import com.team12kotlin.juggle.ui.dto.TaskDetailSessionRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class TaskDetailSessionRequestTest {

    @Test
    fun serializesWithTheFieldNamesTheBackExpects() {
        val request = TaskDetailSessionRequest(
            taskId = 7,
            openedAt = "2026-10-03T10:00:00",
            closedAt = "2026-10-03T10:00:12",
            progressUpdated = true
        )

        val json = Json.parseToJsonElement(Json.encodeToString(TaskDetailSessionRequest.serializer(), request)).jsonObject

        assertEquals(setOf("task_id", "opened_at", "closed_at", "progress_updated"), json.keys)
        assertEquals(7, json.getValue("task_id").jsonPrimitive.int)
        assertEquals("2026-10-03T10:00:00", json.getValue("opened_at").jsonPrimitive.content)
        assertTrue(json.getValue("progress_updated").jsonPrimitive.boolean)
    }

    @Test
    fun dateTimeKeepsMillisecondsAtMost() {
        val withNanos = LocalDateTime.of(2026, 10, 3, 10, 0, 12, 123_456_789)

        assertEquals("2026-10-03T10:00:12.123", withNanos.toApiDateTime())
    }

    @Test
    fun dateTimeWithoutFractionIsStillIso() {
        assertEquals("2026-10-03T10:00:12", LocalDateTime.of(2026, 10, 3, 10, 0, 12).toApiDateTime())
    }
}
