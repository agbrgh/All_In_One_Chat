package com.example.all_in_one_chat.domain.usecase

import com.example.all_in_one_chat.domain.model.ChatMessage
import com.example.all_in_one_chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMediaMessagesUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    operator fun invoke(): Flow<List<ChatMessage>> {
        return repository.getMediaMessages()
    }
}
