package com.example.all_in_one_chat.service.sync

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.PowerManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.all_in_one_chat.data.local.prefs.SecurePreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securePreferences: SecurePreferences
) {
    private val workManager = WorkManager.getInstance(context)
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun schedulePeriodicSync(intervalMinutes: Long? = null) {
        scope.launch {
            val baseInterval = intervalMinutes ?: securePreferences.getString(SecurePreferences.KEY_SYNC_INTERVAL)?.toLongOrNull() ?: 15L
            
            // 적응형 주기 계산: 배터리 상태 및 네트워크 타입에 따라 주기 조정
            val adjustedInterval = calculateAdjustedInterval(baseInterval)
            val interval = adjustedInterval.coerceAtLeast(5L) // 최소 5분

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .setRequiresDeviceIdle(isLowPowerMode()) // Only require idle if in low power mode
                .setRequiresStorageNotLow(true)
                .build()

            if (interval < 15) {
                requestImmediateSync()
            }

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                interval.coerceAtLeast(15), TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    androidx.work.BackoffPolicy.EXPONENTIAL,
                    androidx.work.WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            workManager.enqueueUniquePeriodicWork(
                SyncWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                syncRequest
            )
        }
    }

    private fun calculateAdjustedInterval(baseInterval: Long): Long {
        var multiplier = 1.0

        // 1. 네트워크 상태 확인 (모바일 데이터일 경우 주기 연장)
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        if (capabilities != null && !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            multiplier *= 1.5 // Wi-Fi가 아니면 1.5배 연장
        }

        // 2. 배터리 상태 확인
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        if (!isCharging) {
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = level * 100 / scale.toFloat()

            multiplier *= when {
                batteryPct < 15 -> 4.0
                batteryPct < 30 -> 2.0
                else -> 1.0
            }
        }

        // 3. 저전력 모드 확인
        if (isLowPowerMode()) {
            multiplier *= 2.0
        }

        return (baseInterval * multiplier).toLong()
    }

    private fun isLowPowerMode(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isPowerSaveMode
    }

    suspend fun updateSyncInterval(minutes: Long) {
        securePreferences.saveString(SecurePreferences.KEY_SYNC_INTERVAL, minutes.toString())
        schedulePeriodicSync(minutes)
    }

    fun requestImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val immediateRequest = androidx.work.OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .addTag("ImmediateSync")
            .build()

        workManager.enqueueUniqueWork(
            SyncWorker.WORK_NAME + "_immediate",
            androidx.work.ExistingWorkPolicy.REPLACE,
            immediateRequest
        )
    }

    fun cancelSync() {
        workManager.cancelUniqueWork(SyncWorker.WORK_NAME)
    }
}
