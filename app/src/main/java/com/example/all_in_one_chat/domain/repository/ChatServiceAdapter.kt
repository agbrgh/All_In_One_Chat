package com.example.all_in_one_chat.domain.repository

import com.example.all_in_one_chat.domain.model.ChatMessage
import com.example.all_in_one_chat.domain.model.ChatRoom
import com.example.all_in_one_chat.domain.model.ChatServiceType
import kotlinx.coroutines.flow.Flow

/**
 * Interface to abstract different chat services (Telegram, Discord, etc.)
 * Provides common operations for message handling and room management.
 */
interface ChatServiceAdapter {
    /**
     * The type of chat service this adapter handles
     */
    val serviceType: ChatServiceType

    /**
     * Whether the user is currently connected/authenticated with the service
     */
    val isConnected: Flow<Boolean>

    /**
     * Authenticate with the chat service
     * @return Result indicating success or failure of the login process
     */
    suspend fun login(): Result<Unit>

    /**
     * Get the list of chat rooms from the service
     * @return Result containing list of ChatRoom objects
     */
    suspend fun getChatRooms(): Result<List<ChatRoom>>

    /**
     * Get message history for a specific room
     * @param roomId Unique identifier for the chat room
     * @return Result containing list of ChatMessage objects
     */
    suspend fun getMessages(roomId: String): Result<List<ChatMessage>>

    /**
     * Send a message to a specific room, optionally as a reply
     * @param roomId Unique identifier for the chat room
     * @param content Text content of the message
     * @param replyToMessageId Optional ID of the message being replied to
     * @return Result indicating success or failure of the message delivery
     */
    suspend fun sendMessage(roomId: String, content: String, replyToMessageId: String? = null): Result<Unit>

    /**
     * Delete a message from the service
     */
    suspend fun deleteMessage(roomId: String, messageId: String): Result<Unit>

    /**
     * Forward a message to another room within the same service
     */
    suspend fun forwardMessage(fromRoomId: String, messageId: String, toRoomId: String): Result<Unit>

    /**
     * Observe new messages in real-time as a Flow
     * @return Flow that emits new ChatMessage objects as they arrive
     */
    fun observeNewMessages(): Flow<ChatMessage>

    /**
     * Mark all messages in a specific room as read
     * @param roomId Unique identifier for the chat room
     * @return Result indicating success or failure of the operation
     */
    suspend fun markAsRead(roomId: String): Result<Unit>

    /**
     * Download media content for a specific message
     * @param messageId Unique identifier for the message
     * @param fileId Service-specific file identifier
     * @return Result indicating success or failure of the operation
     */
    suspend fun downloadMedia(messageId: String, fileId: Int): Result<Unit>

    /**
     * Observe file download updates (fileId to localPath)
     */
    fun observeFileUpdates(): Flow<Pair<Int, String>>
}
