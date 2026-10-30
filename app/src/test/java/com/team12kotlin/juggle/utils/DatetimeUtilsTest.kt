package com.team12kotlin.juggle.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.time.LocalDateTime
import java.util.Locale

class DatetimeUtilsTest {

    companion object {
        private val originalLocale: Locale = Locale.getDefault()

        // The month formatter reads the default locale when the file is first used.
        @BeforeClass
        @JvmStatic
        fun useEnglishLocale() {
            Locale.setDefault(Locale.ENGLISH)
        }

        @AfterClass
        @JvmStatic
        fun restoreLocale() {
            Locale.setDefault(originalLocale)
        }
    }

    private val now = LocalDateTime.of(2026, 10, 28, 8, 30, 0)

    @Test
    fun taskDueIsNullForMissingOrUnparseableDeadlines() {
        assertNull(taskDue(null, now))
        assertNull(taskDue("", now))
        assertNull(taskDue("not a date", now))
    }

    @Test
    fun taskDueFlagsPastDeadlinesAsOverdue() {
        val due = taskDue("2026-10-27T09:00:00", now)

        assertEquals(TaskDue("Overdue", true), due)
    }

    @Test
    fun taskDueCountsTheHoursLeftToday() {
        assertEquals(TaskDue("Today - 14 hours left", false), taskDue("2026-10-28T23:00:00", now))
        assertEquals(TaskDue("Today - 1 hour left", false), taskDue("2026-10-28T09:45:00", now))
    }

    @Test
    fun taskDueLabelsTomorrowAndLaterDates() {
        assertEquals(TaskDue("Tomorrow - 15 hours left", false), taskDue("2026-10-29T00:00:00", now))
        assertEquals(TaskDue("Oct 31", false), taskDue("2026-10-31T10:00:00", now))
    }

    @Test
    fun taskDueAcceptsDeadlinesWithOffsetAndMilliseconds() {
        assertEquals(TaskDue("Oct 31", false), taskDue("2026-10-31T10:00:00.123", now))
        assertEquals(TaskDue("Oct 31", false), taskDue("2026-10-31T10:00:00+00:00", now))
    }

    @Test
    fun nextTaskMetaJoinsGroupAndDueText() {
        assertEquals("Group dev  ·  Today - 14 hours left", nextTaskMeta("Group dev", "2026-10-28T23:00:00", now))
        assertEquals("Group dev  ·  Oct 31", nextTaskMeta("Group dev", "2026-10-31T10:00:00", now))
    }

    @Test
    fun nextTaskMetaShowsTheDateInsteadOfOverdueWhenPastDue() {
        assertEquals("Group dev  ·  Oct 24", nextTaskMeta("Group dev", "2026-10-24T17:00:00", now))
    }

    @Test
    fun nextTaskMetaOmitsWhatIsMissing() {
        assertEquals("Oct 31", nextTaskMeta("", "2026-10-31T10:00:00", now))
        assertEquals("Group dev", nextTaskMeta("Group dev", null, now))
        assertEquals("Group dev", nextTaskMeta("Group dev", "garbage", now))
        assertEquals("Group dev", nextTaskMeta("Group dev", "  ", now))
        assertEquals("", nextTaskMeta("", null, now))
        assertEquals("", nextTaskMeta("   ", "garbage", now))
    }
}
