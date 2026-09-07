package com.example.all_in_one_chat.data.repository

import com.example.all_in_one_chat.data.local.dao.MessageDao
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class StorageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao
) : StorageRepository {

    override fun getStorageUsage(serviceType: ChatServiceType): Flow<Long> = flow {
        // We calculate usage periodically or when requested
        // For simplicity, we calculate on each collection
        while (true) {
            val mediaMessages = messageDao.getDownloadedMediaByService(serviceType)
            val totalSize = mediaMessages.sumOf { message ->
                message.mediaLocalPath?.let { path ->
                    File(path).length()
                } ?: 0L
            }
            emit(totalSize)
            kotlinx.coroutines.delay(5000.milliseconds) // Update every 5 seconds if observing
        }
    }

    override fun getTotalStorageUsage(): Flow<Long> {
        val flows = ChatServiceType.entries.map { getStorageUsage(it) }
        return combine(flows) { it.sum() }
    }

    override suspend fun clearServiceCache(serviceType: ChatServiceType): Result<Unit> {
        return try {
            val mediaMessages = messageDao.getDownloadedMediaByService(serviceType)
            mediaMessages.forEach { message ->
                message.mediaLocalPath?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        file.delete()
                    }
                }
            }
            messageDao.clearMediaLocalPaths(serviceType)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
