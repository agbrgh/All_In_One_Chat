package com.example.all_in_one_chat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.all_in_one_chat.data.local.entity.ChatUserEntity
import com.example.all_in_one_chat.domain.model.ChatServiceType
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatUserDao {
    @Query("SELECT * FROM chat_users WHERE serviceType = :serviceType AND userId = :userId LIMIT 1")
    fun getUser(serviceType: ChatServiceType, userId: String): Flow<ChatUserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: ChatUserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<ChatUserEntity>)

    @Query("DELETE FROM chat_users WHERE serviceType = :serviceType AND userId = :userId")
    suspend fun deleteUser(serviceType: ChatServiceType, userId: String)
}
