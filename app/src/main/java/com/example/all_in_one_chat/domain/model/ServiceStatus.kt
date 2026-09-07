package com.example.all_in_one_chat.domain.model

/**
 * Represent the status of a chat service.
 */
data class ServiceStatus(
    val serviceType: ChatServiceType,
    val isConnected: Boolean,
    val isActive: Boolean
)
