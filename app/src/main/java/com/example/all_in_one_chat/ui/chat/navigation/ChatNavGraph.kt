package com.example.all_in_one_chat.ui.chat.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.ui.chat.list.ChatListScreen
import com.example.all_in_one_chat.ui.chat.room.ChatRoomScreen

fun NavGraphBuilder.chatNavGraph(
    navController: NavController,
    onProfileClick: () -> Unit
) {
    composable(ChatNavigationRoutes.CHAT_LIST) {
        ChatListScreen(
            onRoomClick = { serviceType, roomId ->
                navController.navigate(ChatNavigationRoutes.chatRoomRoute(serviceType.name, roomId))
            },
            onProfileClick = onProfileClick
        )
    }

    composable(
        route = ChatNavigationRoutes.CHAT_ROOM,
        arguments = listOf(
            navArgument("serviceType") { type = NavType.StringType },
            navArgument("roomId") { type = NavType.StringType }
        ),
        deepLinks = listOf(
            navDeepLink { uriPattern = ChatNavigationRoutes.CHAT_ROOM_DEEP_LINK }
        )
    ) { backStackEntry ->
        val serviceTypeStr = backStackEntry.arguments?.getString("serviceType") ?: ""
        val serviceType = ChatServiceType.valueOf(serviceTypeStr)
        val roomId = backStackEntry.arguments?.getString("roomId") ?: ""

        ChatRoomScreen(
            serviceType = serviceType,
            roomId = roomId,
            onBackClick = { navController.popBackStack() }
        )
    }
}
