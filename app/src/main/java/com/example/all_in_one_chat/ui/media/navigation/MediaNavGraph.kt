package com.example.all_in_one_chat.ui.media.navigation

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.all_in_one_chat.ui.media.MediaDetailScreen
import com.example.all_in_one_chat.ui.media.MediaGalleryScreen

@OptIn(UnstableApi::class)
fun NavGraphBuilder.mediaNavGraph(
    navController: NavController
) {
    composable(MediaNavigationRoutes.MEDIA_GALLERY) {
        MediaGalleryScreen(
            onMediaClick = { serviceType, messageId ->
                navController.navigate(MediaNavigationRoutes.mediaDetailRoute(serviceType, messageId))
            }
        )
    }

    composable(MediaNavigationRoutes.MEDIA_DETAIL) {
        MediaDetailScreen(
            onBackClick = { navController.popBackStack() }
        )
    }
}
