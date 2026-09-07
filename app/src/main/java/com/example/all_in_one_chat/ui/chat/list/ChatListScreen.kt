package com.example.all_in_one_chat.ui.chat.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.all_in_one_chat.domain.model.ChatRoom
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.model.ChatMessage
import com.example.all_in_one_chat.domain.model.User
import com.example.all_in_one_chat.ui.common.ServiceBadge
import com.example.all_in_one_chat.ui.theme.All_In_One_ChatTheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ChatListScreen(
    onRoomClick: (ChatServiceType, String) -> Unit,
    onProfileClick: () -> Unit,
    viewModel: ChatListViewModel? = if (LocalInspectionMode.current) null else hiltViewModel()
) {
    if (LocalInspectionMode.current) {
        ChatListScreenContent(
            viewState = ChatListViewState(rooms = emptyList()),
            onRoomClick = onRoomClick,
            onProfileClick = onProfileClick,
            onArchiveRoom = { _, _ -> },
            onDeleteRoom = { _, _ -> },
            onToggleMuteRoom = { _, _, _ -> }
        )
    } else {
        val viewState by viewModel!!.viewState.collectAsState()
        val viewEffect = viewModel.viewEffect

        LaunchedEffect(Unit) {
            viewEffect.collect { effect ->
                when (effect) {
                    is ChatListEffect.NavigateToRoom -> {
                        onRoomClick(effect.serviceType, effect.roomId)
                    }
                    is ChatListEffect.ShowError -> {
                        // Show snackbar or similar
                    }
                }
            }
        }

        ChatListScreenContent(
            viewState = viewState,
            onRoomClick = { serviceType, roomId ->
                viewModel.sendIntent(ChatListIntent.RoomClicked(serviceType, roomId))
            },
            onProfileClick = onProfileClick,
            onArchiveRoom = { serviceType, roomId ->
                viewModel.sendIntent(ChatListIntent.ArchiveRoom(serviceType, roomId))
            },
            onDeleteRoom = { serviceType, roomId ->
                viewModel.sendIntent(ChatListIntent.DeleteRoom(serviceType, roomId))
            },
            onToggleMuteRoom = { serviceType, roomId, isMuted ->
                viewModel.sendIntent(ChatListIntent.ToggleMuteRoom(serviceType, roomId, isMuted))
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreenContent(
    viewState: ChatListViewState,
    onRoomClick: (ChatServiceType, String) -> Unit,
    onProfileClick: () -> Unit,
    onArchiveRoom: (ChatServiceType, String) -> Unit,
    onDeleteRoom: (ChatServiceType, String) -> Unit,
    onToggleMuteRoom: (ChatServiceType, String, Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("대화", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            if (viewState.isLoading && viewState.rooms.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
            } else if (viewState.rooms.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "아직 대화가 없습니다.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onProfileClick) {
                        Text("서비스 연결하기")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(viewState.rooms, key = { "${it.serviceType}_${it.roomId}" }) { room ->
                        val dismissState = rememberSwipeToDismissBoxState()

                        LaunchedEffect(dismissState.currentValue) {
                            when (dismissState.currentValue) {
                                SwipeToDismissBoxValue.StartToEnd -> {
                                    onArchiveRoom(room.serviceType, room.roomId)
                                    dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                                }
                                SwipeToDismissBoxValue.EndToStart -> {
                                    onDeleteRoom(room.serviceType, room.roomId)
                                    dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                                }
                                else -> {}
                            }
                        }

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                SwipeBackground(dismissState)
                            },
                            enableDismissFromStartToEnd = true,
                            enableDismissFromEndToStart = true
                        ) {
                            ChatRoomItem(
                                room = room,
                                onClick = { onRoomClick(room.serviceType, room.roomId) },
                                onToggleMute = { isMuted ->
                                    onToggleMuteRoom(room.serviceType, room.roomId, isMuted)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatRoomItem(
    room: ChatRoom,
    onClick: () -> Unit,
    onToggleMute: (Boolean) -> Unit
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { onToggleMute(!room.isMuted) }
            )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image with prominent service badge
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(20.dp)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                // More prominent badge
                Box(modifier = Modifier.padding(2.dp)) {
                    ServiceBadge(serviceType = room.serviceType, isSmall = true)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = room.roomName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (room.isMuted) {
                        Icon(
                            Icons.Default.NotificationsOff,
                            contentDescription = "Muted",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp).padding(start = 4.dp)
                        )
                    }
                    Text(
                        text = formatTime(room.lastMessage?.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = room.lastMessage?.content ?: "대화 내용 없음",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (room.unreadCount > 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = room.unreadCount.toString(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long?): String {
    if (timestamp == null) return ""
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val oneDay = 86400000L
    
    return when {
        diff < 60000 -> "방금"
        diff < 3600000 -> "${diff / 60000}분 전"
        diff < oneDay -> "${diff / 3600000}시간 전"
        diff < oneDay * 2 -> "어제"
        diff < oneDay * 7 -> "${diff / oneDay}일 전"
        else -> {
            val sdf = java.text.SimpleDateFormat("M월 d일", java.util.Locale.KOREA)
            sdf.format(java.util.Date(timestamp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeBackground(dismissState: SwipeToDismissBoxState) {
    val color = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50) // Archive - Green
        SwipeToDismissBoxValue.EndToStart -> Color(0xFFF44336) // Delete - Red
        SwipeToDismissBoxValue.Settled -> Color.Transparent
    }
    val alignment = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        SwipeToDismissBoxValue.Settled -> Alignment.Center
    }
    val icon = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Archive
        SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
        SwipeToDismissBoxValue.Settled -> null
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 20.dp),
        contentAlignment = alignment
    ) {
        icon?.let {
            Icon(
                it,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatListScreenPreview() {
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
        ),
        ChatRoom(
            roomId = "3",
            serviceType = ChatServiceType.SLACK,
            roomName = "Workspace",
            lastMessage = ChatMessage(
                messageId = "m3",
                roomId = "3",
                sender = User("u3", ChatServiceType.SLACK, "su3", "이영희"),
                content = "회의 10분 전입니다.",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24,
                serviceType = ChatServiceType.SLACK,
                isFromMe = false
            ),
            unreadCount = 5
        )
    )

    All_In_One_ChatTheme {
        ChatListScreenContent(
            viewState = ChatListViewState(rooms = sampleRooms),
            onRoomClick = { _, _ -> },
            onProfileClick = {},
            onArchiveRoom = { _, _ -> },
            onDeleteRoom = { _, _ -> },
            onToggleMuteRoom = { _, _, _ -> }
        )
    }
}
