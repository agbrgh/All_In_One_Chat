package com.example.all_in_one_chat.data.repository.whatsapp

import android.service.notification.StatusBarNotification
import com.example.all_in_one_chat.data.local.prefs.SecurePreferences
import com.example.all_in_one_chat.domain.model.ChatMessage
import com.example.all_in_one_chat.domain.model.ChatRoom
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.model.NotificationData
import com.example.all_in_one_chat.domain.model.User
import com.example.all_in_one_chat.domain.repository.ChatServiceAdapter
import com.example.all_in_one_chat.service.NotificationBridge
import com.example.all_in_one_chat.service.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class WhatsAppServiceAdapter @Inject constructor(
    private val notificationBridge: NotificationBridge,
    private val notificationHelper: NotificationHelper,
    private val securePreferences: SecurePreferences
) : ChatServiceAdapter {
    override val serviceType: ChatServiceType = ChatServiceType.WHATSAPP

    override val isConnected: Flow<Boolean> = flow {
        while (true) {
            val hasPermission = notificationHelper.isNotificationListenerEnabled()
            emit(hasPermission)
            delay(5000.milliseconds) // Poll every 5 seconds
        }
    }.distinctUntilChanged()

    private val _newMessages = MutableSharedFlow<ChatMessage>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Map to store the latest notification for each chat room to enable replies
    private val lastNotificationMap = mutableMapOf<String, StatusBarNotification>()

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        scope.launch {
            notificationBridge.notificationFlow
                .filter { it.appType == NotificationData.AppType.WHATSAPP }
                .collect { data ->
                    val roomId = data.title // Using title as roomId for WhatsApp (sender name or group name)
                    data.sbn?.let {
                        lastNotificationMap[roomId] = it
                    }

                    val message = ChatMessage(
                        messageId = data.id.toString(),
                        roomId = roomId,
                        sender = User(
                            id = data.title,
                            serviceType = serviceType,
                            serviceUserId = data.title,
                            userName = data.title
                        ),
                        content = data.message,
                        timestamp = data.timestamp,
                        serviceType = serviceType,
                        isFromMe = false
                    )
                    _newMessages.emit(message)
                }
        }
    }

    override suspend fun login(): Result<Unit> {
        // Notification-based service doesn't have a traditional login.
        // Success if permission is granted.
        return if (notificationHelper.isNotificationListenerEnabled()) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Notification permission not granted"))
        }
    }

    override suspend fun getChatRooms(): Result<List<ChatRoom>> {
        // In notification-based integration, we can't fetch all rooms.
        // We only know about rooms we've received notifications from.
        val rooms = lastNotificationMap.keys.map { roomId ->
            ChatRoom(
                roomId = roomId,
                serviceType = serviceType,
                roomName = roomId,
                lastMessage = null, // Could be filled if we store more history
                unreadCount = 0
            )
        }
        return Result.success(rooms)
    }

    override suspend fun getMessages(roomId: String): Result<List<ChatMessage>> {
        // We don't have message history in this simple notification-based implementation
        return Result.success(emptyList())
    }

    override suspend fun sendMessage(
        roomId: String,
        content: String,
        replyToMessageId: String?
    ): Result<Unit> {
        val sbn = lastNotificationMap[roomId]
            ?: return Result.failure(Exception("No active notification found for room $roomId to reply to"))

        val success = notificationHelper.sendReply(sbn, content)
        return if (success) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Failed to send reply via notification"))
        }
    }

    override suspend fun deleteMessage(
        roomId: String,
        messageId: String
    ): Result<Unit> {
        return Result.failure(Exception("Message deletion is not supported for WhatsApp via notifications"))
    }

    override suspend fun forwardMessage(
        fromRoomId: String,
        messageId: String,
        toRoomId: String
    ): Result<Unit> {
        return Result.failure(Exception("Message forwarding is not supported for WhatsApp via notifications"))
    }

    override fun observeNewMessages(): Flow<ChatMessage> = _newMessages.asSharedFlow()

    override suspend fun markAsRead(roomId: String): Result<Unit> {
        // Notification-based mark as read could involve cancelling the notification
        lastNotificationMap[roomId]?.let {
            notificationHelper.cancelNotification(it.id)
        }
        return Result.success(Unit)
    }

    override suspend fun downloadMedia(messageId: String, fileId: Int): Result<Unit> {
        return Result.failure(Exception("Media download not supported via notifications"))
    }

    override fun observeFileUpdates(): Flow<Pair<Int, String>> = kotlinx.coroutines.flow.flow { }
}
