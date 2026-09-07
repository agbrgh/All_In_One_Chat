package com.example.all_in_one_chat.data.remote

import android.util.Log
import com.example.all_in_one_chat.data.model.FirestoreNotification
import com.example.all_in_one_chat.domain.model.NotificationData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing Firestore operations related to notifications
 * Handles cloud storage, retrieval, and real-time streaming of notifications
 */
@Singleton
class FirestoreNotificationService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    companion object {
        private const val TAG = "FirestoreNotificationSvc"
        private const val NOTIFICATIONS_COLLECTION = "notifications"
        private const val USERS_COLLECTION = "users"
        private const val MAX_LOCAL_STORAGE = 1000  // Max notifications to keep locally
    }

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    /**
     * Save a single notification to Firestore
     */
    suspend fun saveNotification(notification: NotificationData): Boolean {
        return try {
            val userId = currentUserId ?: run {
                Log.w(TAG, "Cannot save notification: User not authenticated")
                return false
            }

            val firestoreNotification = FirestoreNotification.fromDomainModel(notification, userId)

            firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(NOTIFICATIONS_COLLECTION)
                .document(notification.id.toString())
                .set(firestoreNotification)
                .await()

            Log.d(TAG, "Notification saved successfully: ${notification.id}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving notification: ${e.message}", e)
            false
        }
    }

    /**
     * Save multiple notifications in batch
     */
    suspend fun saveNotificationsBatch(notifications: List<NotificationData>): Boolean {
        return try {
            val userId = currentUserId ?: run {
                Log.w(TAG, "Cannot save notifications: User not authenticated")
                return false
            }

            val batch = firestore.batch()

            notifications.forEach { notification ->
                val firestoreNotification = FirestoreNotification.fromDomainModel(notification, userId)
                val docRef = firestore
                    .collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(NOTIFICATIONS_COLLECTION)
                    .document(notification.id.toString())

                batch.set(docRef, firestoreNotification)
            }

            batch.commit().await()
            Log.d(TAG, "Batch saved ${notifications.size} notifications")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving batch notifications: ${e.message}", e)
            false
        }
    }

    /**
     * Get all notifications for current user
     */
    suspend fun getAllNotifications(): List<NotificationData> {
        return try {
            val userId = currentUserId ?: run {
                Log.w(TAG, "Cannot get notifications: User not authenticated")
                return emptyList()
            }

            val querySnapshot = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(NOTIFICATIONS_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(MAX_LOCAL_STORAGE.toLong())
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(FirestoreNotification::class.java)?.toDomainModel()
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting document to NotificationData: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting notifications: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get notifications with pagination
     */
    suspend fun getNotificationsPaginated(
        pageSize: Int = 20,
        lastTimestamp: Long? = null
    ): List<NotificationData> {
        return try {
            val userId = currentUserId ?: return emptyList()

            var query: Query = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(NOTIFICATIONS_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())

            lastTimestamp?.let { timestamp ->
                query = query.whereLessThan("timestamp", timestamp)
            }

            val querySnapshot = query.get().await()

            querySnapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(FirestoreNotification::class.java)?.toDomainModel()
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting document: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting paginated notifications: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get notifications by app type
     */
    suspend fun getNotificationsByAppType(appType: NotificationData.AppType): List<NotificationData> {
        return try {
            val userId = currentUserId ?: return emptyList()

            val querySnapshot = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(NOTIFICATIONS_COLLECTION)
                .whereEqualTo("appType", appType.name)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(FirestoreNotification::class.java)?.toDomainModel()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting notifications by app type: ${e.message}", e)
            emptyList()
        }
    }

    private val notificationsFlow = MutableSharedFlow<List<NotificationData>>(replay = 1)

    /**
     * Real-time stream of notifications
     * Automatically syncs with Firestore whenever data changes
     */
    fun getNotificationsStream(): Flow<List<NotificationData>> {
        setupNotificationsListener()
        return notificationsFlow.asSharedFlow()
    }

    /**
     * Setup real-time listener for notifications
     * This is called lazily when the stream is first accessed
     */
    private fun setupNotificationsListener() {
        val userId = currentUserId ?: run {
            Log.w(TAG, "Cannot setup listener: User not authenticated")
            return
        }

        try {
            firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(NOTIFICATIONS_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(MAX_LOCAL_STORAGE.toLong())
                .addSnapshotListener { querySnapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to notifications: ${error.message}", error)
                        return@addSnapshotListener
                    }

                    val notifications = querySnapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toObject(FirestoreNotification::class.java)?.toDomainModel()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error converting snapshot document: ${e.message}")
                            null
                        }
                    } ?: emptyList()

                    // Emit to the shared flow
                    try {
                        notificationsFlow.tryEmit(notifications)
                        Log.d(TAG, "Emitted ${notifications.size} notifications to stream")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error emitting to notifications flow: ${e.message}")
                    }
                }
            Log.d(TAG, "Notifications listener setup successful")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up notifications listener: ${e.message}", e)
        }
    }

    /**
     * Delete a notification from Firestore
     */
    suspend fun deleteNotification(notificationId: Int): Boolean {
        return try {
            val userId = currentUserId ?: return false

            firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(NOTIFICATIONS_COLLECTION)
                .document(notificationId.toString())
                .delete()
                .await()

            Log.d(TAG, "Notification deleted: $notificationId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting notification: ${e.message}", e)
            false
        }
    }

    /**
     * Delete multiple notifications
     */
    suspend fun deleteNotificationsBatch(notificationIds: List<Int>): Boolean {
        return try {
            val userId = currentUserId ?: return false
            val batch = firestore.batch()

            notificationIds.forEach { id ->
                val docRef = firestore
                    .collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(NOTIFICATIONS_COLLECTION)
                    .document(id.toString())

                batch.delete(docRef)
            }

            batch.commit().await()
            Log.d(TAG, "Batch deleted ${notificationIds.size} notifications")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting batch notifications: ${e.message}", e)
            false
        }
    }

    /**
     * Search notifications by keyword
     */
    suspend fun searchNotifications(keyword: String): List<NotificationData> {
        return try {
            val userId = currentUserId ?: return emptyList()

            // Note: Full-text search requires Firestore indexing
            // This is a basic client-side search implementation
            val allNotifications = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(NOTIFICATIONS_COLLECTION)
                .get()
                .await()

            allNotifications.documents
                .mapNotNull { doc -> doc.toObject(FirestoreNotification::class.java) }
                .filter { notification ->
                    notification.title.contains(keyword, ignoreCase = true) ||
                    notification.message.contains(keyword, ignoreCase = true) ||
                    notification.bigText?.contains(keyword, ignoreCase = true) == true
                }
                .sortedByDescending { it.timestamp }
                .map { it.toDomainModel() }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching notifications: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Clear all notifications for current user
     */
    suspend fun clearAllNotifications(): Boolean {
        return try {
            val userId = currentUserId ?: return false

            // Get all document IDs first
            val querySnapshot = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(NOTIFICATIONS_COLLECTION)
                .get()
                .await()

            val batch = firestore.batch()
            querySnapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }

            batch.commit().await()
            Log.d(TAG, "All notifications cleared")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing notifications: ${e.message}", e)
            false
        }
    }

    /**
     * Get notification count for current user
     */
    suspend fun getNotificationCount(): Long {
        return try {
            val userId = currentUserId ?: return 0

            val querySnapshot = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(NOTIFICATIONS_COLLECTION)
                .get()
                .await()

            querySnapshot.size().toLong()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting notification count: ${e.message}", e)
            0
        }
    }
}









