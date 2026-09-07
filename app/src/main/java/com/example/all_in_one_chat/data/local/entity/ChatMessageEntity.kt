package com.example.all_in_one_chat.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.model.MediaType

@Entity(
    tableName = "chat_messages",
    primaryKeys = ["serviceType", "roomId", "messageId"],
    foreignKeys = [
        ForeignKey(
            entity = ChatRoomEntity::class,
            parentColumns = ["serviceType", "roomId"],
            childColumns = ["serviceType", "roomId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["serviceType", "roomId"])]
)
data class ChatMessageEntity(
    val serviceType: ChatServiceType,
    val messageId: String,
    val roomId: String,
    val senderId: String,
    val senderName: String,
    val senderImageUrl: String? = null,
    val content: String,
    val timestamp: Long,
    val mediaUrl: String? = null,
    val mediaLocalPath: String? = null,
    val mediaType: MediaType? = null,
    val mediaFileId: Int? = null,
    val isFromMe: Boolean,
    val replyToMessageId: String? = null,
    val replyToMessageSender: String? = null,
    val replyToMessageContent: String? = null
)
