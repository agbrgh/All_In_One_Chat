package com.example.all_in_one_chat.ui.media

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import kotlin.OptIn
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.all_in_one_chat.domain.model.MediaType
import androidx.core.net.toUri
import androidx.compose.ui.tooling.preview.Preview
import com.example.all_in_one_chat.domain.model.ChatMessage
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.model.User
import com.example.all_in_one_chat.ui.theme.All_In_One_ChatTheme

@UnstableApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailScreen(
    viewModel: MediaDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val viewState by viewModel.viewState.collectAsState()
    MediaDetailContent(
        viewState = viewState,
        onBackClick = onBackClick
    )
}

@UnstableApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailContent(
    viewState: MediaDetailViewState,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = viewState.message?.sender?.userName ?: "",
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    viewState.message?.let { message ->
                        val mediaPath = message.mediaLocalPath ?: message.mediaUrl
                        if (mediaPath != null) {
                            IconButton(onClick = {
                                shareMedia(context, mediaPath, message.mediaType?.name ?: "image/*")
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                            }
                            IconButton(onClick = {
                                saveMediaToGallery(context, mediaPath, message.mediaType?.name ?: "IMAGE")
                            }) {
                                Icon(Icons.Default.Download, contentDescription = "Save", tint = Color.White)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.5f)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                viewState.isLoading -> {
                    CircularProgressIndicator(color = Color.White)
                }
                viewState.error != null -> {
                    Text(text = viewState.error, color = Color.White)
                }
                viewState.message != null -> {
                    val message = viewState.message
                    when (message.mediaType) {
                        MediaType.IMAGE -> {
                            ZoomableImage(model = message.mediaLocalPath ?: message.mediaUrl)
                        }
                        MediaType.VIDEO -> {
                            VideoPlayer(videoPath = message.mediaLocalPath ?: message.mediaUrl ?: "")
                        }
                        else -> {
                            Text(text = "Unsupported media type", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

private fun shareMedia(context: android.content.Context, path: String, mimeType: String) {
    val uri = path.toUri()
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = if (mimeType.contains("VIDEO")) "video/*" else "image/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share via"))
}

private fun saveMediaToGallery(context: android.content.Context, path: String, type: String) {
    val isVideo = type.contains("VIDEO")
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "AllInOneChat_${System.currentTimeMillis()}")
        put(MediaStore.MediaColumns.MIME_TYPE, if (isVideo) "video/mp4" else "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, if (isVideo) "Movies/AllInOneChat" else "Pictures/AllInOneChat")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
    }

    val collection = if (isVideo) {
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    val uri = context.contentResolver.insert(collection, contentValues)
    if (uri != null) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val sourceFile = java.io.File(path)
                if (sourceFile.exists()) {
                    sourceFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                } else {
                    // If it's a URL, we might need to download it first or handle differently
                    // For now, assume it's a local path since it's a "Save" action
                    Toast.makeText(context, "파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
            }
            Toast.makeText(context, "갤러리에 저장되었습니다.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            context.contentResolver.delete(uri, null, null)
            Toast.makeText(context, "저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun ZoomableImage(model: Any?) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
    ) {
        AsyncImage(
            model = model,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                ),
            contentScale = ContentScale.Fit
        )
    }
}

@UnstableApi
@Composable
fun VideoPlayer(videoPath: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoPath)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = false // No auto-play as per requirement
        }
    }

    DisposableEffect(
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = true
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    ) {
        onDispose {
            exoPlayer.release()
        }
    }
}

@UnstableApi
@Preview(showBackground = true)
@Composable
fun MediaDetailScreenPreview() {
    val sampleUser = User(
        id = "1",
        serviceType = ChatServiceType.TELEGRAM,
        serviceUserId = "user1",
        userName = "John Doe"
    )

    val sampleMessage = ChatMessage(
        messageId = "msg1",
        roomId = "room1",
        sender = sampleUser,
        content = "",
        timestamp = System.currentTimeMillis(),
        serviceType = ChatServiceType.TELEGRAM,
        mediaUrl = "https://example.com/image.jpg",
        mediaType = MediaType.IMAGE,
        isFromMe = false
    )

    val sampleViewState = MediaDetailViewState(
        message = sampleMessage,
        isLoading = false
    )

    All_In_One_ChatTheme {
        MediaDetailContent(
            viewState = sampleViewState,
            onBackClick = {}
        )
    }
}
