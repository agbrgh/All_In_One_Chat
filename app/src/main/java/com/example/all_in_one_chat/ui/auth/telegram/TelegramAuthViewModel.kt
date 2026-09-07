package com.example.all_in_one_chat.ui.auth.telegram

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.all_in_one_chat.data.repository.telegram.TelegramAuthState
import com.example.all_in_one_chat.data.repository.telegram.TelegramServiceAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TelegramAuthViewModel @Inject constructor(
    private val telegramAdapter: TelegramServiceAdapter
) : ViewModel() {

    val authState: StateFlow<TelegramAuthState> = telegramAdapter.authState

    fun onPhoneNumberEntered(phoneNumber: String) {
        viewModelScope.launch {
            telegramAdapter.setPhoneNumber(phoneNumber)
        }
    }

    fun onCodeEntered(code: String) {
        viewModelScope.launch {
            telegramAdapter.setCode(code)
        }
    }

    fun onPasswordEntered(password: String) {
        viewModelScope.launch {
            telegramAdapter.setPassword(password)
        }
    }

    fun retry() {
        viewModelScope.launch {
            telegramAdapter.login()
        }
    }
}
