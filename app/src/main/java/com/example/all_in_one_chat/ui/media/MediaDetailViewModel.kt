package com.example.all_in_one_chat.ui.media

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.all_in_one_chat.domain.model.ChatMessage
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.usecase.GetMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MediaDetailViewState(
    val message: ChatMessage? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class MediaDetailViewModel @Inject constructor(
    private val getMessageUseCase: GetMessageUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _viewState = MutableStateFlow(MediaDetailViewState())
    val viewState: StateFlow<MediaDetailViewState> = _viewState.asStateFlow()

    private val serviceTypeStr: String = savedStateHandle.get<String>("serviceType") ?: ""
    private val messageId: String = savedStateHandle.get<String>("messageId") ?: ""

    init {
        loadMessage()
    }

    private fun loadMessage() {
        if (serviceTypeStr.isBlank() || messageId.isBlank()) {
            _viewState.value = _viewState.value.copy(isLoading = false, error = "Invalid arguments")
            return
        }

        val serviceType = try {
            ChatServiceType.valueOf(serviceTypeStr)
        } catch (_: Exception) {
            null
        }

        if (serviceType == null) {
            _viewState.value = _viewState.value.copy(isLoading = false, error = "Invalid service type")
            return
        }

        viewModelScope.launch {
            getMessageUseCase(serviceType, messageId).collect { message ->
                _viewState.value = _viewState.value.copy(
                    message = message,
                    isLoading = false,
                    error = if (message == null) "Message not found" else null
                )
            }
        }
    }
}
