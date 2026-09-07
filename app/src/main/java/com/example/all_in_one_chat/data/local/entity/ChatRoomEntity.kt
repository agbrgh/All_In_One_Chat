package com.example.all_in_one_chat.data.local.entity

import androidx.room.Entity
import com.example.all_in_one_chat.domain.model.ChatServiceType

@Entity(
    tableName = "chat_rooms",
    primaryKeys = ["serviceType", "roomId"]
)
data class ChatRoomEntity(
    val serviceType: ChatServiceType,
    val roomId: String,
    val roomName: String,
    val lastMessageContent: String? = null,
    val lastMessageTimestamp: Long? = null,
    val unreadCount: Int = 0,
    val iconUrl: String? = null,
    val isArchived: Boolean = false,
    val isMuted: Boolean = false
)
