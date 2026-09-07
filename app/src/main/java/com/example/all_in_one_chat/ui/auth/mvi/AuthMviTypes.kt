package com.example.all_in_one_chat.ui.auth.mvi

import com.example.all_in_one_chat.domain.model.AuthUser

/**
 * Auth View State
 */
data class AuthViewState(
    val isLoading: Boolean = false,
    val currentUser: AuthUser? = null,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    val errorCode: String? = null,
    val successMessage: String? = null,
    val screenMode: AuthScreenMode = AuthScreenMode.LOGIN,
    // Input fields
    val emailInput: String = "",
    val passwordInput: String = "",
    val confirmPasswordInput: String = "",
    val displayNameInput: String = ""
)

enum class AuthScreenMode {
    LOGIN,
    SIGNUP,
    RESET_PASSWORD,
    PROFILE,
    VERIFY_EMAIL
}

/**
 * Auth View Event (User Actions)
 */
sealed class AuthViewEvent {
    data object SignInClicked : AuthViewEvent()
    data object SignUpClicked : AuthViewEvent()
    data object SignOutClicked : AuthViewEvent()
    data object SendPasswordResetClicked : AuthViewEvent()
    data class UpdateProfileClicked(val displayName: String?, val photoUrl: String?) : AuthViewEvent()
    data object SendEmailVerificationClicked : AuthViewEvent()
    data object ScreenModeChanged : AuthViewEvent()
    data object DismissError : AuthViewEvent()
    
    // Input change events
    data class EmailChanged(val email: String) : AuthViewEvent()
    data class PasswordChanged(val password: String) : AuthViewEvent()
    data class ConfirmPasswordChanged(val password: String) : AuthViewEvent()
    data class DisplayNameChanged(val name: String) : AuthViewEvent()
}

/**
 * Auth View Action (Internal Intents)
 */
sealed class AuthViewAction {
    data class SignIn(val email: String, val password: String) : AuthViewAction()
    data class SignUp(val email: String, val password: String, val displayName: String?) : AuthViewAction()
    data object SignOut : AuthViewAction()
    data class SendPasswordReset(val email: String) : AuthViewAction()
    data class UpdateProfile(val displayName: String?, val photoUrl: String?) : AuthViewAction()
    data object SendEmailVerification : AuthViewAction()
    data object LoadCurrentUser : AuthViewAction()
    data class SetScreenMode(val mode: AuthScreenMode) : AuthViewAction()
}

