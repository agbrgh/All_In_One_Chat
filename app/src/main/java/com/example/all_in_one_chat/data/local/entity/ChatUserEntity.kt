package com.example.all_in_one_chat.data.local.entity

import androidx.room.Entity
import com.example.all_in_one_chat.domain.model.ChatServiceType

@Entity(
    tableName = "chat_users",
    primaryKeys = ["serviceType", "userId"]
)
data class ChatUserEntity(
    val serviceType: ChatServiceType,
    val userId: String,
    val name: String,
    val imageUrl: String? = null,
    val lastSeenTimestamp: Long? = null,
    val isMe: Boolean = false
)
