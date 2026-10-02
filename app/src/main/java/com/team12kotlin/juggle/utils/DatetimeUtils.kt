package com.team12kotlin.juggle.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
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

fun taskDue(deadline: String?): TaskDue? {
    if (deadline.isNullOrBlank()) return null

    val parsed = parseDeadline(deadline) ?: return null
    val now = LocalDateTime.now()
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
