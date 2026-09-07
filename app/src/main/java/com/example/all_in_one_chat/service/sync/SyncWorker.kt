package com.example.all_in_one_chat.service.sync

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.WorkRequest
import com.example.all_in_one_chat.data.local.prefs.SecurePreferences
import com.example.all_in_one_chat.domain.repository.ChatRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val chatRepository: ChatRepository,
    private val securePreferences: SecurePreferences
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 저전력 모드이고 배터리가 낮으면 중요도가 낮은 동기화 건너뜀
            if (isPowerSaveMode() && isBatteryLow()) {
                // 완전히 건너뛰기보다는 간격을 벌림
                return@withContext Result.success()
            }

            // 모든 서비스의 채팅방 목록 갱신
            chatRepository.refreshChatRooms()
            
            // 실시간 메시지 수신 시작
            chatRepository.startObservingMessages()

            // 다음 주기 스케줄링 (15분 미만 설정 시)
            val intervalMinutes = securePreferences.getString(SecurePreferences.KEY_SYNC_INTERVAL)?.toLongOrNull() ?: 15L
            if (intervalMinutes < 15) {
                scheduleNextSync(intervalMinutes.coerceAtLeast(5L))
            }

            Result.success()
        } catch (e: Exception) {
            // 네트워크 오류 등 일시적 문제일 경우 retry
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private fun isPowerSaveMode(): Boolean {
        val powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isPowerSaveMode
    }

    private fun isBatteryLow(): Boolean {
        val intent = applicationContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return level * 100 / scale.toFloat() < 20
    }

    private fun scheduleNextSync(intervalMinutes: Long) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val nextRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInitialDelay(intervalMinutes, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag(WORK_NAME + "_loop")
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            WORK_NAME + "_loop",
            androidx.work.ExistingWorkPolicy.REPLACE,
            nextRequest
        )
    }

    companion object {
        const val WORK_NAME = "SyncWorker"
    }
}
