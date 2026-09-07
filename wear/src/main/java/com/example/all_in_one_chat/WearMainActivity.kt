package com.example.all_in_one_chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.all_in_one_chat.service.WearDataLayerService
import com.example.all_in_one_chat.ui.WearChatListScreen
import com.example.all_in_one_chat.ui.WearChatRoomScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WearMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberSwipeDismissableNavController()
                val rooms by WearDataLayerService.chatRooms.collectAsState()

                SwipeDismissableNavHost(
                    navController = navController,
                    startDestination = "chat_list"
                ) {
                    composable("chat_list") {
                        WearChatListScreen(
                            rooms = rooms,
                            onRoomClick = { roomId ->
                                navController.navigate("chat_room/$roomId")
                            }
                        )
                    }
                    composable("chat_room/{roomId}") { backStackEntry ->
                        val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
                        val room = rooms.find { it.roomId == roomId }
                        WearChatRoomScreen(
                            room = room,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
