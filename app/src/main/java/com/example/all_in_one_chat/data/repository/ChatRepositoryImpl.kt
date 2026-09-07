package com.example.all_in_one_chat.data.repository

import android.content.Context
import android.content.Intent
import com.example.all_in_one_chat.data.local.dao.ChatRoomDao
import com.example.all_in_one_chat.data.local.dao.MessageDao
import com.example.all_in_one_chat.data.local.database.AppDatabase
import com.example.all_in_one_chat.data.local.prefs.SecurePreferences
import com.example.all_in_one_chat.data.mapper.toDomain
import com.example.all_in_one_chat.data.mapper.toEntity
import com.example.all_in_one_chat.di.ApplicationScope
import com.example.all_in_one_chat.domain.model.ChatMessage
import com.example.all_in_one_chat.domain.model.ChatRoom
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.model.ServiceStatus
import com.example.all_in_one_chat.domain.repository.ChatRepository
import com.example.all_in_one_chat.domain.repository.ChatServiceAdapter
import com.example.all_in_one_chat.service.ShortcutHelper
import com.example.all_in_one_chat.service.analytics.AnalyticsHelper
import com.example.all_in_one_chat.service.sync.WearSyncManager
import com.example.all_in_one_chat.service.notification.NotificationHelper
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val chatRoomDao: ChatRoomDao,
    private val messageDao: MessageDao,
    private val serviceAdapters: Set<@JvmSuppressWildcards ChatServiceAdapter>,
    private val securePreferences: SecurePreferences,
    private val notificationHelper: NotificationHelper,
    private val analyticsHelper: AnalyticsHelper,
    private val shortcutHelper: ShortcutHelper,
    private val wearSyncManager: WearSyncManager,
    @ApplicationContext private val context: Context,
    @ApplicationScope private val externalScope: CoroutineScope
) : ChatRepository {

    private var isObserving = false
    private val _serviceUpdateTrigger = MutableStateFlow(System.currentTimeMillis())

    private fun triggerWidgetUpdate() {
        val intent = Intent("com.example.all_in_one_chat.ACTION_UPDATE_WIDGET").apply {
            setPackage(context.packageName)
        }
        context.sendBroadcast(intent)
        
        // Update shortcuts as well
        externalScope.launch {
            val rooms = chatRoomDao.getAllChatRooms().first().map { it.toDomain() }
            shortcutHelper.updateShortcuts(rooms)
            wearSyncManager.syncChatRooms(rooms)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getChatRooms(): Flow<List<ChatRoom>> {
        return chatRoomDao.getAllChatRooms().flatMapLatest { entities ->
            if (entities.isEmpty()) return@flatMapLatest flowOf(emptyList())
            val flows = entities.map { entity ->
                securePreferences.getBooleanFlow(getServiceActiveKey(entity.serviceType), true)
                    .map { isActive -> if (isActive) entity.toDomain() else null }
            }
            combine(flows) { it.filterNotNull().toList() }
        }
    }

    override fun getServiceStatuses(): Flow<List<ServiceStatus>> {
        val flows = ChatServiceType.entries.map { type ->
            val adapter = serviceAdapters.find { it.serviceType == type }
            val isConnectedFlow = adapter?.isConnected ?: flowOf(false)
            val isActiveFlow = securePreferences.getBooleanFlow(getServiceActiveKey(type), true)
            
            combine(isConnectedFlow, isActiveFlow) { connected, active ->
                ServiceStatus(
                    serviceType = type,
                    isConnected = connected,
                    isActive = active
                )
            }
        }
        return combine(flows) { it.toList() }
    }

    override suspend fun setServiceActive(serviceType: ChatServiceType, isActive: Boolean): Result<Unit> {
        return try {
            securePreferences.saveBoolean(getServiceActiveKey(serviceType), isActive)
            analyticsHelper.logServiceToggled(serviceType.name, isActive)
            _serviceUpdateTrigger.value = System.currentTimeMillis()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getServiceActiveKey(type: ChatServiceType) = "service_active_${type.name}"

    override suspend fun refreshChatRooms(): Result<Unit> {
        return try {
            serviceAdapters.forEach { adapter ->
                if (!securePreferences.getBoolean(getServiceActiveKey(adapter.serviceType), true)) {
                    return@forEach
                }
                val result = adapter.getChatRooms()
                if (result.isSuccess) {
                    val rooms = result.getOrNull() ?: emptyList()
                    chatRoomDao.insertChatRooms(rooms.map { it.toEntity() })
                }
            }
            triggerWidgetUpdate()
            Result.success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Result.failure(e)
        }
    }

    override fun getChatRoom(serviceType: ChatServiceType, roomId: String): Flow<ChatRoom?> {
        return chatRoomDao.getChatRoom(serviceType, roomId).map { entity ->
            entity?.toDomain()
        }
    }

    override fun getMessages(serviceType: ChatServiceType, roomId: String): Flow<List<ChatMessage>> {
        return messageDao.getMessagesForRoom(serviceType, roomId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun refreshMessages(serviceType: ChatServiceType, roomId: String): Result<Unit> {
        val adapter = serviceAdapters.find { it.serviceType == serviceType }
            ?: return Result.failure(Exception("Adapter not found for $serviceType"))

        return try {
            val result = adapter.getMessages(roomId)
            if (result.isSuccess) {
                val messages = result.getOrNull() ?: emptyList()
                database.insertMessagesWithUsers(
                    messages.map { it.sender.toEntity() },
                    messages.map { it.toEntity() }
                )
                Result.success(Unit)
            } else {
                val exception = result.exceptionOrNull() ?: Exception("Unknown error")
                FirebaseCrashlytics.getInstance().recordException(exception)
                Result.failure(exception)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Result.failure(e)
        }
    }

    override suspend fun sendMessage(
        serviceType: ChatServiceType,
        roomId: String,
        content: String,
        replyToMessageId: String?
    ): Result<Unit> {
        val adapter = serviceAdapters.find { it.serviceType == serviceType }
            ?: return Result.failure(Exception("Adapter not found for $serviceType"))

        return try {
            val result = adapter.sendMessage(roomId, content, replyToMessageId)
            if (result.isSuccess) {
                analyticsHelper.logMessageSent(serviceType.name)
                refreshMessages(serviceType, roomId)
                Result.success(Unit)
            } else {
                val exception = result.exceptionOrNull() ?: Exception("Failed to send message")
                FirebaseCrashlytics.getInstance().recordException(exception)
                Result.failure(exception)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Result.failure(e)
        }
    }

    override suspend fun deleteMessage(
        serviceType: ChatServiceType,
        roomId: String,
        messageId: String
    ): Result<Unit> {
        val adapter = serviceAdapters.find { it.serviceType == serviceType }
            ?: return Result.failure(Exception("Adapter not found for $serviceType"))

        return try {
            val result = adapter.deleteMessage(roomId, messageId)
            if (result.isSuccess) {
                messageDao.deleteMessage(serviceType, roomId, messageId)
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to delete message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun forwardMessage(
        message: ChatMessage,
        targetServiceType: ChatServiceType,
        targetRoomId: String
    ): Result<Unit> {
        val sourceAdapter = serviceAdapters.find { it.serviceType == message.serviceType }
            ?: return Result.failure(Exception("Source adapter not found"))
        
        val targetAdapter = serviceAdapters.find { it.serviceType == targetServiceType }
            ?: return Result.failure(Exception("Target adapter not found"))

        return try {
            val result = if (message.serviceType == targetServiceType) {
                // Same service: try native forward if supported
                targetAdapter.forwardMessage(message.roomId, message.messageId, targetRoomId)
            } else {
                // Different service: cross-platform forward (send content as new message)
                val content = message.content.ifEmpty { "[Media]" }
                targetAdapter.sendMessage(targetRoomId, content)
            }
            
            if (result.isSuccess) {
                refreshMessages(targetServiceType, targetRoomId)
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun startObservingMessages() {
        if (isObserving) return
        isObserving = true

        serviceAdapters.forEach { adapter ->
            adapter.observeNewMessages()
                .combine(securePreferences.getBooleanFlow(getServiceActiveKey(adapter.serviceType), true)) { message, isActive ->
                    if (isActive) message else null
                }
                .filterNotNull()
                .onEach { message ->
                    database.insertMessageWithUser(
                        message.sender.toEntity(),
                        message.toEntity()
                    )
                    if (!message.isFromMe) {
                        notificationHelper.showNotification(message)
                    }
                    triggerWidgetUpdate()
                }
                .launchIn(externalScope)

            adapter.observeFileUpdates()
                .onEach { (fileId, localPath) ->
                    messageDao.updateMediaLocalPath(adapter.serviceType, fileId, localPath)
                }
                .launchIn(externalScope)
        }
    }

    override suspend fun markAsRead(serviceType: ChatServiceType, roomId: String): Result<Unit> {
        val adapter = serviceAdapters.find { it.serviceType == serviceType }
            ?: return Result.failure(Exception("Adapter not found for $serviceType"))

        return try {
            val result = adapter.markAsRead(roomId)
            if (result.isSuccess) {
                chatRoomDao.markAsRead(serviceType, roomId)
                Result.success(Unit)
            } else {
                val exception = result.exceptionOrNull() ?: Exception("Failed to mark as read")
                FirebaseCrashlytics.getInstance().recordException(exception)
                Result.failure(exception)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Result.failure(e)
        }
    }

    override suspend fun deleteChatRoom(serviceType: ChatServiceType, roomId: String): Result<Unit> {
        return try {
            chatRoomDao.deleteChatRoom(serviceType, roomId)
            analyticsHelper.logServiceToggled("RoomDeleted", false) // Reuse toggle log or add new
            Result.success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Result.failure(e)
        }
    }

    override suspend fun setChatRoomArchived(
        serviceType: ChatServiceType,
        roomId: String,
        isArchived: Boolean
    ): Result<Unit> {
        return try {
            chatRoomDao.setArchived(serviceType, roomId, isArchived)
            Result.success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Result.failure(e)
        }
    }

    override suspend fun setChatRoomMuted(
        serviceType: ChatServiceType,
        roomId: String,
        isMuted: Boolean
    ): Result<Unit> {
        return try {
            chatRoomDao.setMuted(serviceType, roomId, isMuted)
            Result.success(Unit)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Result.failure(e)
        }
    }

    override suspend fun downloadMedia(
        serviceType: ChatServiceType,
        messageId: String,
        fileId: Int
    ): Result<Unit> {
        val adapter = serviceAdapters.find { it.serviceType == serviceType }
            ?: return Result.failure(Exception("Adapter not found for $serviceType"))

        return try {
            val result = adapter.downloadMedia(messageId, fileId)
            if (result.isSuccess) {
                analyticsHelper.logMediaDownload(serviceType.name) // Using serviceType as a proxy for media source
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun downloadPendingMedia(): Result<Unit> {
        return try {
            val pendingVideos = messageDao.getPendingVideoMessages()
            pendingVideos.forEach { entity ->
                if (entity.mediaFileId != null) {
                    downloadMedia(entity.serviceType, entity.messageId, entity.mediaFileId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun searchMessages(query: String): Flow<List<ChatMessage>> {
        if (query.isBlank()) return flowOf(emptyList())
        return messageDao.searchMessages("*$query*").map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getMediaMessages(): Flow<List<ChatMessage>> {
        return messageDao.getAllMediaMessages().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getMessage(serviceType: ChatServiceType, messageId: String): Flow<ChatMessage?> {
        return messageDao.getMessage(serviceType, messageId).map { it?.toDomain() }
    }
}
