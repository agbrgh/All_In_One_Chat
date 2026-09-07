package com.example.all_in_one_chat.ui.chat.navigation

object ChatNavigationRoutes {
    const val CHAT_LIST = "chat/list"
    const val CHAT_ROOM = "chat/room/{serviceType}/{roomId}"
    const val CHAT_ROOM_DEEP_LINK = "all-in-one-chat://chat/{serviceType}/{roomId}"
    
    fun chatRoomRoute(serviceType: String, roomId: String): String {
        return "chat/room/$serviceType/$roomId"
    }
}
