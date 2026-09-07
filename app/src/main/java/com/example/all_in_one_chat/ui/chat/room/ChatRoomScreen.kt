package com.example.all_in_one_chat.ui.chat.room

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.ListItem
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.all_in_one_chat.domain.model.ChatMessage
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.model.MediaType
import com.example.all_in_one_chat.domain.model.User
import com.example.all_in_one_chat.domain.model.ChatRoom
import com.example.all_in_one_chat.ui.common.ServiceBadge
import com.example.all_in_one_chat.ui.common.getServiceColor
import com.example.all_in_one_chat.ui.theme.All_In_One_ChatTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.all_in_one_chat.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    serviceType: ChatServiceType,
    roomId: String,
    onBackClick: () -> Unit,
    viewModel: ChatRoomViewModel? = if (LocalInspectionMode.current) null else hiltViewModel(),
) {
    if (LocalInspectionMode.current) {
        val sampleUser = remember {
            User(
                id = "u1",
                serviceType = serviceType,
                serviceUserId = "su1",
                userName = "홍길동"
            )
        }
        val sampleMessages = remember {
            listOf(
                ChatMessage(
                    messageId = "m1",
                    roomId = roomId,
                    sender = sampleUser,
                    content = "안녕하세요! 프리뷰 모드입니다.",
                    timestamp = System.currentTimeMillis() - 60000,
                    serviceType = serviceType,
                    isFromMe = false
                ),
                ChatMessage(
                    messageId = "m2",
                    roomId = roomId,
                    sender = sampleUser,
                    content = "[Photo]",
                    timestamp = System.currentTimeMillis(),
                    serviceType = serviceType,
                    isFromMe = false,
                    mediaType = MediaType.IMAGE,
                    mediaLocalPath = "sample_coffee"
                )
            )
        }
        ChatRoomContent(
            serviceType = serviceType,
            viewState = ChatRoomViewState(
                roomName = "Preview Room (${serviceType.name})",
                serviceType = serviceType,
                messages = sampleMessages
            ),
            onBackClick = onBackClick,
            onIntent = { }
        )
    } else {
        val viewState by viewModel!!.viewState.collectAsState()

        LaunchedEffect(serviceType, roomId) {
            viewModel.sendIntent(ChatRoomIntent.LoadMessages(serviceType, roomId))
        }

        ChatRoomContent(
            serviceType = serviceType,
            viewState = viewState,
            onBackClick = onBackClick,
            onIntent = { intent ->
                viewModel.sendIntent(intent)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomContent(
    serviceType: ChatServiceType,
    viewState: ChatRoomViewState,
    onBackClick: () -> Unit,
    onIntent: (ChatRoomIntent) -> Unit
) {
    var textState by remember { mutableStateOf("") }
    var forwardingMessage by remember { mutableStateOf<ChatMessage?>(null) }

    if (forwardingMessage != null) {
        ForwardDialog(
            rooms = viewState.allRooms,
            onDismiss = { forwardingMessage = null },
            onRoomSelected = { room ->
                onIntent(ChatRoomIntent.ForwardMessage(forwardingMessage!!, room.roomId, room.serviceType))
                forwardingMessage = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = viewState.roomName,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ServiceBadge(serviceType = serviceType, isSmall = true)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = serviceType.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = getServiceColor(serviceType).copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = getServiceColor(serviceType).copy(alpha = 0.1f),
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = getServiceColor(serviceType)
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.navigationBarsPadding()) {
                    if (viewState.replyTargetMessage != null) {
                        ReplyPreview(
                            message = viewState.replyTargetMessage,
                            onCancel = { onIntent(ChatRoomIntent.SetReplyTarget(null)) }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textState,
                            onValueChange = { textState = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            placeholder = { Text("메시지 입력...") },
                            maxLines = 4,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                        FloatingActionButton(
                            onClick = {
                                if (textState.isNotBlank()) {
                                    onIntent(ChatRoomIntent.SendMessage(textState))
                                    textState = ""
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            reverseLayout = true,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewState.messages.reversed()) { message ->
                MessageBubble(
                    message = message,
                    isMe = message.isFromMe,
                    onIntent = onIntent,
                    onForwardTriggered = { forwardingMessage = it }
                )
            }
        }
    }
}

@Composable
fun ReplyPreview(message: ChatMessage, onCancel: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Reply,
                contentDescription = "Replying to",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message.sender.userName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onCancel, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
@SuppressLint("DiscouragedApi", "LocalContextResourcesRead")
fun MessageBubble(
    message: ChatMessage,
    isMe: Boolean,
    onIntent: (ChatRoomIntent) -> Unit,
    onForwardTriggered: (ChatMessage) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    val serviceColor = getServiceColor(message.serviceType)
    
    val bgColor = if (isMe) {
        MaterialTheme.colorScheme.primary
    } else {
        serviceColor.copy(alpha = 0.15f)
    }
    
    val textColor = if (isMe) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    val shape = if (isMe) {
        RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
    }

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), contentAlignment = alignment) {
        Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
            if (!isMe) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = message.sender.userName,
                        style = MaterialTheme.typography.labelSmall,
                        color = serviceColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    ServiceBadge(serviceType = message.serviceType, isSmall = true)
                }
            }
            Surface(
                color = bgColor,
                shape = shape,
                shadowElevation = 1.dp,
                modifier = Modifier.combinedClickable(
                    onClick = { /* 이미지 확대 등 가능 */ },
                    onLongClick = { showMenu = true }
                )
            ) {
                Column {
                    // 답장 정보 표시
                    if (message.replyToMessageId != null) {
                        ReplyInfo(
                            senderName = message.replyToMessageSender ?: "알 수 없음",
                            content = message.replyToMessageContent ?: "",
                            isMe = isMe
                        )
                    }
                    
                    if ((message.mediaType == MediaType.IMAGE) && (message.mediaLocalPath != null)) {
                        val isInspectionMode = LocalInspectionMode.current
                        if (isInspectionMode) {
                            Image(
                                painter = painterResource(id = R.drawable.sample_coffee),
                                contentDescription = "Preview Image",
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(150.dp)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val context = LocalContext.current
                            val imageModel = remember(message.mediaLocalPath) {
                                val path = message.mediaLocalPath
                                if (path.startsWith("http")) {
                                    path
                                } else if (!path.contains("/") && !path.contains("\\")) {
                                    val resName = path.substringBeforeLast(".")
                                    val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                                    if (resId != 0) resId else "android.resource://${context.packageName}/drawable/$resName"
                                } else {
                                    File(path)
                                }
                            }
                            AsyncImage(
                                model = imageModel,
                                contentDescription = "Image message",
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else if (message.mediaType == MediaType.VIDEO) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .padding(4.dp)
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Video placeholder",
                                modifier = Modifier.size(48.dp),
                                tint = textColor.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    if (message.content.isNotEmpty() && message.content != "[Photo]") {
                        Text(
                            text = message.content,
                            color = textColor,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            fontSize = 15.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("답장") },
                        onClick = {
                            showMenu = false
                            onIntent(ChatRoomIntent.SetReplyTarget(message))
                        },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("전달") },
                        onClick = {
                            showMenu = false
                            onForwardTriggered(message)
                        },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Forward, contentDescription = null) }
                    )
                    if (isMe) {
                        DropdownMenuItem(
                            text = { Text("삭제") },
                            onClick = {
                                showMenu = false
                                onIntent(ChatRoomIntent.DeleteMessage(message))
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                        )
                    }
                }
            }
            Text(
                text = SimpleDateFormat("a h:mm", Locale.KOREA).format(Date(message.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
            )
        }
    }
}

@Composable
fun ForwardDialog(
    rooms: List<ChatRoom>,
    onDismiss: () -> Unit,
    onRoomSelected: (ChatRoom) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("전달할 채팅방 선택") },
        text = {
            Box(modifier = Modifier.height(300.dp)) {
                LazyColumn {
                    items(rooms) { room ->
                        ListItem(
                            headlineContent = { Text(room.roomName) },
                            leadingContent = { ServiceBadge(serviceType = room.serviceType, isSmall = true) },
                            modifier = Modifier.clickable { onRoomSelected(room) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
fun ReplyInfo(senderName: String, content: String, isMe: Boolean) {
    val bgColor = if (isMe) {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    }
    val barColor = if (isMe) {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.primary
    }
    val textColor = if (isMe) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp, top = 8.dp)
            .fillMaxWidth()
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(barColor)
            )
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(
                    text = senderName,
                    style = MaterialTheme.typography.labelSmall,
                    color = barColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun ChatRoomScreenPreview() {
    val sampleUser = User(
        id = "u1",
        serviceType = ChatServiceType.TELEGRAM,
        serviceUserId = "su1",
        userName = "홍길동"
    )
    
    val sampleMessages = listOf(
        ChatMessage(
            messageId = "m1",
            roomId = "r1",
            sender = sampleUser,
            content = "안녕하세요!",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 10,
            serviceType = ChatServiceType.TELEGRAM,
            isFromMe = false
        ),
        ChatMessage(
            messageId = "m2",
            roomId = "r1",
            sender = sampleUser,
            content = "네, 반갑습니다. 오늘 날씨가 좋네요.",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 5,
            serviceType = ChatServiceType.TELEGRAM,
            isFromMe = true
        ),
        ChatMessage(
            messageId = "m3",
            roomId = "r1",
            sender = sampleUser,
            content = "그러게요. 산책하기 딱 좋은 날씨예요!",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 1,
            serviceType = ChatServiceType.TELEGRAM,
            isFromMe = false
        ),
        ChatMessage(
            messageId = "m4",
            roomId = "r1",
            sender = sampleUser,
            content = "[Photo]",
            timestamp = System.currentTimeMillis(),
            serviceType = ChatServiceType.TELEGRAM,
            isFromMe = false,
            mediaType = MediaType.IMAGE,
            mediaLocalPath = "sample_coffee"
        )
    )

    val sampleViewState = ChatRoomViewState(
        messages = sampleMessages,
        roomName = "홍길동",
        serviceType = ChatServiceType.TELEGRAM
    )

    All_In_One_ChatTheme {
        ChatRoomContent(
            serviceType = ChatServiceType.TELEGRAM,
            viewState = sampleViewState,
            onBackClick = {},
            onIntent = {}
        )
    }
}
