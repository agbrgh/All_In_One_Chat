package com.example.all_in_one_chat.ui.timeline.navigation

/**
 * Timeline navigation routes
 */
object TimelineNavigationRoutes {
    const val TIMELINE = "timeline"
    const val NOTIFICATION_DETAIL = "timeline/notification/{notificationId}"
    const val SEARCH = "timeline/search"

    fun notificationDetail(notificationId: Int): String = "timeline/notification/$notificationId"
}

