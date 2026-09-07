package com.example.all_in_one_chat.ui.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.all_in_one_chat.domain.model.NotificationData
import com.example.all_in_one_chat.ui.common.getAppColor
import com.example.all_in_one_chat.ui.theme.All_In_One_ChatTheme
import com.example.all_in_one_chat.ui.timeline.mvi.TimelineEffect
import com.example.all_in_one_chat.ui.timeline.mvi.TimelineIntent
import com.example.all_in_one_chat.ui.timeline.mvi.TimelineViewModel
import com.example.all_in_one_chat.ui.timeline.mvi.TimelineViewState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel? = if (LocalInspectionMode.current) null else hiltViewModel(),
    onProfileClick: () -> Unit = {},
    onSearchClick: () -> Unit = {}
) {
    if (LocalInspectionMode.current) {
        TimelineScreenContent(
            viewState = TimelineViewState(),
            snackbarHostState = remember { SnackbarHostState() },
            showReplyDialog = false,
            selectedNotification = null,
            replyMessage = "",
            onReplyMessageChange = {},
            onReplyClick = {},
            onReplyDialogDismiss = {},
            onIntent = {},
            onProfileClick = onProfileClick,
            onSearchClick = onSearchClick
        )
    } else {
        val viewState by viewModel!!.viewState.collectAsState()
        val viewEffect = viewModel.viewEffect
        
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        var showReplyDialog by remember { mutableStateOf(false) }
        var selectedNotification by remember { mutableStateOf<NotificationData?>(null) }
        var replyMessage by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            viewModel.sendIntent(TimelineIntent.LoadNotifications)
        }

        LaunchedEffect(Unit) {
            viewEffect.collect { effect ->
                when (effect) {
                    is TimelineEffect.ShowMessage -> {
                        scope.launch {
                            snackbarHostState.showSnackbar(effect.message)
                        }
                    }
                    is TimelineEffect.ShowError -> {
                        scope.launch {
                            snackbarHostState.showSnackbar(effect.error)
                        }
                    }
                    is TimelineEffect.ReplySent -> {
                        showReplyDialog = false
                        replyMessage = ""
                    }
                    is TimelineEffect.ShowReplyDialog -> {
                        selectedNotification = viewState.notifications.find { it.id == effect.notificationId }
                        showReplyDialog = true
                    }
                    else -> {}
                }
            }
        }

        TimelineScreenContent(
            viewState = viewState,
            snackbarHostState = snackbarHostState,
            showReplyDialog = showReplyDialog,
            selectedNotification = selectedNotification,
            replyMessage = replyMessage,
            onReplyMessageChange = { replyMessage = it },
            onReplyClick = { notification ->
                selectedNotification = notification
                showReplyDialog = true
            },
            onReplyDialogDismiss = {
                showReplyDialog = false
                replyMessage = ""
            },
            onIntent = { viewModel.sendIntent(it) },
            onProfileClick = onProfileClick,
            onSearchClick = onSearchClick
        )
    }
}

@Composable
fun TimelineScreenContent(
    viewState: TimelineViewState,
    snackbarHostState: SnackbarHostState,
    showReplyDialog: Boolean,
    selectedNotification: NotificationData?,
    replyMessage: String,
    onReplyMessageChange: (String) -> Unit,
    onReplyClick: (NotificationData) -> Unit,
    onReplyDialogDismiss: () -> Unit,
    onIntent: (TimelineIntent) -> Unit,
    onProfileClick: () -> Unit = {},
    onSearchClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TimelineTopAppBar(
                onFilterClick = {},
                onSearchClick = onSearchClick,
                onRefreshClick = {
                    onIntent(TimelineIntent.RefreshNotifications)
                },
                onProfileClick = onProfileClick
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (viewState.isLoading && viewState.notifications.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (viewState.filteredNotifications.isEmpty()) {
                EmptyState(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                TimelineContent(
                    notifications = viewState.filteredNotifications,
                    onReplyClick = onReplyClick,
                    onDeleteClick = { notificationId ->
                        onIntent(TimelineIntent.DeleteNotification(notificationId))
                    }
                )
            }

            if (showReplyDialog && selectedNotification != null) {
                ReplyDialog(
                    notification = selectedNotification,
                    message = replyMessage,
                    onMessageChange = onReplyMessageChange,
                    onSendClick = {
                        onIntent(
                            TimelineIntent.ReplyToNotification(
                                notificationId = selectedNotification.id,
                                message = replyMessage
                            )
                        )
                    },
                    onDismiss = onReplyDialogDismiss
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimelineTopAppBar(
    onFilterClick: () -> Unit,
    onSearchClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onProfileClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                "알림",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        ),
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "검색",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onRefreshClick) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = "필터",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

@Composable
private fun TimelineContent(
    notifications: List<NotificationData>,
    onReplyClick: (NotificationData) -> Unit,
    onDeleteClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(notifications) { notification ->
            NotificationCard(
                notification = notification,
                onReplyClick = { onReplyClick(notification) },
                onDeleteClick = { onDeleteClick(notification.id) }
            )
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationData,
    onReplyClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onReplyClick() },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // App Icon Placeholder with refined branding
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = getAppColor(notification.appType),
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val contentColor = if (notification.appType == NotificationData.AppType.KAKAOTALK) Color.Black else Color.White
                        Text(
                            text = notification.appType.name.take(1),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = contentColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = notification.displayName(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = getAppColor(notification.appType)
                        )
                        Text(
                            text = formatTimestamp(notification.timestamp),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Text(
                        text = notification.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notification.message,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "삭제", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("삭제", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onReplyClick,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Reply,
                        contentDescription = "답장",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("답장", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "알림이 없습니다",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "시스템 알림 리스너 권한을 활성화해주세요",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReplyDialog(
    notification: NotificationData,
    message: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clickable(enabled = false) {},
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    "${notification.displayName()}에 답장",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    notification.title,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = onMessageChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    placeholder = {
                        Text("메시지를 입력하세요")
                    },
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("취소")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onSendClick,
                        modifier = Modifier.height(40.dp),
                        enabled = message.isNotBlank()
                    ) {
                        Text("전송")
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60 * 1000 -> "지금"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}분 전"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}시간 전"
        else -> SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}

@Preview(showBackground = true)
@Composable
fun TimelineScreenPreview() {
    val sampleNotifications = listOf(
        NotificationData(
            id = 1,
            packageName = "com.kakao.talk",
            appType = NotificationData.AppType.KAKAOTALK,
            title = "홍길동",
            message = "오늘 저녁 뭐 먹을래?",
            subText = null,
            bigText = null,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 5,
            sbn = null
        ),
        NotificationData(
            id = 2,
            packageName = "jp.naver.line.android",
            appType = NotificationData.AppType.LINE,
            title = "김철수",
            message = "내일 회의 시간 변경됐어.",
            subText = null,
            bigText = null,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
            sbn = null
        ),
        NotificationData(
            id = 3,
            packageName = "org.telegram.messenger",
            appType = NotificationData.AppType.TELEGRAM,
            title = "이영희",
            message = "파일 보냈으니까 확인해봐.",
            subText = null,
            bigText = null,
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24,
            sbn = null
        )
    )

    All_In_One_ChatTheme {
        TimelineScreenContent(
            viewState = TimelineViewState(
                notifications = sampleNotifications
            ),
            snackbarHostState = remember { SnackbarHostState() },
            showReplyDialog = false,
            selectedNotification = null,
            replyMessage = "",
            onReplyMessageChange = {},
            onReplyClick = {},
            onReplyDialogDismiss = {},
            onIntent = {},
            onProfileClick = {}
        )
    }
}
