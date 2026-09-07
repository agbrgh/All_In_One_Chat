package com.example.all_in_one_chat.domain.model

/**
 * Authentication state for Auth UI
 */
sealed class AuthState {
    data object Initial : AuthState()
    data object Loading : AuthState()
    data class Authenticated(val user: AuthUser) : AuthState()
    data object Unauthenticated : AuthState()
    data class Error(val message: String, val code: String? = null) : AuthState()
}


