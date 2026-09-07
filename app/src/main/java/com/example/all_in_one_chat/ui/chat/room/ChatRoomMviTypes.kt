package com.example.all_in_one_chat.ui.chat.room

import com.example.all_in_one_chat.domain.model.ChatMessage
import com.example.all_in_one_chat.domain.model.ChatRoom
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.mvi.MviIntent
import com.example.all_in_one_chat.mvi.MviViewEffect
import com.example.all_in_one_chat.mvi.MviViewState

data class ChatRoomViewState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val roomId: String = "",
    val roomName: String = "Chat",
    val serviceType: ChatServiceType? = null,
    val replyTargetMessage: ChatMessage? = null,
    val allRooms: List<ChatRoom> = emptyList()
) : MviViewState

sealed class ChatRoomIntent : MviIntent {
    data class LoadMessages(val serviceType: ChatServiceType, val roomId: String) : ChatRoomIntent()
    data class SendMessage(val content: String) : ChatRoomIntent()
    data class SetReplyTarget(val message: ChatMessage?) : ChatRoomIntent()
    data class DeleteMessage(val message: ChatMessage) : ChatRoomIntent()
    data class ForwardMessage(val message: ChatMessage, val targetRoomId: String, val targetServiceType: ChatServiceType) : ChatRoomIntent()
}

sealed class ChatRoomEffect : MviViewEffect {
    data class ShowError(val message: String) : ChatRoomEffect()
}
