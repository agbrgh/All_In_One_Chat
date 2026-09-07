package com.example.all_in_one_chat.service.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.example.all_in_one_chat.MainActivity
import com.example.all_in_one_chat.R
import com.example.all_in_one_chat.domain.model.ChatMessage
import com.example.all_in_one_chat.domain.model.ChatServiceType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val GROUP_KEY_PREFIX = "com.example.all_in_one_chat.GROUP_"
        const val KEY_TEXT_REPLY = "key_text_reply"
        const val ACTION_REPLY = "com.example.all_in_one_chat.ACTION_REPLY"
        const val EXTRA_ROOM_ID = "extra_room_id"
        const val EXTRA_SERVICE_TYPE = "extra_service_type"
        const val CHANNEL_SERVICE = "service_channel"
        const val NOTIFICATION_ID_SERVICE = 1001
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Service channel (Foreground)
            val serviceChannel = NotificationChannel(
                CHANNEL_SERVICE,
                "앱 실행 유지 서비스",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "안정적인 메시지 수신을 위해 앱의 백그라운드 실행을 유지합니다."
            }
            notificationManager.createNotificationChannel(serviceChannel)

            ChatServiceType.entries.forEach { serviceType ->
                val channel = NotificationChannel(
                    serviceType.name,
                    serviceType.displayName,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "${serviceType.displayName} messages notification channel"
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    /**
     * Sends a reply to an external notification using RemoteInput
     */
    fun sendReply(sbn: StatusBarNotification, message: String): Boolean {
        val notification = sbn.notification
        val actions = notification.actions ?: return false

        for (action in actions) {
            val remoteInputs = action.remoteInputs ?: continue
            for (remoteInput in remoteInputs) {
                // Check if this action is a reply action (semanticAction is supported from API 28)
                val isReply = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    action.semanticAction == Notification.Action.SEMANTIC_ACTION_REPLY
                } else {
                    action.title?.toString()?.contains("reply", ignoreCase = true) == true ||
                    action.title?.toString()?.contains("답장", ignoreCase = true) == true
                }

                if (isReply) {
                    val resultKey = remoteInput.resultKey
                    val results = android.os.Bundle().apply {
                        putCharSequence(resultKey, message)
                    }

                    val intent = Intent()
                    android.app.RemoteInput.addResultsToIntent(
                        action.remoteInputs,
                        intent,
                        results
                    )

                    try {
                        action.actionIntent.send(context, 0, intent)
                        return true
                    } catch (e: PendingIntent.CanceledException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return false
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNotification(message: ChatMessage) {
        val groupKey = GROUP_KEY_PREFIX + message.serviceType.name + "_" + message.roomId
        
        // Direct Reply Action
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel("답장하기")
            .build()

        val replyIntent = Intent(context, ReplyReceiver::class.java).apply {
            action = ACTION_REPLY
            putExtra(EXTRA_ROOM_ID, message.roomId)
            putExtra(EXTRA_SERVICE_TYPE, message.serviceType.name)
        }

        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            message.roomId.hashCode(),
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val replyAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send,
            "답장",
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()

        // Content Intent (Open App)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, message.serviceType.name)
            .setSmallIcon(R.drawable.ic_notification) // Ensure this exists or use a default
            .setContentTitle(message.sender.userName)
            .setContentText(message.content)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setGroup(groupKey)
            .setContentIntent(pendingIntent)
            .addAction(replyAction)

        notificationManager.notify(message.messageId.hashCode(), builder.build())

        // Summary Notification
        val summaryNotification = NotificationCompat.Builder(context, message.serviceType.name)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(message.serviceType.displayName)
            .setContentText("새로운 메시지가 있습니다.")
            .setGroup(groupKey)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(groupKey.hashCode(), summaryNotification)
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    /**
     * Create notification for Foreground Service
     */
    fun createServiceNotification(): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_SERVICE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("All-In-One Chat")
            .setContentText("실시간 메시지 수신 서비스가 실행 중입니다.")
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /**
     * Checks if the app is ignored from battery optimizations
     */
    fun isBatteryOptimizationIgnored(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
    }

    /**
     * Checks if the NotificationListenerService permission is granted for this app
     */
    fun isNotificationListenerEnabled(): Boolean {
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(context)
        return packageNames.contains(context.packageName)
    }
}
