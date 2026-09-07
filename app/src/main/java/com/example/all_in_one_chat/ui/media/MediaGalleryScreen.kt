package com.example.all_in_one_chat.ui.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.tooling.preview.Preview
import com.example.all_in_one_chat.domain.model.ChatMessage
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.model.MediaType
import com.example.all_in_one_chat.domain.model.User
import com.example.all_in_one_chat.ui.media.mvi.MediaIntent
import com.example.all_in_one_chat.ui.media.mvi.MediaViewState
import com.example.all_in_one_chat.ui.theme.All_In_One_ChatTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaGalleryScreen(
    viewModel: MediaViewModel = hiltViewModel(),
    onMediaClick: (String, String) -> Unit = { _, _ -> }
) {
    val viewState by viewModel.viewState.collectAsState()
    val viewEffect = viewModel.viewEffect

    LaunchedEffect(Unit) {
        viewEffect.collect { effect ->
            when (effect) {
                is com.example.all_in_one_chat.ui.media.mvi.MediaEffect.ShowMediaDetail -> {
                    onMediaClick(effect.message.serviceType.name, effect.message.messageId)
                }
            }
        }
    }

    MediaGalleryContent(
        viewState = viewState,
        onFilterByType = { viewModel.sendIntent(MediaIntent.FilterByType(it)) },
        onFilterByService = { viewModel.sendIntent(MediaIntent.FilterByService(it)) },
        onMediaClick = { viewModel.sendIntent(MediaIntent.ClickMedia(it)) }
    )
}

@Composable
fun FilterChips(
    selectedType: MediaType?,
    selectedService: ChatServiceType?,
    onTypeClick: (MediaType?) -> Unit,
    onServiceClick: (ChatServiceType?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedType == null,
            onClick = { onTypeClick(null) },
            label = { Text("전체") }
        )
        FilterChip(
            selected = selectedType == MediaType.IMAGE,
            onClick = { onTypeClick(MediaType.IMAGE) },
            label = { Text("이미지") }
        )
        FilterChip(
            selected = selectedType == MediaType.VIDEO,
            onClick = { onTypeClick(MediaType.VIDEO) },
            label = { Text("동영상") }
        )
        // Add more as needed
    }
}

@Composable
fun MediaGrid(
    mediaList: List<ChatMessage>,
    onMediaClick: (ChatMessage) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(1.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items(mediaList) { message ->
            MediaItem(
                message = message,
                onClick = { onMediaClick(message) }
            )
        }
    }
}

@Composable
fun MediaItem(
    message: ChatMessage,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = message.mediaLocalPath ?: message.mediaUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (message.mediaType == MediaType.VIDEO) {
            Icon(
                Icons.Default.PlayCircle,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Center),
                tint = Color.White.copy(alpha = 0.8f)
            )
        }

        // Service Indicator
        Box(
            modifier = Modifier
                .padding(4.dp)
                .align(Alignment.BottomEnd)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = message.serviceType.name.take(1),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaGalleryContent(
    viewState: MediaViewState,
    onFilterByType: (MediaType?) -> Unit,
    onFilterByService: (ChatServiceType?) -> Unit,
    onMediaClick: (ChatMessage) -> Unit
) {
    var showTypeFilter by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("미디어 갤러리", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showTypeFilter = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "타입 필터")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            FilterChips(
                selectedType = viewState.selectedType,
                selectedService = viewState.selectedService,
                onTypeClick = onFilterByType,
                onServiceClick = onFilterByService
            )

            if (viewState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (viewState.filteredMedia.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("미디어가 없습니다", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                MediaGrid(
                    mediaList = viewState.filteredMedia,
                    onMediaClick = onMediaClick
                )
            }
        }

        // Type Filter Menu
        DropdownMenu(
            expanded = showTypeFilter,
            onDismissRequest = { showTypeFilter = false }
        ) {
            DropdownMenuItem(
                text = { Text("전체") },
                onClick = { 
                    onFilterByType(null)
                    showTypeFilter = false 
                }
            )
            MediaType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.name) },
                    onClick = { 
                        onFilterByType(type)
                        showTypeFilter = false 
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MediaGalleryScreenPreview() {
    val sampleUser = User(
        id = "user1",
        serviceType = ChatServiceType.TELEGRAM,
        serviceUserId = "tg_user1",
        userName = "John Doe"
    )

    val sampleMedia = listOf(
        ChatMessage(
            messageId = "1",
            roomId = "room1",
            sender = sampleUser,
            content = "",
            timestamp = System.currentTimeMillis(),
            serviceType = ChatServiceType.TELEGRAM,
            mediaUrl = "https://example.com/1.jpg",
            mediaType = MediaType.IMAGE,
            isFromMe = false
        ),
        ChatMessage(
            messageId = "2",
            roomId = "room1",
            sender = sampleUser,
            content = "",
            timestamp = System.currentTimeMillis(),
            serviceType = ChatServiceType.DISCORD,
            mediaUrl = "https://example.com/2.mp4",
            mediaType = MediaType.VIDEO,
            isFromMe = false
        ),
        ChatMessage(
            messageId = "3",
            roomId = "room1",
            sender = sampleUser,
            content = "",
            timestamp = System.currentTimeMillis(),
            serviceType = ChatServiceType.WHATSAPP,
            mediaUrl = "https://example.com/3.jpg",
            mediaType = MediaType.IMAGE,
            isFromMe = true
        )
    )

    val viewState = MediaViewState(
        allMedia = sampleMedia,
        filteredMedia = sampleMedia,
        isLoading = false
    )

    All_In_One_ChatTheme {
        MediaGalleryContent(
            viewState = viewState,
            onFilterByType = {},
            onFilterByService = {},
            onMediaClick = {}
        )
    }
}
