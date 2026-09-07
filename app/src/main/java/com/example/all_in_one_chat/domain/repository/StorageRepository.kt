package com.example.all_in_one_chat.domain.repository

import com.example.all_in_one_chat.domain.model.ChatServiceType
import kotlinx.coroutines.flow.Flow

interface StorageRepository {
    /**
     * Get storage usage in bytes for a specific service.
     */
    fun getStorageUsage(serviceType: ChatServiceType): Flow<Long>

    /**
     * Get total storage usage in bytes for all services.
     */
    fun getTotalStorageUsage(): Flow<Long>

    /**
     * Clear all downloaded media files for a specific service.
     */
    suspend fun clearServiceCache(serviceType: ChatServiceType): Result<Unit>
}
