package com.example.all_in_one_chat.data.model

import com.example.all_in_one_chat.domain.model.NotificationData
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Firestore document model for persisting notifications in the cloud
 * Maps directly to Firestore collection "notifications"
 */
data class FirestoreNotification(
    @DocumentId
    val documentId: String = "",
    val userId: String = "",  // User UID from Firebase Auth
    val notificationId: Int = 0,
    val packageName: String = "",
    val appType: String = "",  // Enum name as string
    val title: String = "",
    val message: String = "",
    val subText: String? = null,
    val bigText: String? = null,
    val timestamp: Long = 0,
    val isRemovable: Boolean = true,
    @ServerTimestamp
    val serverTimestamp: Date? = null,
    val actions: List<ActionData> = emptyList(),
    val isSynced: Boolean = true  // Track sync status for offline support
) {
    /**
     * Convert Firestore document to NotificationData domain model
     */
    fun toDomainModel(): NotificationData {
        return NotificationData(
            id = notificationId,
            packageName = packageName,
            appType = try {
                NotificationData.AppType.valueOf(appType)
            } catch (e: IllegalArgumentException) {
                NotificationData.AppType.OTHER
            },
            title = title,
            message = message,
            subText = subText,
            bigText = bigText,
            timestamp = timestamp,
            sbn = null,  // StatusBarNotification not persisted
            actions = actions.map { NotificationData.Action(it.title, it.semanticAction) },
            isRemovable = isRemovable
        )
    }

    /**
     * Data class for notification actions
     */
    data class ActionData(
        val title: String = "",
        val semanticAction: Int = -1
    )

    companion object {
        /**
         * Convert NotificationData domain model to Firestore document
         */
        fun fromDomainModel(
            notification: NotificationData,
            userId: String
        ): FirestoreNotification {
            return FirestoreNotification(
                userId = userId,
                notificationId = notification.id,
                packageName = notification.packageName,
                appType = notification.appType.name,
                title = notification.title,
                message = notification.message,
                subText = notification.subText,
                bigText = notification.bigText,
                timestamp = notification.timestamp,
                isRemovable = notification.isRemovable,
                actions = notification.actions.map {
                    ActionData(it.title, it.semanticAction)
                }
            )
        }
    }
}

