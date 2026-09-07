package com.example.all_in_one_chat.ui.auth.service

import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.model.ServiceStatus
import com.example.all_in_one_chat.mvi.MviIntent
import com.example.all_in_one_chat.mvi.MviViewEffect
import com.example.all_in_one_chat.mvi.MviViewState

data class ServiceManagementViewState(
    val isLoading: Boolean = false,
    val services: List<ServiceStatus> = emptyList(),
    val error: String? = null
) : MviViewState

sealed class ServiceManagementIntent : MviIntent {
    object LoadServices : ServiceManagementIntent()
    data class ToggleServiceActive(val serviceType: ChatServiceType, val isActive: Boolean) : ServiceManagementIntent()
    data class ConnectService(val serviceType: ChatServiceType) : ServiceManagementIntent()
}

sealed class ServiceManagementViewEffect : MviViewEffect {
    data class NavigateToConnect(val serviceType: ChatServiceType) : ServiceManagementViewEffect()
    data class ShowError(val message: String) : ServiceManagementViewEffect()
}
