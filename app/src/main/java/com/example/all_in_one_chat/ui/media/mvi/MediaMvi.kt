package com.example.all_in_one_chat.ui.media.mvi

import com.example.all_in_one_chat.domain.model.ChatMessage
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.model.MediaType

sealed interface MediaIntent {
    object LoadMedia : MediaIntent
    data class FilterByType(val type: MediaType?) : MediaIntent
    data class FilterByService(val serviceType: ChatServiceType?) : MediaIntent
    data class ClickMedia(val message: ChatMessage) : MediaIntent
}

data class MediaViewState(
    val allMedia: List<ChatMessage> = emptyList(),
    val filteredMedia: List<ChatMessage> = emptyList(),
    val selectedType: MediaType? = null,
    val selectedService: ChatServiceType? = null,
    val isLoading: Boolean = false
)

sealed interface MediaEffect {
    data class ShowMediaDetail(val message: ChatMessage) : MediaEffect
}
