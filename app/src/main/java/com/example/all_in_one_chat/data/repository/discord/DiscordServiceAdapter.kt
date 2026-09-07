package com.example.all_in_one_chat.data.repository.discord

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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class DiscordServiceAdapter @Inject constructor(
    private val notificationBridge: NotificationBridge,
    private val notificationHelper: NotificationHelper,
    private val securePreferences: SecurePreferences
) : ChatServiceAdapter {
    override val serviceType: ChatServiceType = ChatServiceType.DISCORD

    override val isConnected: Flow<Boolean> = combine(
        flow {
            while (true) {
                emit(notificationHelper.isNotificationListenerEnabled())
                delay(5000.milliseconds)
            }
        },
        securePreferences.getStringFlow(SecurePreferences.KEY_DISCORD_TOKEN).map { it != null }
    ) { notificationEnabled, apiConnected ->
        notificationEnabled || apiConnected
    }.distinctUntilChanged()

    private val _newMessages = MutableSharedFlow<ChatMessage>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val lastNotificationMap = mutableMapOf<String, StatusBarNotification>()

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        scope.launch {
            notificationBridge.notificationFlow
                .filter { it.appType == NotificationData.AppType.DISCORD }
                .collect { data ->
                    val roomId = data.title
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
        // Now handled by DiscordAuthViewModel via browser
        return Result.success(Unit)
    }

    suspend fun exchangeCodeForToken(authCode: String): Result<Unit> {
        // 실제 API 연동 시에는 여기서 Retrofit 등으로 토큰을 받아와야 함.
        // 현재는 전달받은 authCode를 토큰으로 간주하고 저장 (목업)
        securePreferences.saveString(SecurePreferences.KEY_DISCORD_TOKEN, "mock_token_$authCode")
        return Result.success(Unit)
    }

    override suspend fun getChatRooms(): Result<List<ChatRoom>> {
        val rooms = lastNotificationMap.keys.map { roomId ->
            ChatRoom(
                roomId = roomId,
                serviceType = serviceType,
                roomName = roomId,
                lastMessage = null,
                unreadCount = 0
            )
        }
        return Result.success(rooms)
    }

    override suspend fun getMessages(roomId: String): Result<List<ChatMessage>> {
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
        return Result.failure(Exception("Message deletion is not supported for Discord via notifications"))
    }

    override suspend fun forwardMessage(
        fromRoomId: String,
        messageId: String,
        toRoomId: String
    ): Result<Unit> {
        return Result.failure(Exception("Message forwarding is not supported for Discord via notifications"))
    }

    override fun observeNewMessages(): Flow<ChatMessage> = _newMessages.asSharedFlow()

    override suspend fun markAsRead(roomId: String): Result<Unit> {
        lastNotificationMap[roomId]?.let {
            notificationHelper.cancelNotification(it.id)
        }
        return Result.success(Unit)
    }

    override suspend fun downloadMedia(messageId: String, fileId: Int): Result<Unit> {
        return Result.failure(Exception("Media download not supported via notifications"))
    }

    override fun observeFileUpdates(): Flow<Pair<Int, String>> = flow { }
}
