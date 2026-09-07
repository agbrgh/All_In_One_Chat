package com.example.all_in_one_chat.ui.chat.list

import androidx.lifecycle.viewModelScope
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.repository.ChatRepository
import com.example.all_in_one_chat.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : MviViewModel<ChatListIntent, ChatListViewState, ChatListEffect>(ChatListViewState()) {

    init {
        sendIntent(ChatListIntent.LoadRooms)
    }

    override suspend fun handleIntent(intent: ChatListIntent) {
        when (intent) {
            is ChatListIntent.LoadRooms -> loadRooms()
            is ChatListIntent.RefreshRooms -> refreshRooms()
            is ChatListIntent.RoomClicked -> {
                sendEffect(ChatListEffect.NavigateToRoom(intent.serviceType, intent.roomId))
            }
            is ChatListIntent.ArchiveRoom -> archiveRoom(intent.serviceType, intent.roomId)
            is ChatListIntent.DeleteRoom -> deleteRoom(intent.serviceType, intent.roomId)
            is ChatListIntent.ToggleMuteRoom -> toggleMuteRoom(intent.serviceType, intent.roomId, intent.isMuted)
        }
    }

    private fun loadRooms() {
        chatRepository.getChatRooms()
            .onEach { rooms ->
                reduceState { it.copy(rooms = rooms) }
            }
            .launchIn(viewModelScope)
    }

    private fun refreshRooms() {
        viewModelScope.launch {
            reduceState { it.copy(isLoading = true) }
            val result = chatRepository.refreshChatRooms()
            if (result.isFailure) {
                sendEffect(ChatListEffect.ShowError(result.exceptionOrNull()?.message ?: "Unknown error"))
            }
            reduceState { it.copy(isLoading = false) }
        }
    }

    private fun archiveRoom(serviceType: ChatServiceType, roomId: String) {
        viewModelScope.launch {
            chatRepository.setChatRoomArchived(serviceType, roomId, true)
        }
    }

    private fun deleteRoom(serviceType: ChatServiceType, roomId: String) {
        viewModelScope.launch {
            chatRepository.deleteChatRoom(serviceType, roomId)
        }
    }

    private fun toggleMuteRoom(serviceType: ChatServiceType, roomId: String, isMuted: Boolean) {
        viewModelScope.launch {
            chatRepository.setChatRoomMuted(serviceType, roomId, !isMuted)
        }
    }
}
