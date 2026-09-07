package com.example.all_in_one_chat

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.all_in_one_chat.domain.repository.ChatRepository
import com.example.all_in_one_chat.service.sync.SyncManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AllInOneChatApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var syncManager: Lazy<SyncManager>

    @Inject
    lateinit var chatRepository: ChatRepository

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        // Crashlytics 초기화 및 기본 로그 설정
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCustomKey("app_start_time", System.currentTimeMillis())
        crashlytics.log("AllInOneChatApp started")

        // 지연 초기화를 통해 앱 시작 시 부하 감소
        syncManager.get().schedulePeriodicSync()
        // 실시간 메시지 관찰 시작
        chatRepository.startObservingMessages()
    }
}
