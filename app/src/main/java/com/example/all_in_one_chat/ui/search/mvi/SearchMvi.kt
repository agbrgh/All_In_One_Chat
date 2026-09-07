package com.example.all_in_one_chat.ui.search.mvi

import com.example.all_in_one_chat.domain.model.ChatMessage

sealed interface SearchIntent {
    data class UpdateQuery(val query: String) : SearchIntent
    data class SelectMessage(val message: ChatMessage) : SearchIntent
    object ClearSearch : SearchIntent
}

data class SearchViewState(
    val query: String = "",
    val results: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface SearchEffect {
    data class NavigateToChat(val serviceType: String, val roomId: String) : SearchEffect
}
