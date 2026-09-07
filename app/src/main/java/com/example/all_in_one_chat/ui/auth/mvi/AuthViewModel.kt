package com.example.all_in_one_chat.ui.auth.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.all_in_one_chat.domain.usecase.DeleteAccountUseCase
import com.example.all_in_one_chat.domain.usecase.GetCurrentUserUseCase
import com.example.all_in_one_chat.domain.usecase.ObserveAuthStateUseCase
import com.example.all_in_one_chat.domain.usecase.SendEmailVerificationUseCase
import com.example.all_in_one_chat.domain.usecase.SendPasswordResetEmailUseCase
import com.example.all_in_one_chat.domain.usecase.SignInUseCase
import com.example.all_in_one_chat.domain.usecase.SignOutUseCase
import com.example.all_in_one_chat.domain.usecase.SignUpUseCase
import com.example.all_in_one_chat.domain.usecase.UpdateProfileUseCase
import com.example.all_in_one_chat.domain.model.AuthState
import com.example.all_in_one_chat.service.analytics.AnalyticsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Auth ViewModel - MVI Pattern
 * Manages authentication state and user interactions
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val sendEmailVerificationUseCase: SendEmailVerificationUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val _viewState = MutableStateFlow(AuthViewState())
    val viewState: StateFlow<AuthViewState> = _viewState.asStateFlow()

    private val _viewEvent = MutableSharedFlow<AuthViewEvent>()

    init {
        // Observe auth state changes
        observeAuthStateUseCase().onEach { authState ->
            handleAuthStateChange(authState)
        }.launchIn(viewModelScope)
    }

    /**
     * Handle view events from UI
     */
    fun handleEvent(event: AuthViewEvent) {
        when (event) {
            is AuthViewEvent.SignInClicked -> {
                handleAction(AuthViewAction.SignIn(viewState.value.emailInput, viewState.value.passwordInput))
            }
            is AuthViewEvent.SignUpClicked -> {
                handleAction(AuthViewAction.SignUp(
                    viewState.value.emailInput,
                    viewState.value.passwordInput,
                    viewState.value.displayNameInput.takeIf { it.isNotEmpty() }
                ))
            }
            is AuthViewEvent.SignOutClicked -> {
                handleAction(AuthViewAction.SignOut)
            }
            is AuthViewEvent.SendPasswordResetClicked -> {
                handleAction(AuthViewAction.SendPasswordReset(viewState.value.emailInput))
            }
            is AuthViewEvent.UpdateProfileClicked -> {
                handleAction(AuthViewAction.UpdateProfile(event.displayName, event.photoUrl))
            }
            is AuthViewEvent.SendEmailVerificationClicked -> {
                handleAction(AuthViewAction.SendEmailVerification)
            }
            is AuthViewEvent.ScreenModeChanged -> {
                updateViewState { copy(error = null, successMessage = null) }
            }
            is AuthViewEvent.DismissError -> {
                updateViewState { copy(error = null, errorCode = null) }
            }
            is AuthViewEvent.EmailChanged -> {
                updateViewState { copy(emailInput = event.email.trim()) }
            }
            is AuthViewEvent.PasswordChanged -> {
                updateViewState { copy(passwordInput = event.password) }
            }
            is AuthViewEvent.ConfirmPasswordChanged -> {
                updateViewState { copy(confirmPasswordInput = event.password) }
            }
            is AuthViewEvent.DisplayNameChanged -> {
                updateViewState { copy(displayNameInput = event.name) }
            }
        }
    }

    /**
     * Handle internal actions
     */
    private fun handleAction(action: AuthViewAction) {
        when (action) {
            is AuthViewAction.SignIn -> signIn(action.email, action.password)
            is AuthViewAction.SignUp -> signUp(action.email, action.password, action.displayName)
            is AuthViewAction.SignOut -> signOut()
            is AuthViewAction.SendPasswordReset -> sendPasswordReset(action.email)
            is AuthViewAction.UpdateProfile -> updateProfile(action.displayName, action.photoUrl)
            is AuthViewAction.SendEmailVerification -> sendEmailVerification()
            is AuthViewAction.LoadCurrentUser -> loadCurrentUser()
            is AuthViewAction.SetScreenMode -> updateViewState { copy(screenMode = action.mode) }
        }
    }

    private fun signIn(email: String, password: String) {
        viewModelScope.launch {
            android.util.Log.d("AuthViewModel", "Starting signIn for $email")
            updateViewState { copy(isLoading = true, error = null) }
            val result = signInUseCase(email, password)

            result.onSuccess { user ->
                android.util.Log.d("AuthViewModel", "signIn success: ${user.uid}")
                analyticsHelper.logLogin("email", true)
                updateViewState { copy(isLoading = false, currentUser = user, isAuthenticated = true) }
            }.onFailure { exception ->
                android.util.Log.e("AuthViewModel", "signIn failure", exception)
                analyticsHelper.logLogin("email", false)
                updateViewState {
                    copy(
                        isLoading = false,
                        error = exception.message ?: "Sign in failed"
                    )
                }
            }
        }
    }

    private fun signUp(email: String, password: String, displayName: String?) {
        viewModelScope.launch {
            android.util.Log.d("AuthViewModel", "Starting signUp for $email")
            updateViewState { copy(isLoading = true, error = null) }
            val result = signUpUseCase(email, password, displayName)

            result.onSuccess { user ->
                android.util.Log.d("AuthViewModel", "signUp success: ${user.uid}")
                analyticsHelper.logLogin("sign_up", true)
                updateViewState {
                    copy(
                        isLoading = false,
                        currentUser = user,
                        isAuthenticated = true,
                        successMessage = "Account created successfully!"
                    )
                }
            }.onFailure { exception ->
                android.util.Log.e("AuthViewModel", "signUp failure", exception)
                updateViewState {
                    copy(
                        isLoading = false,
                        error = exception.message ?: "Sign up failed"
                    )
                }
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            updateViewState { copy(isLoading = true) }
            val result = signOutUseCase()

            result.onSuccess {
                updateViewState {
                    copy(
                        isLoading = false,
                        currentUser = null,
                        isAuthenticated = false,
                        successMessage = "Signed out successfully"
                    )
                }
            }.onFailure { exception ->
                updateViewState {
                    copy(
                        isLoading = false,
                        error = exception.message ?: "Sign out failed"
                    )
                }
            }
        }
    }

    private fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            updateViewState { copy(isLoading = true, error = null) }
            val result = sendPasswordResetEmailUseCase(email)

            result.onSuccess {
                updateViewState {
                    copy(
                        isLoading = false,
                        successMessage = "Password reset email sent to $email"
                    )
                }
            }.onFailure { exception ->
                updateViewState {
                    copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to send reset email"
                    )
                }
            }
        }
    }

    private fun updateProfile(displayName: String?, photoUrl: String?) {
        viewModelScope.launch {
            updateViewState { copy(isLoading = true, error = null) }
            val result = updateProfileUseCase(displayName, photoUrl)

            result.onSuccess { user ->
                updateViewState {
                    copy(
                        isLoading = false,
                        currentUser = user,
                        successMessage = "Profile updated successfully"
                    )
                }
            }.onFailure { exception ->
                updateViewState {
                    copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to update profile"
                    )
                }
            }
        }
    }

    private fun sendEmailVerification() {
        viewModelScope.launch {
            updateViewState { copy(isLoading = true, error = null) }
            val result = sendEmailVerificationUseCase()

            result.onSuccess {
                updateViewState {
                    copy(
                        isLoading = false,
                        successMessage = "Verification email sent"
                    )
                }
            }.onFailure { exception ->
                updateViewState {
                    copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to send verification email"
                    )
                }
            }
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            updateViewState { copy(currentUser = user, isAuthenticated = user != null) }
        }
    }

    private fun handleAuthStateChange(authState: AuthState) {
        android.util.Log.d("AuthViewModel", "handleAuthStateChange: $authState")
        when (authState) {
            is AuthState.Authenticated -> {
                updateViewState {
                    copy(
                        currentUser = authState.user,
                        isAuthenticated = true,
                        isLoading = false,
                        error = null
                    )
                }
            }
            is AuthState.Unauthenticated -> {
                updateViewState {
                    copy(
                        currentUser = null,
                        isAuthenticated = false,
                        isLoading = false,
                        error = null
                    )
                }
            }
            is AuthState.Loading -> {
                updateViewState { copy(isLoading = true) }
            }
            is AuthState.Error -> {
                updateViewState {
                    copy(
                        isLoading = false,
                        error = authState.message,
                        errorCode = authState.code
                    )
                }
            }
            is AuthState.Initial -> {
                updateViewState { copy(isLoading = false) }
            }
        }
    }

    private fun updateViewState(block: AuthViewState.() -> AuthViewState) {
        _viewState.value = _viewState.value.block()
    }
}



