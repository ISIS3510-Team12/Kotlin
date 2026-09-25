package com.team12kotlin.juggle.ui.home

import androidx.lifecycle.ViewModel
import com.team12kotlin.juggle.ui.dto.Notification
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

data class HomeUiState(
    val tasks: List<Task> = emptyList(),
    val user: User,
    val notifications: List<Notification> = emptyList(),
    val selectedTab: Int = 0,
    val showBottomSheet: Boolean = false
) {
    val upcomingTasks: List<Task>
        get() = tasks

    private fun List<Task>.filterFor(): List<Task> {
        val currentDate = LocalDate.now()
        // TODO: descomentar cuando se arregle el DTO de task
        return filter { task -> task.dueLabel != null
//            task.deadline != null && LocalDate.parse(task.deadline) < currentDate.plusDays(7)
//                    && LocalDate.parse(task.deadline) >= currentDate
        }
    }
}

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            user = User(firstName = "Victoria", email = "vs@gmail.com", major = "CS"),
            tasks = listOf(
                Task(id = "p1", title = "Finish Something bruh", isImportant = true),
                Task(id = "p2", title = "Terminar"),
                Task(id = "p3", title = "Work"),
                Task(id = "p4", title = "Finish Something bruh"),
                Task(id = "p5", title = "Terminar"),
                Task(id = "p6", title = "Work")
            ),
            notifications = listOf(
                Notification(id=1, title="Finished task", date="Thursday, September 10 2026 8:00am", origin = "Group dev", type = "complete")
            ),
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onTabSelected(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onQuickActionPress() {
        _uiState.update { it.copy(showBottomSheet = true) }
    }

    fun onDismissalBottomSheet() {
        _uiState.update { it.copy(showBottomSheet = false) }
    }
}