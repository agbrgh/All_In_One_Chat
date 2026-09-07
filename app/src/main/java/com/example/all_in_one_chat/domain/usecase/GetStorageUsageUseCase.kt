package com.example.all_in_one_chat.domain.usecase

import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStorageUsageUseCase @Inject constructor(
    private val repository: StorageRepository
) {
    fun getServiceUsage(serviceType: ChatServiceType): Flow<Long> {
        return repository.getStorageUsage(serviceType)
    }

    fun getTotalUsage(): Flow<Long> {
        return repository.getTotalStorageUsage()
    }
}
