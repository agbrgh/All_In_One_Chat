package com.example.all_in_one_chat.ui.chat.room

import androidx.lifecycle.viewModelScope
import com.example.all_in_one_chat.domain.model.ChatMessage
import com.example.all_in_one_chat.domain.repository.ChatRepository
import com.example.all_in_one_chat.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : MviViewModel<ChatRoomIntent, ChatRoomViewState, ChatRoomEffect>(ChatRoomViewState()) {

    override suspend fun handleIntent(intent: ChatRoomIntent) {
        when (intent) {
            is ChatRoomIntent.LoadMessages -> loadMessages(intent)
            is ChatRoomIntent.SendMessage -> sendMessage(intent.content)
            is ChatRoomIntent.SetReplyTarget -> reduceState { it.copy(replyTargetMessage = intent.message) }
            is ChatRoomIntent.DeleteMessage -> deleteMessage(intent.message)
            is ChatRoomIntent.ForwardMessage -> forwardMessage(intent)
        }
    }

    private fun loadMessages(intent: ChatRoomIntent.LoadMessages) {
        reduceState { it.copy(roomId = intent.roomId, serviceType = intent.serviceType, isLoading = true) }
        
        chatRepository.getChatRoom(intent.serviceType, intent.roomId)
            .onEach { room ->
                room?.let { reduceState { state -> state.copy(roomName = it.roomName) } }
            }
            .launchIn(viewModelScope)

        chatRepository.getMessages(intent.serviceType, intent.roomId)
            .onEach { messages ->
                reduceState { it.copy(messages = messages, isLoading = false) }
            }
            .launchIn(viewModelScope)

        chatRepository.getChatRooms()
            .onEach { rooms ->
                reduceState { it.copy(allRooms = rooms) }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            chatRepository.refreshMessages(intent.serviceType, intent.roomId)
            chatRepository.markAsRead(intent.serviceType, intent.roomId)
        }
    }

    private fun sendMessage(content: String) {
        val currentRoomId = viewState.value.roomId
        val currentServiceType = viewState.value.serviceType ?: return
        val replyToId = viewState.value.replyTargetMessage?.messageId

        viewModelScope.launch {
            val result = chatRepository.sendMessage(
                serviceType = currentServiceType,
                roomId = currentRoomId,
                content = content,
                replyToMessageId = replyToId
            )
            if (result.isSuccess) {
                reduceState { it.copy(replyTargetMessage = null) }
            } else {
                sendEffect(ChatRoomEffect.ShowError(result.exceptionOrNull()?.message ?: "Failed to send message"))
            }
        }
    }

    private fun deleteMessage(message: ChatMessage) {
        viewModelScope.launch {
            val result = chatRepository.deleteMessage(message.serviceType, message.roomId, message.messageId)
            if (result.isFailure) {
                sendEffect(ChatRoomEffect.ShowError(result.exceptionOrNull()?.message ?: "Failed to delete message"))
            }
        }
    }

    private fun forwardMessage(intent: ChatRoomIntent.ForwardMessage) {
        viewModelScope.launch {
            val result = chatRepository.forwardMessage(intent.message, intent.targetServiceType, intent.targetRoomId)
            if (result.isSuccess) {
                // 전달 성공 알림 등 필요 시 Effect 추가 가능
            } else {
                sendEffect(ChatRoomEffect.ShowError(result.exceptionOrNull()?.message ?: "Failed to forward message"))
            }
        }
    }
}
