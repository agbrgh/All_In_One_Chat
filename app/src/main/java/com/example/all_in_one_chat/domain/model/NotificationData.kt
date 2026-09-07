package com.example.all_in_one_chat.domain.model

import android.service.notification.StatusBarNotification

/**
 * Model representing a captured notification from any chat app
 */
data class NotificationData(
    val id: Int,
    val packageName: String,
    val appType: AppType,
    val title: String,
    val message: String,
    val subText: String?,
    val bigText: String?,
    val timestamp: Long,
    val sbn: StatusBarNotification?,
    val actions: List<Action> = emptyList(),
    val isRemovable: Boolean = true
) {
    data class Action(
        val title: String,
        val semanticAction: Int = -1
    )

    enum class AppType {
        KAKAOTALK,
        LINE,
        TELEGRAM,
        WHATSAPP,
        DISCORD,
        MESSENGER,
        WECHAT,
        VIBER,
        OTHER;

        fun displayName(): String = when(this) {
            KAKAOTALK -> "카카오톡"
            LINE -> "LINE"
            TELEGRAM -> "텔레그램"
            WHATSAPP -> "WhatsApp"
            DISCORD -> "Discord"
            MESSENGER -> "Messenger"
            WECHAT -> "WeChat"
            VIBER -> "Viber"
            OTHER -> "기타"
        }
    }

    fun displayName(): String = when(appType) {
        AppType.KAKAOTALK -> "카카오톡"
        AppType.LINE -> "LINE"
        AppType.TELEGRAM -> "텔레그램"
        AppType.WHATSAPP -> "WhatsApp"
        AppType.DISCORD -> "Discord"
        AppType.MESSENGER -> "Messenger"
        AppType.WECHAT -> "WeChat"
        AppType.VIBER -> "Viber"
        AppType.OTHER -> "기타"
    }
}
