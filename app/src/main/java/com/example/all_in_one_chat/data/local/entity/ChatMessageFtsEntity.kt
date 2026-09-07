package com.example.all_in_one_chat.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = ChatMessageEntity::class)
@Entity(tableName = "chat_messages_fts")
data class ChatMessageFtsEntity(
    val content: String,
    val senderName: String
)
