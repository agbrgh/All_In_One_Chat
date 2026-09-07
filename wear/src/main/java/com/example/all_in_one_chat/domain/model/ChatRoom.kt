package com.example.all_in_one_chat.domain.model

data class ChatRoom(
    val roomId: String,
    val serviceType: ChatServiceType,
    val roomName: String,
    val lastMessage: ChatMessage? = null,
    val unreadCount: Int = 0,
    val iconUrl: String? = null,
    val participants: List<User> = emptyList()
)
