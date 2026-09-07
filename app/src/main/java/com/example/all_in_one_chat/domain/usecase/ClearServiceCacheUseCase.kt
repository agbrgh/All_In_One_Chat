package com.example.all_in_one_chat.domain.usecase

import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.repository.StorageRepository
import javax.inject.Inject

class ClearServiceCacheUseCase @Inject constructor(
    private val repository: StorageRepository
) {
    suspend operator fun invoke(serviceType: ChatServiceType): Result<Unit> {
        return repository.clearServiceCache(serviceType)
    }
}
