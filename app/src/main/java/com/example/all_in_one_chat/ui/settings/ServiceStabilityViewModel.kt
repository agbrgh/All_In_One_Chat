package com.example.all_in_one_chat.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.all_in_one_chat.service.notification.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

data class ServiceStabilityViewState(
    val isNotificationListenerEnabled: Boolean = false,
    val isBatteryOptimizationIgnored: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class ServiceStabilityViewModel @Inject constructor(
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _viewState = MutableStateFlow(ServiceStabilityViewState())
    val viewState: StateFlow<ServiceStabilityViewState> = _viewState.asStateFlow()

    init {
        refreshStatus()
        observeStatus()
    }

    fun refreshStatus() {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(
                isNotificationListenerEnabled = notificationHelper.isNotificationListenerEnabled(),
                isBatteryOptimizationIgnored = notificationHelper.isBatteryOptimizationIgnored(),
                isLoading = false
            )
        }
    }

    private fun observeStatus() {
        viewModelScope.launch {
            while (true) {
                val currentListener = notificationHelper.isNotificationListenerEnabled()
                val currentBattery = notificationHelper.isBatteryOptimizationIgnored()
                
                if (currentListener != _viewState.value.isNotificationListenerEnabled ||
                    currentBattery != _viewState.value.isBatteryOptimizationIgnored) {
                    _viewState.value = _viewState.value.copy(
                        isNotificationListenerEnabled = currentListener,
                        isBatteryOptimizationIgnored = currentBattery
                    )
                }
                delay(2000.milliseconds)
            }
        }
    }
}
