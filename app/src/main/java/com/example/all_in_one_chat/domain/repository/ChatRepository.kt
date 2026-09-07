package com.example.all_in_one_chat.domain.repository

import com.example.all_in_one_chat.domain.model.ChatMessage
import com.example.all_in_one_chat.domain.model.ChatRoom
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.model.ServiceStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for chat-related data operations.
 * Acts as a single source of truth by coordinating local database and remote services.
 */
interface ChatRepository {
    /**
     * Get all chat rooms from all services, observed as a Flow from local storage.
     */
    fun getChatRooms(): Flow<List<ChatRoom>>

    /**
     * Get statuses of all available services.
     */
    fun getServiceStatuses(): Flow<List<ServiceStatus>>

    /**
     * Set the active status of a service.
     */
    suspend fun setServiceActive(serviceType: ChatServiceType, isActive: Boolean): Result<Unit>

    /**
     * Refresh chat rooms from all connected remote services and update local storage.
     */
    suspend fun refreshChatRooms(): Result<Unit>

    /**
     * Get a specific chat room by its ID and service type.
     */
    fun getChatRoom(serviceType: ChatServiceType, roomId: String): Flow<ChatRoom?>

    /**
     * Get messages for a specific chat room, observed as a Flow from local storage.
     */
    fun getMessages(serviceType: ChatServiceType, roomId: String): Flow<List<ChatMessage>>

    /**
     * Refresh messages for a specific chat room from remote service and update local storage.
     */
    suspend fun refreshMessages(serviceType: ChatServiceType, roomId: String): Result<Unit>

    /**
     * Send a message to a specific chat room, optionally as a reply.
     */
    suspend fun sendMessage(
        serviceType: ChatServiceType,
        roomId: String,
        content: String,
        replyToMessageId: String? = null
    ): Result<Unit>

    /**
     * Delete a message.
     */
    suspend fun deleteMessage(serviceType: ChatServiceType, roomId: String, messageId: String): Result<Unit>

    /**
     * Forward a message to another room.
     */
    suspend fun forwardMessage(
        message: ChatMessage,
        targetServiceType: ChatServiceType,
        targetRoomId: String
    ): Result<Unit>

    /**
     * Observe and handle new incoming messages from all services.
     */
    fun startObservingMessages()

    /**
     * Mark all messages in a specific chat room as read.
     */
    suspend fun markAsRead(serviceType: ChatServiceType, roomId: String): Result<Unit>

    /**
     * Delete a chat room from local storage and optionally from the service.
     */
    suspend fun deleteChatRoom(serviceType: ChatServiceType, roomId: String): Result<Unit>

    /**
     * Archive or unarchive a chat room.
     */
    suspend fun setChatRoomArchived(serviceType: ChatServiceType, roomId: String, isArchived: Boolean): Result<Unit>

    /**
     * Mute or unmute a chat room.
     */
    suspend fun setChatRoomMuted(serviceType: ChatServiceType, roomId: String, isMuted: Boolean): Result<Unit>

    /**
     * Download media content for a message.
     */
    suspend fun downloadMedia(serviceType: ChatServiceType, messageId: String, fileId: Int): Result<Unit>

    /**
     * Find and trigger download for all pending video messages.
     * Called when app comes to foreground.
     */
    suspend fun downloadPendingMedia(): Result<Unit>

    /**
     * Search messages across all rooms and services.
     */
    fun searchMessages(query: String): Flow<List<ChatMessage>>

    /**
     * Get all messages that contain media (images, videos, etc.).
     */
    fun getMediaMessages(): Flow<List<ChatMessage>>

    /**
     * Get a specific message by its ID and service type.
     */
    fun getMessage(serviceType: ChatServiceType, messageId: String): Flow<ChatMessage?>
}
