package com.example.all_in_one_chat.data.repository

import android.app.RemoteInput
import android.content.Context
import android.util.Log
import com.example.all_in_one_chat.data.remote.FirestoreNotificationService
import com.example.all_in_one_chat.domain.model.NotificationData
import com.example.all_in_one_chat.domain.repository.NotificationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementation of NotificationRepository
 * Handles notification data operations with Firestore cloud sync
 * Features:
 * - Local memory cache for fast access
 * - Firestore cloud synchronization
 * - Real-time stream updates
 * - RemoteInput reply sending
 * - Offline support
 */
class NotificationRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val firestoreService: FirestoreNotificationService
) : NotificationRepository {
    companion object {
        private const val TAG = "NotificationRepoImpl"
    }

    // Local in-memory cache for fast access
    private val localNotifications = mutableMapOf<Int, NotificationData>()

    /**
     * Get all notifications - returns from local cache initially,
     * then syncs with Firestore for most up-to-date data
     */
    override suspend fun getAllNotifications(): List<NotificationData> = withContext(Dispatchers.IO) {
        try {
            // Try to fetch from Firestore first (cloud source of truth)
            val firestoreNotifications = firestoreService.getAllNotifications()

            // Update local cache
            firestoreNotifications.forEach { notification ->
                localNotifications[notification.id] = notification
            }

            firestoreNotifications.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching from Firestore, falling back to local cache", e)
            // Fallback to local cache if Firestore fails
            localNotifications.values.sortedByDescending { it.timestamp }
        }
    }

    /**
     * Get real-time stream of notifications from Firestore
     */
    fun getNotificationsStream(): Flow<List<NotificationData>> {
        return firestoreService.getNotificationsStream().map { notifications ->
            // Update local cache with streamed data
            notifications.forEach { notification ->
                localNotifications[notification.id] = notification
            }
            notifications
        }
    }

    /**
     * Get notifications by app type from Firestore
     */
    suspend fun getNotificationsByAppType(appType: NotificationData.AppType): List<NotificationData> {
        return withContext(Dispatchers.IO) {
            try {
                firestoreService.getNotificationsByAppType(appType)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching notifications by app type", e)
                localNotifications.values
                    .filter { it.appType == appType }
                    .sortedByDescending { it.timestamp }
            }
        }
    }

    /**
     * Search notifications by keyword
     */
    suspend fun searchNotifications(keyword: String): List<NotificationData> {
        return withContext(Dispatchers.IO) {
            try {
                firestoreService.searchNotifications(keyword)
            } catch (e: Exception) {
                Log.e(TAG, "Error searching notifications", e)
                // Fallback to local search
                localNotifications.values
                    .filter { notification ->
                        notification.title.contains(keyword, ignoreCase = true) ||
                        notification.message.contains(keyword, ignoreCase = true) ||
                        notification.bigText?.contains(keyword, ignoreCase = true) == true
                    }
                    .sortedByDescending { it.timestamp }
            }
        }
    }

    /**
     * Send a reply to a notification via RemoteInput
     */
    override suspend fun sendReply(notificationId: Int, message: String): Unit = withContext(Dispatchers.IO) {
        val notification = localNotifications[notificationId] ?: return@withContext

        notification.sbn?.let { sbn ->
            try {
                // Find the reply action in the notification
                val notificationObj = sbn.notification
                val actions = notificationObj.actions ?: return@let

                // Look for a reply action
                val replyAction = actions.find { action ->
                    action.remoteInputs?.any { it.resultKey.contains("reply", ignoreCase = true) } == true
                } ?: return@let

                // Prepare the remote input
                val remoteInputs = replyAction.remoteInputs ?: return@let
                val replyInput = remoteInputs.find { it.resultKey.contains("reply", ignoreCase = true) } 
                    ?: return@let

                // Create bundle with reply message
                val results = android.os.Bundle().apply {
                    putString(replyInput.resultKey, message)
                }

                // Send the reply intent
                try {
                    val resultIntent = android.content.Intent().apply {
                        RemoteInput.addResultsToIntent(remoteInputs, this, results)
                    }
                    replyAction.actionIntent?.send(context, 0, resultIntent)
                    Log.d(TAG, "Reply sent successfully for notification $notificationId")
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending reply intent", e)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing reply", e)
            }
        }
    }

    /**
     * Delete a notification from both local cache and Firestore
     */
    override suspend fun deleteNotification(notificationId: Int) = withContext(Dispatchers.IO) {
        val deletedNotification = localNotifications.remove(notificationId)

        // Also delete from Firestore
        try {
            firestoreService.deleteNotification(notificationId)
            Log.d(TAG, "Notification deleted from both local cache and Firestore: $notificationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting notification from Firestore", e)
        }

        deletedNotification
    }

    /**
     * Add a new notification to local cache and Firestore
     */
    override suspend fun addNotification(notification: NotificationData): Unit = withContext(Dispatchers.IO) {
        // Update local cache immediately for UI responsiveness
        localNotifications[notification.id] = notification

        // Sync to Firestore in background
        try {
            firestoreService.saveNotification(notification)
            Log.d(TAG, "Notification saved to Firestore: ${notification.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving notification to Firestore (local copy retained)", e)
        }
    }

    /**
     * Clear all notifications
     */
    suspend fun clearAllNotifications(): Boolean {
        return withContext(Dispatchers.IO) {
            localNotifications.clear()
            try {
                firestoreService.clearAllNotifications()
                Log.d(TAG, "All notifications cleared")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing notifications from Firestore", e)
                false
            }
        }
    }

    /**
     * Get notification count
     */
    suspend fun getNotificationCount(): Long {
        return withContext(Dispatchers.IO) {
            try {
                firestoreService.getNotificationCount()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting notification count", e)
                localNotifications.size.toLong()
            }
        }
    }
}
