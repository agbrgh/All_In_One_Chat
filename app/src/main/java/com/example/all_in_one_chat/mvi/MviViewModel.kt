package com.example.all_in_one_chat.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel for MVI pattern
 * 
 * @param Initial state
 * @param I - Intent type
 * @param S - ViewState type
 * @param E - ViewEffect type
 */
abstract class MviViewModel<I : MviIntent, S : MviViewState, E : MviViewEffect>(
    initialState: S
) : ViewModel() {

    // State management
    private val _viewState = MutableStateFlow(initialState)
    val viewState: StateFlow<S> = _viewState.asStateFlow()

    // Effect management (one-time events)
    private val _viewEffect = MutableSharedFlow<E>(replay = 0, extraBufferCapacity = 1)
    val viewEffect: SharedFlow<E> = _viewEffect.asSharedFlow()

    // Intent handling
    private val _intent = MutableSharedFlow<I>(replay = 0, extraBufferCapacity = 64)

    init {
        handleIntents()
    }

    /**
     * Handle incoming intents and update state accordingly
     */
    private fun handleIntents() {
        viewModelScope.launch {
            _intent.collect { intent ->
                handleIntent(intent)
            }
        }
    }

    /**
     * Process user intent and update state/effect
     */
    protected abstract suspend fun handleIntent(intent: I)

    /**
     * Send a user intent to be processed
     */
    fun sendIntent(intent: I) {
        viewModelScope.launch {
            _intent.emit(intent)
        }
    }

    /**
     * Update the view state
     */
    protected fun setState(newState: S) {
        _viewState.value = newState
    }

    /**
     * Reduce the current state with a reducer function
     */
    protected fun reduceState(reducer: (S) -> S) {
        _viewState.value = reducer(_viewState.value)
    }

    /**
     * Send a view effect (one-time event)
     */
    protected fun sendEffect(effect: E) {
        viewModelScope.launch {
            _viewEffect.emit(effect)
        }
    }

    /**
     * Get current state synchronously
     */
    protected fun getCurrentState(): S = _viewState.value
}
