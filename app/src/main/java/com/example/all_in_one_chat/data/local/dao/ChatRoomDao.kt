package com.example.all_in_one_chat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.all_in_one_chat.data.local.entity.ChatRoomEntity
import com.example.all_in_one_chat.domain.model.ChatServiceType
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatRoomDao {
    @Query("SELECT * FROM chat_rooms WHERE isArchived = 0 ORDER BY lastMessageTimestamp DESC")
    fun getAllChatRooms(): Flow<List<ChatRoomEntity>>

    @Query("SELECT * FROM chat_rooms WHERE serviceType = :serviceType AND isArchived = 0 ORDER BY lastMessageTimestamp DESC")
    fun getChatRoomsByService(serviceType: ChatServiceType): Flow<List<ChatRoomEntity>>

    @Query("SELECT * FROM chat_rooms WHERE serviceType = :serviceType AND roomId = :roomId LIMIT 1")
    fun getChatRoom(serviceType: ChatServiceType, roomId: String): Flow<ChatRoomEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatRooms(chatRooms: List<ChatRoomEntity>)

    @Query("DELETE FROM chat_rooms WHERE serviceType = :serviceType AND roomId = :roomId")
    suspend fun deleteChatRoom(serviceType: ChatServiceType, roomId: String)

    @Query("DELETE FROM chat_rooms")
    suspend fun deleteAllChatRooms()

    @Query("UPDATE chat_rooms SET unreadCount = 0 WHERE serviceType = :serviceType AND roomId = :roomId")
    suspend fun markAsRead(serviceType: ChatServiceType, roomId: String)

    @Query("UPDATE chat_rooms SET isArchived = :isArchived WHERE serviceType = :serviceType AND roomId = :roomId")
    suspend fun setArchived(serviceType: ChatServiceType, roomId: String, isArchived: Boolean)

    @Query("UPDATE chat_rooms SET isMuted = :isMuted WHERE serviceType = :serviceType AND roomId = :roomId")
    suspend fun setMuted(serviceType: ChatServiceType, roomId: String, isMuted: Boolean)
}
