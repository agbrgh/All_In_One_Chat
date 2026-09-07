package com.example.all_in_one_chat.domain.repository

import com.example.all_in_one_chat.domain.model.NotificationData
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for notification operations
 * Supports cloud sync, real-time streaming, and local caching
 */
interface NotificationRepository {
    
    /**
     * Get all stored notifications
     * Returns from cloud (Firestore) with fallback to local cache
     */
    suspend fun getAllNotifications(): List<NotificationData>
    
    /**
     * Send a reply to a notification using RemoteInput
     */
    suspend fun sendReply(notificationId: Int, message: String)
    
    /**
     * Delete a notification from both local and cloud storage
     */
    suspend fun deleteNotification(notificationId: Int): NotificationData?

    /**
     * Add a new notification and sync to cloud
     */
    suspend fun addNotification(notification: NotificationData)
}
