package com.example.all_in_one_chat.di

import android.content.Context
import com.example.all_in_one_chat.service.analytics.AnalyticsHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    @Provides
    @Singleton
    fun provideAnalyticsHelper(@ApplicationContext context: Context): AnalyticsHelper {
        return AnalyticsHelper(context)
    }
}
