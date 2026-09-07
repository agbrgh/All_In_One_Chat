package com.example.all_in_one_chat.di

import com.example.all_in_one_chat.data.repository.AuthRepositoryImpl
import com.example.all_in_one_chat.data.repository.ChatRepositoryImpl
import com.example.all_in_one_chat.data.repository.NotificationRepositoryImpl
import com.example.all_in_one_chat.data.repository.StorageRepositoryImpl
import com.example.all_in_one_chat.domain.repository.AuthRepository
import com.example.all_in_one_chat.domain.repository.ChatRepository
import com.example.all_in_one_chat.domain.repository.NotificationRepository
import com.example.all_in_one_chat.domain.repository.StorageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        impl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindStorageRepository(
        impl: StorageRepositoryImpl
    ): StorageRepository
}
