package com.team12kotlin.juggle.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val DATE_UI: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
private val TIME_UI: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val ISO_LOCAL: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

fun splitDeadline(deadline: String?): Pair<String, String> {
    if (deadline.isNullOrBlank()) return "" to ""

    val parsed = parseDeadline(deadline)
    if (parsed == null) return "" to ""

    return parsed.toLocalDate().format(DATE_UI) to parsed.toLocalTime().format(TIME_UI)
}

fun toIsoDateTime(date: String, time: String): String? {
    if (date.isBlank()) return null
    return try {
        val localDate = LocalDate.parse(date, DATE_UI)
        val localTime = if (time.isBlank()) LocalTime.MIDNIGHT else LocalTime.parse(time, TIME_UI)
        LocalDateTime.of(localDate, localTime).format(ISO_LOCAL)
    } catch (_: Exception) {
        null
    }
}

private val DUE_DATE_UI: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")

data class TaskDue(val text: String, val isOverdue: Boolean)

fun taskDue(deadline: String?, now: LocalDateTime = LocalDateTime.now()): TaskDue? {
    if (deadline.isNullOrBlank()) return null

    val parsed = parseDeadline(deadline) ?: return null
    if (parsed.isBefore(now)) return TaskDue("Overdue", true)

    val dueDate = parsed.toLocalDate()
    val days = ChronoUnit.DAYS.between(now.toLocalDate(), dueDate)
    val dayLabel = when (days) {
        0L -> "Today"
        1L -> "Tomorrow"
        else -> dueDate.format(DUE_DATE_UI)
    }
    if (days > 1L) return TaskDue(dayLabel, false)

    val hours = ChronoUnit.HOURS.between(now, parsed)
    val hourText = if (hours == 1L) "1 hour" else "$hours hours"
    return TaskDue("$dayLabel - $hourText left", false)
}

/** Text for how long ago a task changed; the backend sends UTC, so [now] is UTC too. */
fun lastUpdateText(lastChangedAt: String?, now: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)): String? {
    if (lastChangedAt.isNullOrBlank()) return null

    val changedAt = parseDeadline(lastChangedAt) ?: return null
    val minutes = maxOf(ChronoUnit.MINUTES.between(changedAt, now), 0L)
    if (minutes < 1L) return "Updated just now"
    if (minutes < 60L) return "Updated $minutes min ago"

    val hours = minutes / 60L
    if (hours < 24L) return if (hours == 1L) "Updated 1 hour ago" else "Updated $hours hours ago"

    val days = hours / 24L
    return if (days == 1L) "Updated 1 day ago" else "Updated $days days ago"
}

fun nextTaskMeta(groupName: String, dueAt: String?, now: LocalDateTime = LocalDateTime.now()): String {
    val parsed = dueAt?.takeIf { it.isNotBlank() }?.let(::parseDeadline)
    val due = when {
        parsed == null -> null
        parsed.isBefore(now) -> parsed.toLocalDate().format(DUE_DATE_UI)
        else -> taskDue(dueAt, now)?.text
    }
    return listOfNotNull(groupName.takeIf { it.isNotBlank() }, due).joinToString("  -  ")
}

fun parseDeadline(deadline: String): LocalDateTime? {
    return try {
        OffsetDateTime.parse(deadline).toLocalDateTime()
    } catch (_: Exception) {
        try {
            LocalDateTime.parse(deadline)
        } catch (_: Exception) {
            null
        }
    }
}
