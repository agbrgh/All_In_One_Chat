package com.example.all_in_one_chat.ui.timeline.mvi

import com.example.all_in_one_chat.domain.model.NotificationData
import com.example.all_in_one_chat.mvi.MviViewEffect
import com.example.all_in_one_chat.mvi.MviIntent
import com.example.all_in_one_chat.mvi.MviViewState

/**
 * Timeline screen intents (user actions)
 */
sealed interface TimelineIntent : MviIntent {
    data object LoadNotifications : TimelineIntent
    data class ReplyToNotification(
        val notificationId: Int,
        val message: String
    ) : TimelineIntent
    
    data class DeleteNotification(val notificationId: Int) : TimelineIntent
    
    data class FilterByApp(val appType: NotificationData.AppType?) : TimelineIntent
    
    data object RefreshNotifications : TimelineIntent
    
    data class SearchNotifications(val query: String) : TimelineIntent
    
    data class RequestReplyPermission(val notificationId: Int) : TimelineIntent

    data object ClearAllNotifications : TimelineIntent
}

/**
 * Timeline screen view state
 */
data class TimelineViewState(
    val notifications: List<NotificationData> = emptyList(),
    val isLoading: Boolean = false,
    val filterAppType: NotificationData.AppType? = null,
    val searchQuery: String = "",
    val error: String? = null,
    val hasNotificationListenerPermission: Boolean = false,
    val selectedNotificationId: Int? = null
) : MviViewState {
    val filteredNotifications: List<NotificationData>
        get() {
            var filtered = notifications
            
            // Filter by app type
            if (filterAppType != null) {
                filtered = filtered.filter { it.appType == filterAppType }
            }
            
            // Filter by search query
            if (searchQuery.isNotEmpty()) {
                filtered = filtered.filter { notification ->
                    notification.title.contains(searchQuery, ignoreCase = true) ||
                    notification.message.contains(searchQuery, ignoreCase = true) ||
                    notification.bigText?.contains(searchQuery, ignoreCase = true) == true
                }
            }
            
            return filtered.sortedByDescending { it.timestamp }
        }
}

/**
 * Timeline screen view effects (one-time events)
 */
sealed interface TimelineEffect : MviViewEffect {
    data class ShowMessage(val message: String) : TimelineEffect
    
    data class ShowError(val error: String) : TimelineEffect
    
    data object ReplySent : TimelineEffect
    
    data object RequestNotificationListenerPermission : TimelineEffect
    
    data object NavigateToSettings : TimelineEffect
    
    data class ShowReplyDialog(val notificationId: Int, val appName: String) : TimelineEffect
}
