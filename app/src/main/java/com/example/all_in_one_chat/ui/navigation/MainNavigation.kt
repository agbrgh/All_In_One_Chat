package com.example.all_in_one_chat.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.all_in_one_chat.ui.auth.navigation.AuthNavigationRoutes
import com.example.all_in_one_chat.ui.chat.navigation.ChatNavigationRoutes
import com.example.all_in_one_chat.ui.media.navigation.MediaNavigationRoutes
import com.example.all_in_one_chat.ui.timeline.navigation.TimelineNavigationRoutes

sealed class MainNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Chats : MainNavItem(
        route = ChatNavigationRoutes.CHAT_LIST,
        icon = Icons.AutoMirrored.Filled.Chat,
        label = "Chats"
    )

    object Timeline : MainNavItem(
        route = TimelineNavigationRoutes.TIMELINE,
        icon = Icons.Default.History,
        label = "Timeline"
    )

    object Media : MainNavItem(
        route = MediaNavigationRoutes.MEDIA_GALLERY,
        icon = Icons.Default.PermMedia,
        label = "Media"
    )

    object Profile : MainNavItem(
        route = AuthNavigationRoutes.PROFILE,
        icon = Icons.Default.Person,
        label = "Profile"
    )
}

val mainNavItems = listOf(
    MainNavItem.Chats,
    MainNavItem.Timeline,
    MainNavItem.Media,
    MainNavItem.Profile
)
