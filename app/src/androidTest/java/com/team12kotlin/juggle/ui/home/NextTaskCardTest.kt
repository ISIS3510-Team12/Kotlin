package com.team12kotlin.juggle.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.team12kotlin.juggle.ui.dto.NextTask
import com.team12kotlin.juggle.ui.dto.toFlag
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NextTaskCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val task = NextTask(
        taskId = 37,
        title = "Finish the Figma prototype",
        groupName = "Group dev",
        dueAt = "2099-11-08T11:00:00",
        reasonCode = "due_today",
        reasonText = "Due today"
    )

    private fun setContent(fontScale: Float = 1f, content: @Composable () -> Unit) {
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                JuggleTheme { content() }
            }
        }
    }

    @Test
    fun showsTheTitleTheFlagAndTheGroup() {
        setContent { NextTaskCard(task = task, flag = task.toFlag()) }

        composeRule.onNodeWithText("Finish the Figma prototype").assertIsDisplayed()
        composeRule.onNodeWithText("Due today").assertIsDisplayed()
        composeRule.onNodeWithText("Group dev", substring = true).assertIsDisplayed()
    }

    @Test
    fun hidesTheFlagWhenThereIsNone() {
        setContent { NextTaskCard(task = task, flag = null) }

        composeRule.onNodeWithText("Finish the Figma prototype").assertIsDisplayed()
        composeRule.onAllNodes(hasText("Due today")).assertCountEquals(0)
    }

    @Test
    fun isASingleClickableNodeThatReadsTheWholeCard() {
        setContent { NextTaskCard(task = task, flag = task.toFlag()) }

        composeRule
            .onAllNodes(
                hasClickAction() and
                    hasText("Finish the Figma prototype") and
                    hasText("Due today") and
                    hasText("Group dev", substring = true)
            )
            .assertCountEquals(1)
    }

    @Test
    fun clickingTheCardInvokesTheCallback() {
        var clicks = 0
        setContent { NextTaskCard(task = task, flag = task.toFlag(), onClick = { clicks++ }) }

        composeRule.onNodeWithText("Finish the Figma prototype").performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun anUnknownReasonCodeStillRendersTheCard() {
        val unknown = task.copy(reasonCode = "something_new", reasonText = "Something new")
        setContent { NextTaskCard(task = unknown, flag = unknown.toFlag()) }

        composeRule.onNodeWithText("Something new").assertIsDisplayed()
        composeRule.onNodeWithText("Finish the Figma prototype").assertIsDisplayed()
    }

    @Test
    fun aTaskWithoutGroupOrDueDateStillRendersTheCard() {
        val bare = task.copy(groupName = "", dueAt = null)
        setContent { NextTaskCard(task = bare, flag = bare.toFlag()) }

        composeRule.onNodeWithText("Finish the Figma prototype").assertIsDisplayed()
        composeRule.onNodeWithText("Due today").assertIsDisplayed()
    }

    @Test
    fun aLongTitleStaysInsideTheCardAtTwiceTheFontScale() {
        val long = task.copy(title = "Prepare the final presentation and ".repeat(5).trim())
        setContent(fontScale = 2f) { NextTaskCard(task = long, flag = long.toFlag()) }

        composeRule.onNodeWithText(long.title).assertIsDisplayed()
        composeRule.onNodeWithText("Due today").assertIsDisplayed()
        composeRule.onNodeWithText("Group dev", substring = true).assertIsDisplayed()
    }

    @Test
    fun sectionShowsTheCardAndReportsTheTaskId() {
        var clickedId: Int? = null
        setContent {
            NextTaskSection(
                state = NextTaskUiState.Content(task, task.toFlag()),
                onTaskClick = { clickedId = it }
            )
        }

        composeRule.onNodeWithText("Finish the Figma prototype").performClick()

        assertEquals(37, clickedId)
    }

    @Test
    fun sectionShowsTheEmptyMessage() {
        setContent { NextTaskSection(state = NextTaskUiState.Empty(NEXT_TASK_EMPTY_MESSAGE)) }

        composeRule.onNodeWithText(NEXT_TASK_EMPTY_MESSAGE).assertIsDisplayed()
    }

    @Test
    fun sectionShowsTheSharedErrorCard() {
        setContent { NextTaskSection(state = NextTaskUiState.Error) }

        composeRule
            .onNodeWithText("An error occurred while loading the resource. Try again later.")
            .assertIsDisplayed()
    }

    @Test
    fun sectionShowsASpinnerWhileLoading() {
        composeRule.mainClock.autoAdvance = false
        setContent { NextTaskSection(state = NextTaskUiState.Loading) }

        composeRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }
}
