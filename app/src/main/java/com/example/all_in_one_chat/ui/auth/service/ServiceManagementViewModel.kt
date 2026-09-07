package com.example.all_in_one_chat.ui.auth.service

import androidx.lifecycle.viewModelScope
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.repository.ChatRepository
import com.example.all_in_one_chat.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ServiceManagementViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : MviViewModel<ServiceManagementIntent, ServiceManagementViewState, ServiceManagementViewEffect>(
    ServiceManagementViewState()
) {

    init {
        sendIntent(ServiceManagementIntent.LoadServices)
    }

    override suspend fun handleIntent(intent: ServiceManagementIntent) {
        when (intent) {
            is ServiceManagementIntent.LoadServices -> loadServices()
            is ServiceManagementIntent.ToggleServiceActive -> toggleServiceActive(intent.serviceType, intent.isActive)
            is ServiceManagementIntent.ConnectService -> connectService(intent.serviceType)
        }
    }

    private fun loadServices() {
        reduceState { it.copy(isLoading = true) }
        chatRepository.getServiceStatuses()
            .onEach { services ->
                reduceState { it.copy(isLoading = false, services = services) }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun toggleServiceActive(serviceType: ChatServiceType, isActive: Boolean) {
        val result = chatRepository.setServiceActive(serviceType, isActive)
        if (result.isFailure) {
            sendEffect(ServiceManagementViewEffect.ShowError("Failed to update service status"))
        }
    }

    private fun connectService(serviceType: ChatServiceType) {
        sendEffect(ServiceManagementViewEffect.NavigateToConnect(serviceType))
    }
}
