package com.example.all_in_one_chat.ui.auth.service

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.model.ServiceStatus
import com.example.all_in_one_chat.ui.theme.All_In_One_ChatTheme

@Composable
fun ServiceManagementScreen(
    onBackClick: () -> Unit,
    onNavigateToConnect: (ChatServiceType) -> Unit,
    viewModel: ServiceManagementViewModel? = if (LocalInspectionMode.current) null else hiltViewModel()
) {
    if (LocalInspectionMode.current) {
        val sampleViewState = ServiceManagementViewState(
            services = listOf(
                ServiceStatus(ChatServiceType.TELEGRAM, isConnected = true, isActive = true),
                ServiceStatus(ChatServiceType.DISCORD, isConnected = true, isActive = false),
                ServiceStatus(ChatServiceType.WHATSAPP, isConnected = false, isActive = false)
            )
        )
        ServiceManagementContent(
            viewState = sampleViewState,
            onBackClick = onBackClick,
            onToggleActive = { _, _ -> },
            onConnectClick = onNavigateToConnect
        )
    } else {
        val viewState by viewModel!!.viewState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            viewModel.viewEffect.collect { effect ->
                when (effect) {
                    is ServiceManagementViewEffect.NavigateToConnect -> onNavigateToConnect(effect.serviceType)
                    is ServiceManagementViewEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                }
            }
        }

        ServiceManagementContent(
            viewState = viewState,
            onBackClick = onBackClick,
            onToggleActive = { serviceType, isActive ->
                viewModel.sendIntent(ServiceManagementIntent.ToggleServiceActive(serviceType, isActive))
            },
            onConnectClick = { serviceType ->
                viewModel.sendIntent(ServiceManagementIntent.ConnectService(serviceType))
            },
            snackbarHostState = snackbarHostState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceManagementContent(
    viewState: ServiceManagementViewState,
    onBackClick: () -> Unit,
    onToggleActive: (ChatServiceType, Boolean) -> Unit,
    onConnectClick: (ChatServiceType) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    var showAddServiceDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("서비스 관리", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (viewState.isLoading && viewState.services.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "연결된 서비스 및 알림 설정",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(viewState.services.filter { it.isConnected }) { service ->
                    ServiceManagementItem(
                        service = service,
                        onToggleActive = { isActive ->
                            onToggleActive(service.serviceType, isActive)
                        },
                        onConnectClick = {
                            onConnectClick(service.serviceType)
                        }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showAddServiceDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("새 서비스 추가", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showAddServiceDialog) {
        val unconnectedServices = viewState.services.filter { !it.isConnected }
        AddServiceDialog(
            services = unconnectedServices,
            onDismiss = { showAddServiceDialog = false },
            onConnectClick = {
                onConnectClick(it)
                showAddServiceDialog = false
            }
        )
    }
}

@Composable
fun AddServiceDialog(
    services: List<ServiceStatus>,
    onDismiss: () -> Unit,
    onConnectClick: (ChatServiceType) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "새 서비스 추가",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                if (services.isEmpty()) {
                    Text(
                        text = "모든 지원 서비스가 연결되었습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    services.forEach { service ->
                        TextButton(
                            onClick = { onConnectClick(service.serviceType) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(service.serviceType.displayName, fontSize = 18.sp)
                        }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("닫기")
                }
            }
        }
    }
}

@Composable
fun ServiceManagementItem(
    service: ServiceStatus,
    onToggleActive: (Boolean) -> Unit,
    onConnectClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Service Icon
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = when (service.serviceType) {
                    ChatServiceType.TELEGRAM -> Color(0xFF26A5E4).copy(alpha = 0.1f)
                    ChatServiceType.DISCORD -> Color(0xFF5865F2).copy(alpha = 0.1f)
                    ChatServiceType.WHATSAPP -> Color(0xFF25D366).copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (service.isConnected) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = when (service.serviceType) {
                            ChatServiceType.TELEGRAM -> Color(0xFF26A5E4)
                            ChatServiceType.DISCORD -> Color(0xFF5865F2)
                            ChatServiceType.WHATSAPP -> Color(0xFF25D366)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.serviceType.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = if (service.isConnected) "연결됨" else "연결되지 않음",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (service.isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline
                )
            }

            if (service.isConnected) {
                Switch(
                    checked = service.isActive,
                    onCheckedChange = onToggleActive,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            } else {
                TextButton(onClick = onConnectClick) {
                    Text("연결")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ServiceManagementPreview() {
    val sampleViewState = ServiceManagementViewState(
        services = listOf(
            ServiceStatus(ChatServiceType.TELEGRAM, isConnected = true, isActive = true),
            ServiceStatus(ChatServiceType.DISCORD, isConnected = true, isActive = false),
            ServiceStatus(ChatServiceType.WHATSAPP, isConnected = false, isActive = false)
        )
    )
    All_In_One_ChatTheme {
        ServiceManagementContent(
            viewState = sampleViewState,
            onBackClick = {},
            onToggleActive = { _, _ -> },
            onConnectClick = {}
        )
    }
}
