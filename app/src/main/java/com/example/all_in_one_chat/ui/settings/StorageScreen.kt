package com.example.all_in_one_chat.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.ui.theme.All_In_One_ChatTheme
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun StorageScreen(
    viewModel: StorageViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val viewState by viewModel.viewState.collectAsState()
    StorageScreenContent(
        viewState = viewState,
        onBackClick = onBackClick,
        onClearClick = { viewModel.clearCache(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageScreenContent(
    viewState: StorageViewState,
    onBackClick: () -> Unit,
    onClearClick: (ChatServiceType) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("저장공간 관리", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            TotalUsageCard(totalBytes = viewState.totalUsageBytes)

            Text(
                "서비스별 미디어 용량",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viewState.serviceUsages) { item ->
                    ServiceStorageItem(
                        item = item,
                        onClearClick = { onClearClick(item.serviceType) }
                    )
                }
            }

            if (viewState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }

    LaunchedEffect(viewState.message) {
        viewState.message?.let {
            // In a real app, show a Snackbar
        }
    }
}

@Composable
fun TotalUsageCard(totalBytes: Long) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("총 미디어 사용량", style = MaterialTheme.typography.titleMedium)
            Text(
                formatSize(totalBytes),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ServiceStorageItem(
    item: StorageServiceItem,
    onClearClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(item.serviceType.name, fontWeight = FontWeight.Bold)
                Text(formatSize(item.usageBytes), color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
            }
            
            IconButton(onClick = onClearClick) {
                Icon(
                    Icons.Default.DeleteSweep,
                    contentDescription = "Clear Cache",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

fun formatSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    val locale = java.util.Locale.getDefault()
    return when {
        gb >= 1 -> String.format(locale, "%.2f GB", gb)
        mb >= 1 -> String.format(locale, "%.2f MB", mb)
        kb >= 1 -> String.format(locale, "%.2f KB", kb)
        else -> "$bytes Bytes"
    }
}

@Preview(showBackground = true)
@Composable
fun StorageScreenPreview() {
    All_In_One_ChatTheme {
        StorageScreenContent(
            viewState = StorageViewState(
                serviceUsages = listOf(
                    StorageServiceItem(ChatServiceType.TELEGRAM, 1024L * 1024 * 450), // 450 MB
                    StorageServiceItem(ChatServiceType.DISCORD, 1024L * 1024 * 120), // 120 MB
                    StorageServiceItem(ChatServiceType.WHATSAPP, 1024L * 1024 * 1024 * 2 + 1024L * 1024 * 300), // 2.3 GB
                    StorageServiceItem(ChatServiceType.SLACK, 1024L * 1024 * 15), // 15 MB
                ),
                totalUsageBytes = 3103784960L // ~2.89 GB
            ),
            onBackClick = {},
            onClearClick = {}
        )
    }
}
