package com.example.all_in_one_chat.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.all_in_one_chat.data.local.prefs.ThemeMode
import com.example.all_in_one_chat.ui.theme.All_In_One_ChatTheme

@Composable
fun AppearanceSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppearanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    AppearanceSettingsContent(
        themeMode = uiState.themeMode,
        dynamicColorEnabled = uiState.dynamicColorEnabled,
        onNavigateBack = onNavigateBack,
        onThemeModeChange = { viewModel.setThemeMode(it) },
        onDynamicColorChange = { viewModel.setDynamicColorEnabled(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsContent(
    themeMode: ThemeMode,
    dynamicColorEnabled: Boolean,
    onNavigateBack: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("화면 설정") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("테마 모드", style = MaterialTheme.typography.titleMedium)

            ThemeMode.entries.forEach { mode ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = themeMode == mode,
                        onClick = { onThemeModeChange(mode) }
                    )
                    Text(
                        text = when (mode) {
                            ThemeMode.LIGHT -> "라이트 모드"
                            ThemeMode.DARK -> "다크 모드"
                            ThemeMode.SYSTEM -> "시스템 설정"
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dynamic Color", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Android 12 이상에서 배경화면 색상을 시스템 테마에 적용합니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = dynamicColorEnabled,
                    onCheckedChange = onDynamicColorChange
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppearanceSettingsPreview() {
    All_In_One_ChatTheme {
        AppearanceSettingsContent(
            themeMode = ThemeMode.SYSTEM,
            dynamicColorEnabled = true,
            onNavigateBack = {},
            onThemeModeChange = {},
            onDynamicColorChange = {}
        )
    }
}
