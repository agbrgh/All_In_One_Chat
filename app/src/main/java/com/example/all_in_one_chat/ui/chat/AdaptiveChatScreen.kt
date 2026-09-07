package com.example.all_in_one_chat.ui.chat

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.ui.chat.list.ChatListScreen
import com.example.all_in_one_chat.ui.chat.room.ChatRoomScreen
import com.example.all_in_one_chat.ui.theme.All_In_One_ChatTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AdaptiveChatScreen(
    onProfileClick: () -> Unit
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<ChatRoomArgs>()
    val scope = rememberCoroutineScope()

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            ChatListScreen(
                onRoomClick = { serviceType, roomId ->
                    scope.launch {
                        navigator.navigateTo(
                            pane = ListDetailPaneScaffoldRole.Detail,
                            contentKey = ChatRoomArgs(serviceType.name, roomId)
                        )
                    }
                },
                onProfileClick = onProfileClick
            )
        },
        detailPane = {
            val args = navigator.currentDestination?.contentKey
            if (args != null) {
                ChatRoomScreen(
                    serviceType = ChatServiceType.valueOf(args.serviceType),
                    roomId = args.roomId,
                    onBackClick = {
                        scope.launch {
                            navigator.navigateBack()
                        }
                    }
                )
            }
        }
    )
}

data class ChatRoomArgs(
    val serviceType: String,
    val roomId: String
)

@Preview(showBackground = true)
@Composable
fun AdaptiveChatScreenPreview() {
    All_In_One_ChatTheme {
        AdaptiveChatScreen(
            onProfileClick = {}
        )
    }
}
