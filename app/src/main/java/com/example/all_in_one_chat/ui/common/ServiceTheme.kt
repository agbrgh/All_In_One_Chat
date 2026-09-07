package com.example.all_in_one_chat.ui.common

import androidx.compose.ui.graphics.Color
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.model.NotificationData
import com.example.all_in_one_chat.ui.theme.*

/**
 * Returns the brand color for a specific chat service
 */
fun getServiceColor(serviceType: ChatServiceType): Color {
    return Color(serviceType.brandColor)
}

/**
 * Returns the brand color for a specific notification app type
 */
fun getAppColor(appType: NotificationData.AppType): Color {
    return when (appType) {
        NotificationData.AppType.TELEGRAM -> Color(ChatServiceType.TELEGRAM.brandColor)
        NotificationData.AppType.DISCORD -> Color(ChatServiceType.DISCORD.brandColor)
        NotificationData.AppType.WHATSAPP -> Color(ChatServiceType.WHATSAPP.brandColor)
        NotificationData.AppType.KAKAOTALK -> Color(ChatServiceType.KAKAOTALK.brandColor)
        NotificationData.AppType.LINE -> Color(ChatServiceType.LINE.brandColor)
        NotificationData.AppType.MESSENGER -> Color(0xFF00B2FF)
        else -> BrandPrimary
    }
}

/**
 * Returns a readable text color to be used on top of the service color
 */
fun getOnServiceColor(serviceType: ChatServiceType): Color {
    return when (serviceType) {
        ChatServiceType.KAKAOTALK -> Color.Black
        else -> Color.White
    }
}

/**
 * Returns a readable text color to be used on top of the app color
 */
fun getOnAppColor(appType: NotificationData.AppType): Color {
    return when (appType) {
        NotificationData.AppType.KAKAOTALK -> Color.Black
        else -> Color.White
    }
}
