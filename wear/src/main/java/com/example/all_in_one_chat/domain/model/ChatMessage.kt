package com.example.all_in_one_chat.domain.model

data class ChatMessage(
    val messageId: String,
    val roomId: String,
    val sender: User,
    val content: String,
    val timestamp: Long,
    val serviceType: ChatServiceType,
    val mediaUrl: String? = null,
    val mediaLocalPath: String? = null,
    val mediaType: MediaType? = null,
    val mediaFileId: Int? = null,
    val isFromMe: Boolean,
    val replyToMessageId: String? = null,
    val replyToMessageSender: String? = null,
    val replyToMessageContent: String? = null
)

enum class MediaType {
    IMAGE,
    VIDEO,
    AUDIO,
    FILE
}
