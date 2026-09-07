package com.example.all_in_one_chat.domain.usecase

import com.example.all_in_one_chat.domain.model.ChatMessage
import com.example.all_in_one_chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchMessagesUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    operator fun invoke(query: String): Flow<List<ChatMessage>> {
        return repository.searchMessages(query)
    }
}
