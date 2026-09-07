package com.example.all_in_one_chat.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.all_in_one_chat.ui.theme.All_In_One_ChatTheme
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun SecuritySettingsScreen(
    viewModel: SecurityViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val viewState by viewModel.viewState.collectAsState()
    SecuritySettingsContent(
        viewState = viewState,
        onBackClick = onBackClick,
        onToggleAppLock = { viewModel.toggleAppLock(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecuritySettingsContent(
    viewState: SecurityViewState,
    onBackClick: () -> Unit,
    onToggleAppLock: (Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("보안 설정", fontWeight = FontWeight.Bold) },
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
        ) {
            Text(
                "앱 보호",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("생체 인식 / PIN 잠금", fontWeight = FontWeight.Bold)
                                Text(
                                    "앱을 실행하거나 백그라운드에서 복귀할 때 본인 인증을 요청합니다.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = viewState.isAppLockEnabled,
                            onCheckedChange = onToggleAppLock,
                            enabled = viewState.isBiometricAvailable
                        )
                    }

                    if (!viewState.isBiometricAvailable) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "이 기기는 생체 인식 또는 화면 잠금을 지원하지 않거나 설정되지 않았습니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "보안 정보",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            InfoRow(Icons.Default.Security, "데이터 보호", "모든 채팅 데이터는 기기 내에서 암호화되어 저장됩니다.")
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SecuritySettingsScreenPreview() {
    All_In_One_ChatTheme {
        SecuritySettingsContent(
            viewState = SecurityViewState(
                isAppLockEnabled = true,
                isBiometricAvailable = true
            ),
            onBackClick = {},
            onToggleAppLock = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SecuritySettingsScreenNoBiometricPreview() {
    All_In_One_ChatTheme {
        SecuritySettingsContent(
            viewState = SecurityViewState(
                isAppLockEnabled = false,
                isBiometricAvailable = false
            ),
            onBackClick = {},
            onToggleAppLock = {}
        )
    }
}
