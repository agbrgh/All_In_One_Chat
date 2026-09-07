package com.example.all_in_one_chat.service

import android.app.Notification
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.all_in_one_chat.domain.model.NotificationData
import com.example.all_in_one_chat.service.notification.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Service to capture and parse system notifications
 * 
 * This service listens to system notifications and extracts relevant data
 * from apps like KakaoTalk, LINE, Telegram, etc.
 */
@AndroidEntryPoint
class AllInOneChatNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationListener"
    }

    @Inject
    lateinit var notificationBridge: NotificationBridge

    @Inject
    lateinit var notificationHelper: NotificationHelper

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        return START_STICKY
    }

    private fun startForegroundService() {
        val notification = notificationHelper.createServiceNotification()
        startForeground(NotificationHelper.NOTIFICATION_ID_SERVICE, notification)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification Listener connected")
        startForegroundService()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Notification Listener disconnected - attempting to rebind")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestRebind(android.content.ComponentName(this, AllInOneChatNotificationListenerService::class.java))
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "Service Bound")
        return super.onBind(intent)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let { notification ->
            try {
                val notificationData = parseNotification(notification)
                scope.launch {
                    notificationBridge.publishNotification(notificationData)
                }
                Log.d(TAG, "Notification captured: ${notification.packageName}")
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing notification", e)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        sbn?.let {
            Log.d(TAG, "Notification removed: ${it.packageName}")
        }
    }

    /**
     * Parse notification and extract relevant data
     * Support for KakaoTalk, LINE, Telegram, and other chat apps
     */
    @RequiresApi(Build.VERSION_CODES.P)
    private fun parseNotification(sbn: StatusBarNotification): NotificationData {
        val notification = sbn.notification
        val packageName = sbn.packageName
        val key = sbn.key
        val postTime = sbn.postTime

        // Extract text from notification
        val title = notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = notification.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val subText = notification.extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        
        // Extract big text if available
        val bigText = notification.extras.getCharSequence("android.bigText")?.toString()
            ?: text

        // Extract actions (for reply capability)
        val actions = mutableListOf<NotificationData.Action>()
        notification.actions?.forEach { action ->
            if (action != null) {
                actions.add(
                    NotificationData.Action(
                        title = action.title?.toString() ?: "",
                        semanticAction = action.semanticAction
                    )
                )
            }
        }

        // Determine app type based on package name
        val appType = determineAppType(packageName)

        return NotificationData(
            id = key.hashCode(),
            packageName = packageName,
            appType = appType,
            title = title ?: "",
            message = text ?: "",
            subText = subText,
            bigText = bigText,
            timestamp = postTime,
            sbn = sbn,
            actions = actions,
            isRemovable = sbn.isClearable
        )
    }

    /**
     * Determine the type of app from package name
     */
    private fun determineAppType(packageName: String): NotificationData.AppType {
        return when {
            packageName.contains("kakaotalk", ignoreCase = true) -> NotificationData.AppType.KAKAOTALK
            packageName.contains("line", ignoreCase = true) -> NotificationData.AppType.LINE
            packageName.contains("telegram", ignoreCase = true) -> NotificationData.AppType.TELEGRAM
            packageName.contains("whatsapp", ignoreCase = true) -> NotificationData.AppType.WHATSAPP
            packageName.contains("discord", ignoreCase = true) -> NotificationData.AppType.DISCORD
            packageName.contains("messenger", ignoreCase = true) -> NotificationData.AppType.MESSENGER
            packageName.contains("wechat", ignoreCase = true) -> NotificationData.AppType.WECHAT
            packageName.contains("viber", ignoreCase = true) -> NotificationData.AppType.VIBER
            else -> NotificationData.AppType.OTHER
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun CoroutineScope.cancel() {
        this.coroutineContext.cancel()
    }
}
