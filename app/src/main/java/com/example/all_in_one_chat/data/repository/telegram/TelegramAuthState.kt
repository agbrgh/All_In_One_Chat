package com.example.all_in_one_chat.data.repository.telegram

sealed class TelegramAuthState {
    object Initial : TelegramAuthState()
    object WaitPhoneNumber : TelegramAuthState()
    object WaitCode : TelegramAuthState()
    object WaitPassword : TelegramAuthState()
    object Authenticated : TelegramAuthState()
    data class Error(val message: String) : TelegramAuthState()
}
