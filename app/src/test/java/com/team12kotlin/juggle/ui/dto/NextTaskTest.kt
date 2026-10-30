package com.team12kotlin.juggle.ui.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NextTaskTest {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    private fun decode(body: String): NextTaskResponse =
        json.decodeFromString(NextTaskResponse.serializer(), body)

    @Test
    fun decodesATaskWithEveryField() {
        val response = decode(
            """
            {"task":{"task_id":37,"title":"Finish the Figma prototype","group_id":10,
            "group_name":"Group dev","due_at":"2026-10-28T23:00:00",
            "reason_code":"due_today","reason_text":"Due today"},"message":null}
            """
        )

        val task = response.task
        assertNotNull(task)
        assertEquals(37, task!!.taskId)
        assertEquals("Finish the Figma prototype", task.title)
        assertEquals(10, task.groupId)
        assertEquals("Group dev", task.groupName)
        assertEquals("2026-10-28T23:00:00", task.dueAt)
        assertEquals("due_today", task.reasonCode)
        assertEquals("Due today", task.reasonText)
        assertNull(response.message)
    }

    @Test
    fun decodesTheEmptyRecommendationWithItsMessage() {
        val response = decode(
            """{"task":null,"message":"You have no pending tasks in the next 14 days."}"""
        )

        assertNull(response.task)
        assertEquals("You have no pending tasks in the next 14 days.", response.message)
    }

    @Test
    fun ignoresFieldsTheClientDoesNotKnow() {
        val response = decode(
            """{"task":{"task_id":1,"title":"A","brand_new_field":42,"reason_code":"overdue"},"extra":true}"""
        )

        assertEquals(1, response.task!!.taskId)
    }

    @Test
    fun acceptsAnEmptyGroupNameAndANullDueDate() {
        val task = decode(
            """{"task":{"task_id":2,"title":"B","group_name":"","due_at":null,"reason_code":"priority"}}"""
        ).task!!

        assertEquals("", task.groupName)
        assertNull(task.dueAt)
    }

    @Test
    fun fillsDefaultsWhenTheTaskHasNoFields() {
        val task = decode("""{"task":{}}""").task!!

        assertEquals(0, task.taskId)
        assertEquals("", task.title)
        assertEquals("", task.groupName)
        assertNull(task.dueAt)
        assertEquals("", task.reasonCode)
    }

    @Test
    fun treatsANullReasonCodeAsUnknown() {
        val task = decode("""{"task":{"task_id":3,"reason_code":null}}""").task!!

        assertEquals("", task.reasonCode)
        assertEquals(NextTaskReason.NEXT_DEADLINE, task.reason)
    }

    @Test
    fun mapsEveryKnownReasonCode() {
        assertEquals(NextTaskReason.OVERDUE, NextTaskReason.fromCode("overdue"))
        assertEquals(NextTaskReason.DUE_TODAY, NextTaskReason.fromCode("due_today"))
        assertEquals(NextTaskReason.DUE_SOON, NextTaskReason.fromCode("due_soon"))
        assertEquals(NextTaskReason.PRIORITY, NextTaskReason.fromCode("priority"))
        assertEquals(NextTaskReason.NEEDS_HELP, NextTaskReason.fromCode("needs_help"))
        assertEquals(NextTaskReason.NEXT_DEADLINE, NextTaskReason.fromCode("next_deadline"))
    }

    @Test
    fun unknownReasonCodesFallBackToNextDeadline() {
        assertEquals(NextTaskReason.NEXT_DEADLINE, NextTaskReason.fromCode(""))
        assertEquals(NextTaskReason.NEXT_DEADLINE, NextTaskReason.fromCode("weird"))
        assertEquals(NextTaskReason.NEXT_DEADLINE, NextTaskReason.fromCode("OVERDUE"))
    }

    @Test
    fun readsTheReasonOfATask() {
        assertEquals(NextTaskReason.NEEDS_HELP, NextTask(reasonCode = "needs_help").reason)
    }

    @Test
    fun buildsTheFlagFromTheReasonText() {
        val flag = NextTask(reasonCode = "overdue", reasonText = "Overdue by 3 days").toFlag()

        assertEquals(NextTaskFlag(NextTaskReason.OVERDUE, "Overdue by 3 days"), flag)
    }

    @Test
    fun hasNoFlagWhenTheReasonTextIsBlank() {
        assertNull(NextTask(reasonCode = "overdue", reasonText = "").toFlag())
        assertNull(NextTask(reasonCode = "overdue", reasonText = "   ").toFlag())
    }

    @Test
    fun keepsTheFlagTextForAnUnknownCode() {
        val flag = NextTask(reasonCode = "weird", reasonText = "Something new").toFlag()

        assertEquals(NextTaskReason.NEXT_DEADLINE, flag!!.reason)
        assertEquals("Something new", flag.text)
    }
}
