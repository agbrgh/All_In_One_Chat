package com.example.all_in_one_chat.di

import android.content.Context
import androidx.room.Room
import com.example.all_in_one_chat.data.local.dao.ChatRoomDao
import com.example.all_in_one_chat.data.local.dao.MessageDao
import com.example.all_in_one_chat.data.local.dao.ChatUserDao
import com.example.all_in_one_chat.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                AppDatabase.DATABASE_NAME
            ).fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    fun provideChatUserDao(database: AppDatabase): ChatUserDao {
        return database.chatUserDao()
    }

    @Provides
    fun provideChatRoomDao(database: AppDatabase): ChatRoomDao {
        return database.chatRoomDao()
    }

    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao {
        return database.messageDao()
    }
}
