package com.example.all_in_one_chat.ui.timeline.mvi

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.all_in_one_chat.domain.usecase.AddNotificationUseCase
import com.example.all_in_one_chat.domain.usecase.ClearAllNotificationsUseCase
import com.example.all_in_one_chat.domain.usecase.DeleteNotificationUseCase
import com.example.all_in_one_chat.domain.usecase.GetNotificationCountUseCase
import com.example.all_in_one_chat.domain.usecase.GetNotificationsByAppTypeUseCase
import com.example.all_in_one_chat.domain.usecase.GetNotificationsStreamUseCase
import com.example.all_in_one_chat.domain.usecase.GetNotificationsUseCase
import com.example.all_in_one_chat.domain.usecase.SearchNotificationsUseCase
import com.example.all_in_one_chat.domain.usecase.SendReplyUseCase
import com.example.all_in_one_chat.mvi.MviViewModel
import com.example.all_in_one_chat.service.NotificationBridge
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val sendReplyUseCase: SendReplyUseCase,
    private val deleteNotificationUseCase: DeleteNotificationUseCase,
    private val addNotificationUseCase: AddNotificationUseCase,
    private val getNotificationsStreamUseCase: GetNotificationsStreamUseCase,
    private val getNotificationsByAppTypeUseCase: GetNotificationsByAppTypeUseCase,
    private val searchNotificationsUseCase: SearchNotificationsUseCase,
    private val clearAllNotificationsUseCase: ClearAllNotificationsUseCase,
    private val getNotificationCountUseCase: GetNotificationCountUseCase,
    @param:ApplicationContext private val context: Context,
    private val notificationBridge: NotificationBridge
) : MviViewModel<TimelineIntent, TimelineViewState, TimelineEffect>(
    initialState = TimelineViewState()
) {
    companion object {
        private const val TAG = "TimelineViewModel"
    }

    init {
        observeNotifications()
        observeFirestoreStream()
        checkNotificationListenerPermission()
    }

    /**
     * Observe notifications from the local notification listener service
     * These are newly captured notifications from the system
     */
    private fun observeNotifications() {
        notificationBridge.notificationFlow
            .onEach { notification ->
                Log.d(TAG, "New notification received: ${notification.title}")

                // Add to repository (which will sync to Firestore)
                viewModelScope.launch {
                    try {
                        addNotificationUseCase(notification)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error adding notification", e)
                    }
                }

                // Update UI state
                reduceState { state ->
                    state.copy(
                        notifications = (state.notifications + notification)
                            .sortedByDescending { it.timestamp }
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Observe real-time Firestore updates
     * This keeps the UI in sync with cloud data
     */
    private fun observeFirestoreStream() {
        getNotificationsStreamUseCase()
            .onEach { firestoreNotifications ->
                Log.d(TAG, "Firestore stream update: ${firestoreNotifications.size} notifications")

                // Apply current filter/search if any
                val displayNotifications = applyCurrentFiltersAndSearch(firestoreNotifications)

                reduceState { state ->
                    state.copy(
                        notifications = displayNotifications,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Apply current filter and search query to notifications
     */
    private fun applyCurrentFiltersAndSearch(notifications: List<com.example.all_in_one_chat.domain.model.NotificationData>): List<com.example.all_in_one_chat.domain.model.NotificationData> {
        val currentState = getCurrentState()
        var filtered = notifications

        // Apply app type filter
        if (currentState.filterAppType != null) {
            filtered = filtered.filter { it.appType == currentState.filterAppType }
        }

        // Apply search query
        if (currentState.searchQuery.isNotEmpty()) {
            filtered = filtered.filter { notification ->
                notification.title.contains(currentState.searchQuery, ignoreCase = true) ||
                notification.message.contains(currentState.searchQuery, ignoreCase = true) ||
                notification.bigText?.contains(currentState.searchQuery, ignoreCase = true) == true
            }
        }

        return filtered.sortedByDescending { it.timestamp }
    }

    /**
     * Check if notification listener permission is granted
     */
    private fun checkNotificationListenerPermission() {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        val isEnabled = flat != null && flat.contains(packageName)
        
        reduceState { state ->
            state.copy(hasNotificationListenerPermission = isEnabled)
        }
        
        if (!isEnabled) {
            Log.d(TAG, "Notification listener permission not granted")
            sendEffect(TimelineEffect.RequestNotificationListenerPermission)
        }
    }

    override suspend fun handleIntent(intent: TimelineIntent) {
        when (intent) {
            is TimelineIntent.LoadNotifications -> handleLoadNotifications()
            is TimelineIntent.ReplyToNotification -> handleReply(intent)
            is TimelineIntent.DeleteNotification -> handleDeleteNotification(intent)
            is TimelineIntent.FilterByApp -> handleFilterByApp(intent)
            is TimelineIntent.RefreshNotifications -> handleRefreshNotifications()
            is TimelineIntent.SearchNotifications -> handleSearchNotifications(intent)
            is TimelineIntent.RequestReplyPermission -> handleRequestReplyPermission(intent)
            is TimelineIntent.ClearAllNotifications -> handleClearAllNotifications()
        }
    }

    private suspend fun handleLoadNotifications() {
        setState(getCurrentState().copy(isLoading = true))
        try {
            val notifications = getNotificationsUseCase()
            val displayNotifications = applyCurrentFiltersAndSearch(notifications)

            reduceState { state ->
                state.copy(
                    notifications = displayNotifications,
                    isLoading = false,
                    error = null
                )
            }
            Log.d(TAG, "Notifications loaded: ${notifications.size} total, ${displayNotifications.size} displayed")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading notifications", e)
            reduceState { state ->
                state.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
            sendEffect(TimelineEffect.ShowError(e.message ?: "Failed to load notifications"))
        }
    }

    private suspend fun handleReply(intent: TimelineIntent.ReplyToNotification) {
        try {
            sendReplyUseCase(intent.notificationId, intent.message)
            sendEffect(TimelineEffect.ReplySent)
            sendEffect(TimelineEffect.ShowMessage("메시지가 전송되었습니다"))
            Log.d(TAG, "Reply sent successfully for notification ${intent.notificationId}")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending reply", e)
            sendEffect(TimelineEffect.ShowError(e.message ?: "Failed to send reply"))
        }
    }

    private suspend fun handleDeleteNotification(intent: TimelineIntent.DeleteNotification) {
        try {
            deleteNotificationUseCase(intent.notificationId)
            reduceState { state ->
                state.copy(
                    notifications = state.notifications.filter { it.id != intent.notificationId }
                )
            }
            sendEffect(TimelineEffect.ShowMessage("알림이 삭제되었습니다"))
            Log.d(TAG, "Notification deleted: ${intent.notificationId}")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting notification", e)
            sendEffect(TimelineEffect.ShowError(e.message ?: "Failed to delete notification"))
        }
    }

    private suspend fun handleFilterByApp(intent: TimelineIntent.FilterByApp) {
        try {
            val filtered = if (intent.appType != null) {
                getNotificationsByAppTypeUseCase(intent.appType)
            } else {
                getNotificationsUseCase()
            }

            reduceState { state ->
                state.copy(
                    filterAppType = intent.appType,
                    notifications = filtered.sortedByDescending { it.timestamp }
                )
            }
            Log.d(TAG, "Filter applied: ${intent.appType?.displayName() ?: "All apps"}")
        } catch (e: Exception) {
            Log.e(TAG, "Error filtering notifications", e)
            sendEffect(TimelineEffect.ShowError(e.message ?: "Failed to filter notifications"))
        }
    }

    private suspend fun handleRefreshNotifications() {
        Log.d(TAG, "Refreshing notifications...")
        handleLoadNotifications()
    }

    private suspend fun handleSearchNotifications(intent: TimelineIntent.SearchNotifications) {
        try {
            val results = if (intent.query.isNotEmpty()) {
                searchNotificationsUseCase(intent.query)
            } else {
                getNotificationsUseCase()
            }

            reduceState { state ->
                state.copy(
                    searchQuery = intent.query,
                    notifications = results.sortedByDescending { it.timestamp }
                )
            }
            Log.d(TAG, "Search completed: ${results.size} results for '${intent.query}'")
        } catch (e: Exception) {
            Log.e(TAG, "Error searching notifications", e)
            sendEffect(TimelineEffect.ShowError(e.message ?: "Failed to search notifications"))
        }
    }

    private suspend fun handleRequestReplyPermission(intent: TimelineIntent.RequestReplyPermission) {
        val notification = getCurrentState().notifications.find { it.id == intent.notificationId }
        if (notification != null) {
            sendEffect(TimelineEffect.ShowReplyDialog(intent.notificationId, notification.displayName()))
        }
    }

    private suspend fun handleClearAllNotifications() {
        try {
            val success = clearAllNotificationsUseCase()
            if (success) {
                reduceState { state ->
                    state.copy(notifications = emptyList())
                }
                sendEffect(TimelineEffect.ShowMessage("모든 알림이 삭제되었습니다"))
                Log.d(TAG, "All notifications cleared")
            } else {
                sendEffect(TimelineEffect.ShowError("Failed to clear notifications"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing all notifications", e)
            sendEffect(TimelineEffect.ShowError(e.message ?: "Failed to clear notifications"))
        }
    }
}
