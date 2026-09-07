package com.example.all_in_one_chat.ui.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.all_in_one_chat.domain.usecase.GetMediaMessagesUseCase
import com.example.all_in_one_chat.ui.media.mvi.MediaEffect
import com.example.all_in_one_chat.ui.media.mvi.MediaIntent
import com.example.all_in_one_chat.ui.media.mvi.MediaViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val getMediaMessagesUseCase: GetMediaMessagesUseCase
) : ViewModel() {

    private val _viewState = MutableStateFlow(MediaViewState())
    val viewState: StateFlow<MediaViewState> = _viewState.asStateFlow()

    private val _viewEffect = MutableSharedFlow<MediaEffect>()
    val viewEffect: SharedFlow<MediaEffect> = _viewEffect.asSharedFlow()

    init {
        loadMedia()
    }

    private fun loadMedia() {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isLoading = true)
            getMediaMessagesUseCase().collectLatest { media ->
                _viewState.value = _viewState.value.copy(
                    allMedia = media,
                    isLoading = false
                )
                applyFilters()
            }
        }
    }

    fun sendIntent(intent: MediaIntent) {
        when (intent) {
            MediaIntent.LoadMedia -> loadMedia()
            is MediaIntent.FilterByType -> {
                _viewState.value = _viewState.value.copy(selectedType = intent.type)
                applyFilters()
            }
            is MediaIntent.FilterByService -> {
                _viewState.value = _viewState.value.copy(selectedService = intent.serviceType)
                applyFilters()
            }
            is MediaIntent.ClickMedia -> {
                viewModelScope.launch {
                    _viewEffect.emit(MediaEffect.ShowMediaDetail(intent.message))
                }
            }
        }
    }

    private fun applyFilters() {
        val state = _viewState.value
        val filtered = state.allMedia.filter { message ->
            val typeMatch = state.selectedType == null || message.mediaType == state.selectedType
            val serviceMatch = state.selectedService == null || message.serviceType == state.selectedService
            typeMatch && serviceMatch
        }
        _viewState.value = state.copy(filteredMedia = filtered)
    }
}
