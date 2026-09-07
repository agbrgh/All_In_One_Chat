package com.example.all_in_one_chat.service

import com.example.all_in_one_chat.domain.model.NotificationData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bridge between AllInOneChatNotificationListenerService and ViewModels.
 * Since a Service is a Context, injecting it directly into a ViewModel causes a memory leak.
 * This singleton bridge allows sharing the notification stream safely.
 */
@Singleton
class NotificationBridge @Inject constructor() {
    
    private val _notificationFlow = MutableSharedFlow<NotificationData>(
        replay = 0,
        extraBufferCapacity = 100 // Increased capacity to prevent loss during bursts
    )
    val notificationFlow: SharedFlow<NotificationData> = _notificationFlow.asSharedFlow()

    /**
     * Publish a new notification to all observers
     */
    suspend fun publishNotification(notification: NotificationData) {
        _notificationFlow.emit(notification)
    }
}
