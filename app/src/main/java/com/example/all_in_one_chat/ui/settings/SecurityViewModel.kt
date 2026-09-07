package com.example.all_in_one_chat.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.all_in_one_chat.data.local.prefs.SecurePreferences
import com.example.all_in_one_chat.service.security.BiometricAuthenticator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SecurityViewState(
    val isAppLockEnabled: Boolean = false,
    val isBiometricAvailable: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val securePreferences: SecurePreferences,
    private val biometricAuthenticator: BiometricAuthenticator
) : ViewModel() {

    private val _viewState = MutableStateFlow(SecurityViewState())
    val viewState: StateFlow<SecurityViewState> = _viewState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val isEnabled = securePreferences.getBoolean(SecurePreferences.KEY_APP_LOCK_ENABLED, false)
            _viewState.value = _viewState.value.copy(
                isAppLockEnabled = isEnabled,
                isBiometricAvailable = biometricAuthenticator.isBiometricAvailable()
            )
        }
    }

    fun toggleAppLock(enabled: Boolean) {
        viewModelScope.launch {
            securePreferences.saveBoolean(SecurePreferences.KEY_APP_LOCK_ENABLED, enabled)
            _viewState.value = _viewState.value.copy(isAppLockEnabled = enabled)
        }
    }

    fun clearMessage() {
        _viewState.value = _viewState.value.copy(message = null)
    }
}
