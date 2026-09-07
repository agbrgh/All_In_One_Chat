package com.example.all_in_one_chat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.all_in_one_chat.data.local.entity.ChatMessageEntity
import com.example.all_in_one_chat.domain.model.ChatServiceType
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM chat_messages WHERE serviceType = :serviceType AND roomId = :roomId ORDER BY timestamp ASC")
    fun getMessagesForRoom(serviceType: ChatServiceType, roomId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE serviceType = :serviceType AND roomId = :roomId")
    suspend fun deleteMessagesForRoom(serviceType: ChatServiceType, roomId: String)

    @Query("DELETE FROM chat_messages WHERE serviceType = :serviceType AND roomId = :roomId AND messageId = :messageId")
    suspend fun deleteMessage(serviceType: ChatServiceType, roomId: String, messageId: String)

    @Query("SELECT * FROM chat_messages WHERE mediaType = 'VIDEO' AND mediaLocalPath IS NULL AND mediaFileId IS NOT NULL")
    suspend fun getPendingVideoMessages(): List<ChatMessageEntity>

    @Query("UPDATE chat_messages SET mediaLocalPath = :localPath WHERE serviceType = :serviceType AND mediaFileId = :fileId")
    suspend fun updateMediaLocalPath(serviceType: ChatServiceType, fileId: Int, localPath: String)

    @Query("""
        SELECT * FROM chat_messages 
        JOIN chat_messages_fts ON chat_messages.content = chat_messages_fts.content 
        WHERE chat_messages_fts MATCH :query
    """)
    fun searchMessages(query: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE mediaType IS NOT NULL ORDER BY timestamp DESC")
    fun getAllMediaMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE serviceType = :serviceType AND mediaLocalPath IS NOT NULL")
    suspend fun getDownloadedMediaByService(serviceType: ChatServiceType): List<ChatMessageEntity>

    @Query("UPDATE chat_messages SET mediaLocalPath = NULL WHERE serviceType = :serviceType")
    suspend fun clearMediaLocalPaths(serviceType: ChatServiceType)

    @Query("SELECT * FROM chat_messages WHERE serviceType = :serviceType AND messageId = :messageId LIMIT 1")
    fun getMessage(serviceType: ChatServiceType, messageId: String): Flow<ChatMessageEntity?>
}
