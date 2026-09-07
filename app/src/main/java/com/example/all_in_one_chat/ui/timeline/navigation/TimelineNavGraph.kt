package com.example.all_in_one_chat.ui.timeline.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.all_in_one_chat.ui.chat.navigation.ChatNavigationRoutes
import com.example.all_in_one_chat.ui.search.SearchScreen
import com.example.all_in_one_chat.ui.timeline.TimelineScreen

/**
 * Timeline navigation graph builder
 */
fun NavGraphBuilder.timelineNavGraph(
    navController: NavController,
    onProfileClick: () -> Unit
) {
    composable(TimelineNavigationRoutes.TIMELINE) {
        TimelineScreen(
            onProfileClick = onProfileClick,
            onSearchClick = {
                navController.navigate(TimelineNavigationRoutes.SEARCH)
            }
        )
    }

    composable(TimelineNavigationRoutes.SEARCH) {
        SearchScreen(
            onBackClick = { navController.popBackStack() },
            onNavigateToChat = { serviceType, roomId ->
                navController.navigate(ChatNavigationRoutes.chatRoomRoute(serviceType, roomId))
            }
        )
    }
}

