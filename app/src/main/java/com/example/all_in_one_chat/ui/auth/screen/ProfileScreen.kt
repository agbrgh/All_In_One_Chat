package com.example.all_in_one_chat.ui.auth.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.ui.platform.LocalInspectionMode
import coil.compose.AsyncImage
import com.example.all_in_one_chat.data.repository.telegram.TelegramAuthState
import com.example.all_in_one_chat.domain.model.AuthUser
import com.example.all_in_one_chat.ui.auth.mvi.AuthViewEvent
import com.example.all_in_one_chat.ui.auth.mvi.AuthViewModel
import com.example.all_in_one_chat.ui.auth.telegram.TelegramAuthViewModel
import com.example.all_in_one_chat.ui.theme.All_In_One_ChatTheme

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onBackClick: () -> Unit,
    onManageServicesClick: () -> Unit,
    onStorageManagementClick: () -> Unit = {},
    onSecuritySettingsClick: () -> Unit = {},
    onServiceStabilityClick: () -> Unit = {},
    authViewModel: AuthViewModel? = if (LocalInspectionMode.current) null else hiltViewModel(),
    telegramAuthViewModel: TelegramAuthViewModel? = if (LocalInspectionMode.current) null else hiltViewModel()
) {
    if (LocalInspectionMode.current) {
        ProfileScreen(
            user = AuthUser(
                uid = "preview",
                email = "preview@example.com",
                displayName = "Preview User",
                isEmailVerified = true
            ),
            telegramAuthState = TelegramAuthState.Initial,
            isLoading = false,
            onLogout = onLogout,
            onBackClick = onBackClick,
            onManageServicesClick = onManageServicesClick,
            onStorageManagementClick = onStorageManagementClick,
            onSecuritySettingsClick = onSecuritySettingsClick,
            onServiceStabilityClick = onServiceStabilityClick,
            onUpdateProfile = {},
            onTelegramRetry = {}
        )
    } else {
        val authState by authViewModel!!.viewState.collectAsState()
        val telegramAuthState by telegramAuthViewModel!!.authState.collectAsState()

        ProfileScreen(
            user = authState.currentUser,
            telegramAuthState = telegramAuthState,
            isLoading = authState.isLoading,
            onLogout = onLogout,
            onBackClick = onBackClick,
            onManageServicesClick = onManageServicesClick,
            onStorageManagementClick = onStorageManagementClick,
            onSecuritySettingsClick = onSecuritySettingsClick,
            onServiceStabilityClick = onServiceStabilityClick,
            onUpdateProfile = { name ->
                authViewModel.handleEvent(AuthViewEvent.UpdateProfileClicked(name, null))
            },
            onTelegramRetry = {
                telegramAuthViewModel.retry()
            }
        )
    }
}

@Composable
fun ProfileScreen(
    user: AuthUser?,
    telegramAuthState: TelegramAuthState,
    isLoading: Boolean,
    onLogout: () -> Unit,
    onBackClick: () -> Unit,
    onManageServicesClick: () -> Unit,
    onStorageManagementClick: () -> Unit,
    onSecuritySettingsClick: () -> Unit,
    onServiceStabilityClick: () -> Unit,
    onUpdateProfile: (String) -> Unit,
    onTelegramRetry: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editDisplayName by remember { mutableStateOf(user?.displayName ?: "") }

    ProfileContent(
        user = user,
        telegramAuthState = telegramAuthState,
        isLoading = isLoading,
        onSignOutClick = onLogout,
        onEditClick = {
            editDisplayName = user?.displayName ?: ""
            showEditDialog = true
        },
        onConnectTelegramClick = onTelegramRetry,
        onManageServicesClick = onManageServicesClick,
        onStorageManagementClick = onStorageManagementClick,
        onSecuritySettingsClick = onSecuritySettingsClick,
        onServiceStabilityClick = onServiceStabilityClick,
        onBackClick = onBackClick
    )

    if (showEditDialog) {
        EditProfileDialog(
            displayName = editDisplayName,
            onDisplayNameChange = { editDisplayName = it },
            onSaveClick = {
                onUpdateProfile(editDisplayName)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false },
            isLoading = isLoading
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    user: AuthUser?,
    telegramAuthState: TelegramAuthState,
    isLoading: Boolean,
    onSignOutClick: () -> Unit,
    onEditClick: () -> Unit,
    onConnectTelegramClick: () -> Unit,
    onManageServicesClick: () -> Unit,
    onStorageManagementClick: () -> Unit,
    onSecuritySettingsClick: () -> Unit,
    onServiceStabilityClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("프로필", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Profile Picture Section
            Box(contentAlignment = Alignment.BottomEnd) {
                if (!user?.photoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Default Avatar",
                            modifier = Modifier.padding(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                SmallFloatingActionButton(
                    onClick = onEditClick,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "편집", modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = user?.displayName ?: "이름 없음",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = user?.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Info Sections
            SectionTitle("계정 정보")
            ProfileInfoCard {
                InfoItem(Icons.Default.Email, "이메일", user?.email ?: "")
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                InfoItem(
                    Icons.Default.VerifiedUser, 
                    "이메일 인증", 
                    if (user?.isEmailVerified == true) "인증됨" else "미인증",
                    tint = if (user?.isEmailVerified == true) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle("연동된 서비스")
            ProfileInfoCard {
                ServiceItem(
                    name = "Telegram",
                    status = when (telegramAuthState) {
                        is TelegramAuthState.Authenticated -> "연결됨"
                        is TelegramAuthState.Initial -> "확인 중"
                        else -> "연결 안 됨"
                    },
                    isConnected = telegramAuthState is TelegramAuthState.Authenticated,
                    onConnectClick = onConnectTelegramClick
                )
                
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp, end = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                
                TextButton(
                    onClick = onManageServicesClick,
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("서비스 통합 관리", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle("설정")
            ProfileInfoCard {
                TextButton(
                    onClick = onStorageManagementClick,
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("저장공간 관리", style = MaterialTheme.typography.labelLarge)
                }
                
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp, end = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                TextButton(
                    onClick = onSecuritySettingsClick,
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("보안 설정", style = MaterialTheme.typography.labelLarge)
                }

                HorizontalDivider(modifier = Modifier.padding(start = 16.dp, end = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                TextButton(
                    onClick = onServiceStabilityClick,
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Icon(Icons.Default.HealthAndSafety, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("연동 안정성 설정", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logout Button
            TextButton(
                onClick = onSignOutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("로그아웃", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
private fun ProfileInfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, tint: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(value, style = MaterialTheme.typography.bodyLarge, color = tint)
        }
    }
}

@Composable
private fun ServiceItem(name: String, status: String, isConnected: Boolean, onConnectClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = if (isConnected) Color(0xFF26A5E4).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(name.take(1), fontWeight = FontWeight.Bold, color = if (isConnected) Color(0xFF26A5E4) else MaterialTheme.colorScheme.outline)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(status, style = MaterialTheme.typography.labelSmall, color = if (isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline)
            }
        }
        
        if (!isConnected) {
            TextButton(onClick = onConnectClick) {
                Text("연결")
            }
        } else {
            IconButton(onClick = { /* Disconnect logic */ }) {
                Icon(Icons.Default.LinkOff, contentDescription = "연결 해제", tint = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("프로필 수정") },
        text = {
            Column {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = onDisplayNameChange,
                    label = { Text("이름") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSaveClick,
                enabled = displayName.isNotEmpty() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 8.dp),
                        strokeWidth = 2.dp
                    )
                }
                Text("저장")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !isLoading) {
                Text("취소")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    All_In_One_ChatTheme {
        ProfileScreen(
            user = AuthUser(
                uid = "123",
                email = "user@example.com",
                displayName = "홍길동",
                isEmailVerified = true
            ),
            telegramAuthState = TelegramAuthState.Authenticated,
            isLoading = false,
            onLogout = {},
            onBackClick = {},
            onManageServicesClick = {},
            onStorageManagementClick = {},
            onSecuritySettingsClick = {},
            onServiceStabilityClick = {},
            onUpdateProfile = {},
            onTelegramRetry = {}
        )
    }
}
