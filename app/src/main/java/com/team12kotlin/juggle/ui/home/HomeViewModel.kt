package com.team12kotlin.juggle.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team12kotlin.juggle.data.Dependencies
import com.team12kotlin.juggle.data.local.UserCache
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
import com.team12kotlin.juggle.ui.utils.toUserMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
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
    val dataLoaded: Boolean = false,
    val tasksLoaded: Boolean = false,
    val notificationsLoaded: Boolean = false,
    val urgentLoaded: Boolean = false,
    val nextTask: NextTaskUiState = NextTaskUiState.Loading,
    val urgentCount: Int = 0
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

    init {
        viewModelScope.launch {
            Dependencies.pendingTaskStore.synced.filter { it > 0L }.collect { load() }
        }
    }

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
        // Retrying from the error state shows the spinner again, a refresh keeps the current card
        _uiState.update {
            if (it.nextTask is NextTaskUiState.Error) it.copy(nextTask = NextTaskUiState.Loading) else it
        }
        nextTaskJob = viewModelScope.launch {
            val result = runCatching { smartRepository.getNextTask() }
            result.exceptionOrNull()?.let { if (it is CancellationException) throw it }
            _uiState.update { it.copy(nextTask = reduceNextTask(it.nextTask, result)) }
        }
    }

    fun load() {
        loadNextTask()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !hasLoaded, errorMessage = null) }

            coroutineScope {
                val userResult = async { runCatching { userRepository.getCurrentUser() } }
                val tasksResult = async {
                    runCatching {
                        taskRepository.getAllTasks(dueWithinDays = UPCOMING_DAYS, mine = true)
                    }
                }
                val notificationsResult = async { runCatching { notificationRepository.getNotifications() } }
                val groupsResult = async { runCatching { groupRepository.getGroups() } }
                val urgentResult = async {
                    runCatching { taskRepository.getAllTasks(priority = true) }
                }

                val user = userResult.await()
                val tasks = tasksResult.await()
                val notifications = notificationsResult.await()
                val groups = groupsResult.await()
                val urgent = urgentResult.await()

                val realError = listOf(tasks, notifications, groups, urgent, user)
                    .firstNotNullOfOrNull { it.exceptionOrNull()?.toUserMessage() }

                hasLoaded = true
                user.getOrNull()?.let {
                    UserCache.save(firstName = it.firstName, displayName = it.displayName)
                }

                _uiState.update { state ->
                    state.copy(
                        user = user.getOrNull() ?: state.user,
                        upcomingTasks = tasks.getOrDefault(state.upcomingTasks),
                        notifications = notifications.getOrNull()
                            ?.map { item -> item.toNotification() }
                            ?: state.notifications,
                        firstGroupId = groups.getOrNull()?.firstOrNull()?.id ?: state.firstGroupId,
                        isLoading = false,
                        errorMessage = realError,
                        dataLoaded = tasks.isSuccess,
                        tasksLoaded = tasks.isSuccess,
                        notificationsLoaded = notifications.isSuccess,
                        urgentLoaded = urgent.isSuccess,
                        urgentCount = urgent.getOrNull()?.size ?: state.urgentCount
                    )
                }
            }
        }
    }

    private fun currentFirebaseUser(): User {
        val current = authRepository.currentUser
        val cachedName = UserCache.firstName()
        if (current == null) {
            return User(userId = "", firstName = cachedName)
        }
        val firstName = current.displayName?.trim()?.substringBefore(' ').orEmpty()
            .ifBlank { cachedName }
        return User(userId = current.uid, firstName = firstName)
    }

    private data class Loaded(
        val user: User,
        val tasks: List<Task>,
        val notifications: List<TaskNotification>,
        val firstGroupId: Int?,
        val urgent: List<Task>
    )

    private companion object {
        const val UPCOMING_DAYS = 7
    }
}
