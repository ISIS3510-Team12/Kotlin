package com.team12kotlin.juggle.ui.tasks.board

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.team12kotlin.juggle.ui.dto.Task

/** The card being dragged; the pointer and the card are in window coordinates. */
@Stable
internal class BoardDragState {
    var task by mutableStateOf<Task?>(null)
        private set
    var pointer by mutableStateOf(Offset.Zero)
        private set
    // Where inside the card it was picked up, so it stays under the finger.
    var grab by mutableStateOf(Offset.Zero)
        private set
    var cardSize by mutableStateOf(IntSize.Zero)
        private set

    val isDragging: Boolean
        get() = task != null

    fun start(task: Task, pointer: Offset, grab: Offset, cardSize: IntSize) {
        this.task = task
        this.pointer = pointer
        this.grab = grab
        this.cardSize = cardSize
    }

    fun move(delta: Offset) {
        pointer += delta
    }

    fun end() {
        task = null
    }
}
