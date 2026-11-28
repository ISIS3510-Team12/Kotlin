package com.team12kotlin.juggle.ui.tasks.board

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.outlined.Lock
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.TaskStatus
import com.team12kotlin.juggle.ui.tasks.TasksUiState
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/** Columns of the board, in the order they are shown. */
internal val BOARD_STATUSES = listOf(TaskStatus.NOT_STARTED, TaskStatus.IN_PROGRESS, TaskStatus.COMPLETED)

private val EDGE_SCROLL = 48.dp

// Each column takes most of the width, so the start of the next one shows and invites to swipe.
private val ColumnPageSize = object : PageSize {
    override fun Density.calculateMainAxisPageSize(availableSpace: Int, pageSpacing: Int): Int =
        ((availableSpace - 2 * pageSpacing) * 0.82f).toInt()
}

@Composable
fun TaskBoard(
    uiState: TasksUiState,
    isOffline: Boolean,
    modifier: Modifier = Modifier,
    onTaskClick: (Task) -> Unit = {},
    onMoveTask: (Task, TaskStatus) -> Unit = { _, _ -> },
    onOfflineMoveAttempt: () -> Unit = {}
) {
    val pagerState = rememberPagerState { BOARD_STATUSES.size }
    var taskToMove by remember { mutableStateOf<Task?>(null) }
    val dragState = remember { BoardDragState() }
    val columnBounds = remember { mutableStateMapOf<TaskStatus, Rect>() }
    var boardBounds by remember { mutableStateOf(Rect.Zero) }
    val density = LocalDensity.current

    // The column under the finger, unless it is the one the card is already in.
    val hoveredStatus by remember {
        derivedStateOf {
            val dragged = dragState.task ?: return@derivedStateOf null
            columnBounds.entries
                .firstOrNull { it.value.contains(dragState.pointer) }
                ?.key
                ?.takeIf { it != dragged.status }
        }
    }

    // Near the edges the board scrolls to the next column, so a card can cross the whole board.
    LaunchedEffect(dragState.isDragging) {
        val edge = with(density) { EDGE_SCROLL.toPx() }
        while (dragState.isDragging) {
            val x = dragState.pointer.x
            val target = when {
                x > boardBounds.right - edge -> pagerState.currentPage + 1
                x < boardBounds.left + edge -> pagerState.currentPage - 1
                else -> null
            }
            if (target != null && target in BOARD_STATUSES.indices) pagerState.animateScrollToPage(target)
            delay(150)
        }
    }

    val finishDrag = {
        val dragged = dragState.task
        val target = hoveredStatus
        dragState.end()
        if (dragged != null && target != null) onMoveTask(dragged, target)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { boardBounds = it.boundsInRoot() }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isOffline) ReadOnlyNotice()

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
                pageSize = ColumnPageSize,
                pageSpacing = 12.dp,
                // Every column stays composed, so the card being dragged never loses its gesture.
                beyondViewportPageCount = BOARD_STATUSES.size,
                userScrollEnabled = !dragState.isDragging
            ) { page ->
                val status = BOARD_STATUSES[page]
                BoardColumn(
                    status = status,
                    tasks = uiState.boardTasks(status),
                    unavailable = status == TaskStatus.COMPLETED && uiState.completedUnavailable,
                    locked = isOffline,
                    movingTaskIds = uiState.movingTaskIds,
                    draggingTaskId = dragState.task?.id,
                    highlighted = hoveredStatus == status,
                    onBoundsChange = { columnBounds[status] = it },
                    onTaskClick = onTaskClick,
                    onMoveClick = { task ->
                        if (isOffline) onOfflineMoveAttempt() else taskToMove = task
                    },
                    onDragStart = { task, pointer, grab, size -> dragState.start(task, pointer, grab, size) },
                    onDrag = dragState::move,
                    onDragEnd = finishDrag,
                    onLockedLongPress = onOfflineMoveAttempt
                )
            }

            // Keeps the dots above the bottom navigation bar.
            PageDots(
                count = BOARD_STATUSES.size,
                current = pagerState.currentPage,
                modifier = Modifier.padding(top = 10.dp, bottom = 118.dp)
            )
        }

        // The card lifts off the column, tilted, and follows the finger.
        dragState.task?.let { dragged ->
            BoardTaskCard(
                task = dragged,
                locked = false,
                moving = false,
                floating = true,
                modifier = Modifier
                    .width(with(density) { dragState.cardSize.width.toDp() })
                    .offset {
                        IntOffset(
                            (dragState.pointer.x - dragState.grab.x - boardBounds.left).roundToInt(),
                            (dragState.pointer.y - dragState.grab.y - boardBounds.top).roundToInt()
                        )
                    }
                    .graphicsLayer { rotationZ = -3f }
            )
        }
    }

    taskToMove?.let { task ->
        MoveTaskSheet(
            task = task,
            onSelect = { status ->
                taskToMove = null
                onMoveTask(task, status)
            },
            onDismiss = { taskToMove = null }
        )
    }
}

@Composable
private fun ReadOnlyNotice(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = MaterialSymbols.Outlined.Lock,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "The board is read only until you reconnect",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun PageDots(
    count: Int,
    current: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Column ${current + 1} of $count" },
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            val selected = index == current
            val size by animateDpAsState(if (selected) 8.dp else 6.dp, label = "dotSize")
            val color by animateColorAsState(
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                label = "dotColor"
            )
            Box(modifier = Modifier.size(size).clip(CircleShape).background(color))
        }
    }
}

@Composable
internal fun TaskStatus.boardColor(): Color = when (this) {
    TaskStatus.NOT_STARTED -> MaterialTheme.colorScheme.outline
    TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
    TaskStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
}

@Preview(showBackground = true)
@Composable
private fun TaskBoardPreview() {
    JuggleTheme {
        TaskBoard(
            uiState = TasksUiState(
                ownTasks = listOf(
                    Task(id = 1, title = "Finish the figma", needsHelp = true, deadline = "2026-12-01T12:00:00"),
                    Task(id = 2, title = "Do the design of the app", isPriority = true, status = TaskStatus.IN_PROGRESS)
                )
            ),
            isOffline = false
        )
    }
}
