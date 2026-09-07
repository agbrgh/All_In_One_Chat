package com.example.all_in_one_chat.ui.chat.list

import com.example.all_in_one_chat.domain.model.ChatRoom
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.mvi.MviIntent
import com.example.all_in_one_chat.mvi.MviViewEffect
import com.example.all_in_one_chat.mvi.MviViewState

data class ChatListViewState(
    val rooms: List<ChatRoom> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) : MviViewState

sealed class ChatListIntent : MviIntent {
    object LoadRooms : ChatListIntent()
    object RefreshRooms : ChatListIntent()
    data class RoomClicked(val serviceType: ChatServiceType, val roomId: String) : ChatListIntent()
    data class ArchiveRoom(val serviceType: ChatServiceType, val roomId: String) : ChatListIntent()
    data class DeleteRoom(val serviceType: ChatServiceType, val roomId: String) : ChatListIntent()
    data class ToggleMuteRoom(val serviceType: ChatServiceType, val roomId: String, val isMuted: Boolean) : ChatListIntent()
}

sealed class ChatListEffect : MviViewEffect {
    data class ShowError(val message: String) : ChatListEffect()
    data class NavigateToRoom(val serviceType: ChatServiceType, val roomId: String) : ChatListEffect()
}
