package com.team12kotlin.juggle.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.repository.AuthRepository
import com.team12kotlin.juggle.data.repository.GroupRepository
import com.team12kotlin.juggle.data.repository.NotificationRepository
import com.team12kotlin.juggle.data.repository.SmartRepository
import com.team12kotlin.juggle.data.repository.TaskRepository
import com.team12kotlin.juggle.data.repository.UserRepository
import com.team12kotlin.juggle.ui.dto.NextTask
import com.team12kotlin.juggle.ui.dto.NextTaskFlag
import com.team12kotlin.juggle.ui.dto.NextTaskResponse
import com.team12kotlin.juggle.ui.dto.Notification
import com.team12kotlin.juggle.ui.dto.Task
import com.team12kotlin.juggle.ui.dto.TaskNotification
import com.team12kotlin.juggle.ui.dto.User
import com.team12kotlin.juggle.ui.dto.toFlag
import com.team12kotlin.juggle.ui.dto.toNotification
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface NextTaskUiState {
    data object Loading : NextTaskUiState
    data class Content(val task: NextTask, val flag: NextTaskFlag?) : NextTaskUiState
    data class Empty(val message: String) : NextTaskUiState
    data object Error : NextTaskUiState
}

internal const val NEXT_TASK_EMPTY_MESSAGE = "You have no pending tasks in the next 14 days."

internal fun reduceNextTask(previous: NextTaskUiState, result: Result<NextTaskResponse>): NextTaskUiState {
    val response = result.getOrNull()
        ?: return if (previous is NextTaskUiState.Loading) NextTaskUiState.Error else previous

    val task = response.task
    return if (task != null && task.taskId > 0) {
        NextTaskUiState.Content(task = task, flag = task.toFlag())
    } else {
        NextTaskUiState.Empty(message = response.message?.takeIf { it.isNotBlank() } ?: NEXT_TASK_EMPTY_MESSAGE)
    }
}

data class HomeUiState(
    val user: User = User(userId = "", firstName = ""),
    val upcomingTasks: List<Task> = emptyList(),
    val notifications: List<Notification> = emptyList(),
    val selectedTab: Int = 0,
    val showBottomSheet: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val firstGroupId: Int? = null,
    val nextTask: NextTaskUiState = NextTaskUiState.Loading,
) {
    val taskCount: Int get() = upcomingTasks.size
    val notificationCount: Int get() = notifications.size
}

class HomeViewModel : ViewModel() {

    private val authRepository: AuthRepository = Dependencies.authRepository
    private val userRepository: UserRepository = Dependencies.userRepository
    private val taskRepository: TaskRepository = Dependencies.taskRepository
    private val notificationRepository: NotificationRepository = Dependencies.notificationRepository
    private val groupRepository: GroupRepository = Dependencies.groupRepository
    private val smartRepository: SmartRepository = Dependencies.smartRepository

    private val _uiState = MutableStateFlow(HomeUiState(user = currentFirebaseUser(), isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var hasLoaded = false
    private var nextTaskJob: Job? = null

    fun onTabSelected(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onQuickActionPress() {
        _uiState.update { it.copy(showBottomSheet = true) }
    }

    fun onDismissalBottomSheet() {
        _uiState.update { it.copy(showBottomSheet = false) }
    }

    fun loadNextTask() {
        nextTaskJob?.cancel()
        nextTaskJob = viewModelScope.launch {
            val result = runCatching { smartRepository.getNextTask() }
            result.exceptionOrNull()?.let { if (it is CancellationException) throw it }
            _uiState.update { it.copy(nextTask = reduceNextTask(it.nextTask, result)) }
        }
    }

    fun load() {
        // Separate from the calls below so a failure here never affects the rest of Home
        loadNextTask()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !hasLoaded, errorMessage = null) }

            runCatching {
                // The four calls are independent
                coroutineScope {
                    val user = async { userRepository.getCurrentUser() }
                    val tasks = async {
                        taskRepository.getAllTasks(dueWithinDays = UPCOMING_DAYS, mine = true)
                    }
                    val notifications = async { notificationRepository.getNotifications() }
                    val groups = async { groupRepository.getGroups() }
                    Loaded(
                        user = user.await(),
                        tasks = tasks.await(),
                        notifications = notifications.await(),
                        firstGroupId = groups.await().firstOrNull()?.id
                    )
                }
            }
                .onSuccess { loaded ->
                    hasLoaded = true
                    _uiState.update {
                        it.copy(
                            user = loaded.user,
                            upcomingTasks = loaded.tasks,
                            notifications = loaded.notifications.map { item -> item.toNotification() },
                            firstGroupId = loaded.firstGroupId,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    private fun currentFirebaseUser(): User {
        val current = authRepository.currentUser ?: return User(userId = "", firstName = "")
        val firstName = current.displayName?.trim()?.substringBefore(' ').orEmpty()
        return User(userId = current.uid, firstName = firstName)
    }

    private data class Loaded(
        val user: User,
        val tasks: List<Task>,
        val notifications: List<TaskNotification>,
        val firstGroupId: Int?
    )

    private companion object {
        const val UPCOMING_DAYS = 7
    }
}
