package com.example.all_in_one_chat.domain.usecase

import com.example.all_in_one_chat.data.repository.NotificationRepositoryImpl
import com.example.all_in_one_chat.domain.model.NotificationData
import com.example.all_in_one_chat.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get all notifications
 */
class GetNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(): List<NotificationData> {
        return repository.getAllNotifications()
    }
}

/**
 * Use case to send a reply to a notification
 */
class SendReplyUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(notificationId: Int, message: String) {
        repository.sendReply(notificationId, message)
    }
}

/**
 * Use case to delete a notification
 */
class DeleteNotificationUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(notificationId: Int) {
        repository.deleteNotification(notificationId)
    }
}

/**
 * Use case to add a new notification
 */
class AddNotificationUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(notification: NotificationData) {
        repository.addNotification(notification)
    }
}

/**
 * Use case to get real-time stream of notifications from Firestore
 */
class GetNotificationsStreamUseCase @Inject constructor(
    private val repository: NotificationRepositoryImpl
) {
    operator fun invoke(): Flow<List<NotificationData>> {
        return repository.getNotificationsStream()
    }
}

/**
 * Use case to get notifications by app type
 */
class GetNotificationsByAppTypeUseCase @Inject constructor(
    private val repository: NotificationRepositoryImpl
) {
    suspend operator fun invoke(appType: NotificationData.AppType): List<NotificationData> {
        return repository.getNotificationsByAppType(appType)
    }
}

/**
 * Use case to search notifications by keyword
 */
class SearchNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepositoryImpl
) {
    suspend operator fun invoke(keyword: String): List<NotificationData> {
        return repository.searchNotifications(keyword)
    }
}

/**
 * Use case to clear all notifications
 */
class ClearAllNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepositoryImpl
) {
    suspend operator fun invoke(): Boolean {
        return repository.clearAllNotifications()
    }
}

/**
 * Use case to get notification count
 */
class GetNotificationCountUseCase @Inject constructor(
    private val repository: NotificationRepositoryImpl
) {
    suspend operator fun invoke(): Long {
        return repository.getNotificationCount()
    }
}

