package com.example.all_in_one_chat.ui.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.core.net.toUri
import androidx.compose.ui.tooling.preview.Preview
import com.example.all_in_one_chat.ui.theme.All_In_One_ChatTheme

@SuppressLint("BatteryLife")
@Composable
fun ServiceStabilityScreen(
    viewModel: ServiceStabilityViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val viewState by viewModel.viewState.collectAsState()
    val context = LocalContext.current

    ServiceStabilityContent(
        viewState = viewState,
        onBackClick = onBackClick,
        onNotificationActionClick = {
            try {
                context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            } catch (_: Exception) {
                context.startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        },
        onBatteryActionClick = {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = "package:${context.packageName}".toUri()
            }
            context.startActivity(intent)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceStabilityContent(
    viewState: ServiceStabilityViewState,
    onBackClick: () -> Unit,
    onNotificationActionClick: () -> Unit,
    onBatteryActionClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("연동 안정성 설정", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "안정적인 메시지 수신을 위해 아래 설정을 확인해주세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 1. Notification Listener Permission
            StabilityItem(
                title = "알림 접근 권한",
                description = "디스코드, 왓츠앱 등의 알림을 읽기 위해 필요합니다.",
                isEnabled = viewState.isNotificationListenerEnabled,
                icon = Icons.Default.NotificationsActive,
                onActionClick = onNotificationActionClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Battery Optimization Exception
            StabilityItem(
                title = "배터리 최적화 제외",
                description = "시스템이 앱을 종료하지 않도록 예외 등록이 권장됩니다.",
                isEnabled = viewState.isBatteryOptimizationIgnored,
                icon = Icons.Default.BatteryAlert,
                onActionClick = onBatteryActionClick
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Info Box
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "상태 표시줄 알림 안내",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "앱이 실행 중임을 알리는 상단 바 알림은 안드로이드 시스템 정책에 따라 서비스 유지를 위해 표시됩니다.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun StabilityItem(
    title: String,
    description: String,
    isEnabled: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isEnabled) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (isEnabled) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (isEnabled) "설정됨" else "설정 필요",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isEnabled) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    )
                }
            }

            if (!isEnabled) {
                Button(
                    onClick = onActionClick,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("설정")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ServiceStabilityScreenPreview() {
    All_In_One_ChatTheme {
        ServiceStabilityContent(
            viewState = ServiceStabilityViewState(
                isNotificationListenerEnabled = false,
                isBatteryOptimizationIgnored = true,
                isLoading = false
            ),
            onBackClick = {},
            onNotificationActionClick = {},
            onBatteryActionClick = {}
        )
    }
}
