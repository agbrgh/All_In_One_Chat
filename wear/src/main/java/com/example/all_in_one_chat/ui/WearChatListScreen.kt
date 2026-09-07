package com.example.all_in_one_chat.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.ScreenScaffold
import com.example.all_in_one_chat.domain.model.ChatMessage
import com.example.all_in_one_chat.domain.model.ChatRoom
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.model.User

@Composable
fun WearChatListScreen(
    rooms: List<ChatRoom>,
    onRoomClick: (String) -> Unit
) {
    ScreenScaffold {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(rooms) { room ->
                Button(
                    onClick = { onRoomClick(room.roomId) },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(text = room.roomName)
                }
            }
        }
    }
}

@Preview
@Composable
fun WearChatListScreenPreview() {
    val sampleRooms = listOf(
        ChatRoom(
            roomId = "1",
            serviceType = ChatServiceType.TELEGRAM,
            roomName = "홍길동",
            lastMessage = ChatMessage(
                messageId = "m1",
                roomId = "1",
                sender = User("u1", ChatServiceType.TELEGRAM, "su1", "홍길동"),
                content = "오늘 뭐해?",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 5,
                serviceType = ChatServiceType.TELEGRAM,
                isFromMe = false
            ),
            unreadCount = 2
        ),
        ChatRoom(
            roomId = "2",
            serviceType = ChatServiceType.DISCORD,
            roomName = "Android Dev",
            lastMessage = ChatMessage(
                messageId = "m2",
                roomId = "2",
                sender = User("u2", ChatServiceType.DISCORD, "su2", "김철수"),
                content = "새로운 Compose 버전이 나왔네요!",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
                serviceType = ChatServiceType.DISCORD,
                isFromMe = false
            ),
            unreadCount = 0
        )
    )

    MaterialTheme {
        WearChatListScreen(
            rooms = sampleRooms,
            onRoomClick = {}
        )
    }
}
