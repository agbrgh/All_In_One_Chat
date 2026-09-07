package com.example.all_in_one_chat.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.all_in_one_chat.data.local.dao.ChatRoomDao
import com.example.all_in_one_chat.data.local.dao.MessageDao
import com.example.all_in_one_chat.data.local.dao.ChatUserDao
import com.example.all_in_one_chat.data.local.entity.ChatMessageEntity
import com.example.all_in_one_chat.data.local.entity.ChatMessageFtsEntity
import com.example.all_in_one_chat.data.local.entity.ChatRoomEntity
import com.example.all_in_one_chat.data.local.entity.ChatUserEntity
import androidx.room.Transaction

@Database(
    entities = [ChatRoomEntity::class, ChatMessageEntity::class, ChatUserEntity::class, ChatMessageFtsEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatRoomDao(): ChatRoomDao
    abstract fun messageDao(): MessageDao
    abstract fun chatUserDao(): ChatUserDao

    @Transaction
    suspend fun insertMessagesWithUsers(users: List<ChatUserEntity>, messages: List<ChatMessageEntity>) {
        chatUserDao().insertUsers(users)
        messageDao().insertMessages(messages)
    }

    @Transaction
    suspend fun insertMessageWithUser(user: ChatUserEntity, message: ChatMessageEntity) {
        chatUserDao().insertUser(user)
        messageDao().insertMessage(message)
    }

    companion object {
        const val DATABASE_NAME = "all_in_one_chat_db"
    }
}
