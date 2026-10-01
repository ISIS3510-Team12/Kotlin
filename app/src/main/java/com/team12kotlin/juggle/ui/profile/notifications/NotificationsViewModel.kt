package com.team12kotlin.juggle.ui.profile.notifications

import androidx.lifecycle.ViewModel
import com.team12kotlin.juggle.ui.dto.Notification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class NotificationsUiState(
    val notifications: List<Notification> = emptyList()
)

class NotificationsViewModel: ViewModel() {

    private  val _uiState = MutableStateFlow(
        NotificationsUiState(
            notifications = listOf(
                Notification(id=1, title="Finished task", date="Thursday, September 10 2026 8:00am", origin = "Group dev", type = "complete"),
                Notification(id=1, title="Edited task", date="Thursday, September 10 2026 8:00am", origin = "Group dev", type = "edit")
            ),
        )
    )
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    fun onNotificationDismissed(id: Int) {
        // TODO: change it so it actually deletes the notification from the model
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.filter { it.id != id }
            )
        }
    }

}