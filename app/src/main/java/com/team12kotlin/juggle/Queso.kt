
package com.team12kotlin.juggle

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun ScrollableCalendar() {
    val currentMonth = YearMonth.now()

    // Create a range of 24 months (12 past, 12 future)
    val totalMonths = 24
    val initialPage = 12

    val pagerState = rememberPagerState(
        initialPage = initialPage
    ) {
        totalMonths
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            // Calculate the target month based on page index
            val monthOffset = page - initialPage
            val targetMonth = currentMonth.plusMonths(
                monthOffset.toLong()
            )

            MonthView(yearMonth = targetMonth)
        }
    }
}

@Composable
fun MonthView(yearMonth: YearMonth) {
    val days = getDaysInMonth(yearMonth)

    Column {
        // Month Header (e.g., "October 2026")
        Text(
            text = "${yearMonth.month.getDisplayName(
                TextStyle.FULL,
                LocalLocale.current.platformLocale
            )} ${yearMonth.year}",
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            textAlign = TextAlign.Center
        )

        // 7-column calendar grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(300.dp)
        ) {
            items(days) { date ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (date != null) {
                        Text(
                            text = date.dayOfMonth.toString()
                        )
                    }
                }
            }
        }
    }
}

private fun getDaysInMonth(
    yearMonth: YearMonth
): List<LocalDate?> {
    val daysList = mutableListOf<LocalDate?>()
    val firstDayOfMonth = yearMonth.atDay(1)

    // Calculate offset assuming the week starts on Sunday
    val emptyDaysBefore =
        firstDayOfMonth.dayOfWeek.value % 7

    repeat(emptyDaysBefore) {
        daysList.add(null)
    }

    val totalDays = yearMonth.lengthOfMonth()

    for (day in 1..totalDays) {
        daysList.add(yearMonth.atDay(day))
    }

    return daysList
}

