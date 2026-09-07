package com.example.all_in_one_chat.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.all_in_one_chat.domain.usecase.SearchMessagesUseCase
import com.example.all_in_one_chat.ui.search.mvi.SearchEffect
import com.example.all_in_one_chat.ui.search.mvi.SearchIntent
import com.example.all_in_one_chat.ui.search.mvi.SearchViewState
import com.example.all_in_one_chat.service.analytics.AnalyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchMessagesUseCase: SearchMessagesUseCase,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val _viewState = MutableStateFlow(SearchViewState())
    val viewState: StateFlow<SearchViewState> = _viewState.asStateFlow()

    private val _viewEffect = MutableSharedFlow<SearchEffect>()
    val viewEffect: SharedFlow<SearchEffect> = _viewEffect.asSharedFlow()

    private val _queryFlow = MutableStateFlow("")

    init {
        observeQuery()
    }

    @OptIn(FlowPreview::class)
    private fun observeQuery() {
        viewModelScope.launch {
            _queryFlow
                .debounce(300L.milliseconds)
                .distinctUntilChanged()
                .onEach { query ->
                    _viewState.value = _viewState.value.copy(isLoading = query.isNotBlank())
                }
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _viewState.value = _viewState.value.copy(results = emptyList(), isLoading = false)
                    } else {
                        analyticsHelper.logSearchPerformed(query.length)
                        searchMessagesUseCase(query).collect { results ->
                            _viewState.value = _viewState.value.copy(results = results, isLoading = false)
                        }
                    }
                }
        }
    }

    fun sendIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.UpdateQuery -> {
                _viewState.value = _viewState.value.copy(query = intent.query)
                _queryFlow.value = intent.query
            }
            is SearchIntent.SelectMessage -> {
                viewModelScope.launch {
                    _viewEffect.emit(
                        SearchEffect.NavigateToChat(
                            serviceType = intent.message.serviceType.name,
                            roomId = intent.message.roomId
                        )
                    )
                }
            }
            SearchIntent.ClearSearch -> {
                _viewState.value = _viewState.value.copy(query = "", results = emptyList(), isLoading = false)
                _queryFlow.value = ""
            }
        }
    }
}
