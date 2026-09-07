package com.example.all_in_one_chat.ui.media.navigation

object MediaNavigationRoutes {
    const val MEDIA_GALLERY = "media/gallery"
    const val MEDIA_DETAIL = "media/detail/{serviceType}/{messageId}"

    fun mediaDetailRoute(serviceType: String, messageId: String): String {
        return "media/detail/$serviceType/$messageId"
    }
}
