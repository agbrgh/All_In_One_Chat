package com.example.all_in_one_chat.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material3.*
import com.example.all_in_one_chat.domain.model.ChatMessage
import com.example.all_in_one_chat.domain.model.ChatRoom
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.model.User

@Composable
fun WearChatRoomScreen(
    room: ChatRoom?,
    onBack: () -> Unit
) {
    ScreenScaffold {
        if (room == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "채팅방을 찾을 수 없습니다.")
            }
        } else {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = room.roomName,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                room.lastMessage?.let { message ->
                    item {
                        Card(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = message.sender.userName,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(text = message.content)
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = { /* TODO: Implement reply action */ },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(text = "답장하기")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun WearChatRoomScreenPreview() {
    val sampleRoom = ChatRoom(
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
    )

    MaterialTheme {
        WearChatRoomScreen(
            room = sampleRoom,
            onBack = {}
        )
    }
}
