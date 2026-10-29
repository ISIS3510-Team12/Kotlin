package com.team12kotlin.juggle.ui.home

import com.team12kotlin.juggle.ui.dto.NextTask
import com.team12kotlin.juggle.ui.dto.NextTaskFlag
import com.team12kotlin.juggle.ui.dto.NextTaskReason
import com.team12kotlin.juggle.ui.dto.NextTaskResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.IOException

class NextTaskReducerTest {

    private val task = NextTask(
        taskId = 37,
        title = "Finish the Figma prototype",
        groupId = 10,
        groupName = "Group dev",
        dueAt = "2026-10-28T23:00:00",
        reasonCode = "due_today",
        reasonText = "Due today"
    )

    private val content = NextTaskUiState.Content(
        task = task,
        flag = NextTaskFlag(NextTaskReason.DUE_TODAY, "Due today")
    )

    @Test
    fun showsTheTaskWithItsFlagOnSuccess() {
        val state = reduceNextTask(NextTaskUiState.Loading, Result.success(NextTaskResponse(task = task)))

        assertEquals(content, state)
    }

    @Test
    fun hasNoFlagWhenTheReasonTextIsBlank() {
        val bare = task.copy(reasonText = "")

        val state = reduceNextTask(NextTaskUiState.Loading, Result.success(NextTaskResponse(task = bare)))

        assertEquals(NextTaskUiState.Content(task = bare, flag = null), state)
    }

    @Test
    fun showsTheServerMessageWhenThereIsNoTask() {
        val response = NextTaskResponse(message = "Nothing pending")

        val state = reduceNextTask(NextTaskUiState.Loading, Result.success(response))

        assertEquals(NextTaskUiState.Empty("Nothing pending"), state)
    }

    @Test
    fun usesTheDefaultMessageWhenThereIsNoTaskAndNoMessage() {
        val state = reduceNextTask(NextTaskUiState.Loading, Result.success(NextTaskResponse()))

        assertEquals(NextTaskUiState.Empty(NEXT_TASK_EMPTY_MESSAGE), state)
    }

    @Test
    fun treatsATaskWithoutAnIdAsEmpty() {
        val response = NextTaskResponse(task = NextTask(taskId = 0, title = "Ghost"))

        val state = reduceNextTask(NextTaskUiState.Loading, Result.success(response))

        assertEquals(NextTaskUiState.Empty(NEXT_TASK_EMPTY_MESSAGE), state)
    }

    @Test
    fun showsAnErrorWhenItFailsAndThereWasNoData() {
        val state = reduceNextTask(NextTaskUiState.Loading, Result.failure(IOException("offline")))

        assertEquals(NextTaskUiState.Error, state)
    }

    @Test
    fun keepsThePreviousTaskWhenARefreshFails() {
        val state = reduceNextTask(content, Result.failure(IOException("offline")))

        assertSame(content, state)
    }

    @Test
    fun keepsThePreviousEmptyStateWhenARefreshFails() {
        val previous = NextTaskUiState.Empty("Nothing pending")

        assertSame(previous, reduceNextTask(previous, Result.failure(IOException("offline"))))
    }

    @Test
    fun recoversFromAnErrorOnTheNextSuccess() {
        val state = reduceNextTask(NextTaskUiState.Error, Result.success(NextTaskResponse(task = task)))

        assertEquals(content, state)
    }

    @Test
    fun replacesThePreviousTaskWhenANewOneArrives() {
        val other = task.copy(taskId = 38, title = "Review the API contract", reasonCode = "due_soon", reasonText = "Due tomorrow")

        val state = reduceNextTask(content, Result.success(NextTaskResponse(task = other)))

        assertEquals(NextTaskUiState.Content(other, NextTaskFlag(NextTaskReason.DUE_SOON, "Due tomorrow")), state)
    }

    @Test
    fun replacesThePreviousTaskWithTheEmptyStateWhenNothingIsPending() {
        val state = reduceNextTask(content, Result.success(NextTaskResponse(message = "Nothing pending")))

        assertEquals(NextTaskUiState.Empty("Nothing pending"), state)
    }

    @Test
    fun showsTheTaskAfterAnEmptyState() {
        val state = reduceNextTask(NextTaskUiState.Empty("Nothing pending"), Result.success(NextTaskResponse(task = task)))

        assertEquals(content, state)
    }

    @Test
    fun staysInErrorWhenItKeepsFailing() {
        val state = reduceNextTask(NextTaskUiState.Error, Result.failure(IOException("offline")))

        assertSame(NextTaskUiState.Error, state)
    }

    @Test
    fun treatsANegativeTaskIdAsEmpty() {
        val response = NextTaskResponse(task = NextTask(taskId = -1, title = "Ghost"))

        assertEquals(NextTaskUiState.Empty(NEXT_TASK_EMPTY_MESSAGE), reduceNextTask(NextTaskUiState.Loading, Result.success(response)))
    }

    @Test
    fun prefersTheServerMessageEvenWhenTheTaskHasNoId() {
        val response = NextTaskResponse(task = NextTask(taskId = 0), message = "Nothing pending")

        assertEquals(NextTaskUiState.Empty("Nothing pending"), reduceNextTask(NextTaskUiState.Loading, Result.success(response)))
    }

    @Test
    fun fallsBackToTheDefaultMessageWhenTheServerMessageIsBlank() {
        assertEquals(NextTaskUiState.Empty(NEXT_TASK_EMPTY_MESSAGE), reduceNextTask(NextTaskUiState.Loading, Result.success(NextTaskResponse(message = ""))))
        assertEquals(NextTaskUiState.Empty(NEXT_TASK_EMPTY_MESSAGE), reduceNextTask(NextTaskUiState.Loading, Result.success(NextTaskResponse(message = "   "))))
    }
}
