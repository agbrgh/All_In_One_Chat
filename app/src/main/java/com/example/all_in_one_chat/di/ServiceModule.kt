package com.example.all_in_one_chat.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.example.all_in_one_chat.data.repository.telegram.TelegramServiceAdapter
import com.example.all_in_one_chat.data.repository.discord.DiscordServiceAdapter
import com.example.all_in_one_chat.data.repository.whatsapp.WhatsAppServiceAdapter
import com.example.all_in_one_chat.domain.repository.ChatServiceAdapter
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import dagger.multibindings.Multibinds
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Multibinds
    abstract fun bindChatServiceAdapters(): Set<ChatServiceAdapter>

    @Binds
    @IntoSet
    abstract fun bindTelegramServiceAdapter(adapter: TelegramServiceAdapter): ChatServiceAdapter

    @Binds
    @IntoSet
    abstract fun bindDiscordServiceAdapter(adapter: DiscordServiceAdapter): ChatServiceAdapter

    @Binds
    @IntoSet
    abstract fun bindWhatsAppServiceAdapter(adapter: WhatsAppServiceAdapter): ChatServiceAdapter

    companion object {
        /**
         * Provide Firebase Authentication instance
         */
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth {
            return FirebaseAuth.getInstance()
        }

        /**
         * Provide Firestore instance with offline persistence enabled
         */
        @Provides
        @Singleton
        fun provideFirebaseFirestore(): FirebaseFirestore {
            val firestore = FirebaseFirestore.getInstance()

            // Enable offline persistence using the modern PersistentCacheSettings API
            try {
                val settings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(PersistentCacheSettings.newBuilder()
                        .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                        .build())
                    .build()
                firestore.firestoreSettings = settings
            } catch (e: Exception) {
                // Offline persistence settings can only be applied once
                e.printStackTrace()
            }

            return firestore
        }
    }
}
