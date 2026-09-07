package com.example.all_in_one_chat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import com.example.all_in_one_chat.data.local.prefs.SecurePreferences
import com.example.all_in_one_chat.data.repository.discord.DiscordServiceAdapter
import com.example.all_in_one_chat.domain.repository.ChatRepository
import com.example.all_in_one_chat.service.security.BiometricAuthenticator
import com.example.all_in_one_chat.service.sync.SyncManager
import com.example.all_in_one_chat.ui.auth.mvi.AuthViewModel
import com.example.all_in_one_chat.ui.auth.navigation.AuthNavigationRoutes
import com.example.all_in_one_chat.ui.navigation.RootNavGraph
import com.example.all_in_one_chat.ui.settings.AppearanceViewModel
import com.example.all_in_one_chat.ui.theme.All_In_One_ChatTheme
import com.example.all_in_one_chat.ui.timeline.navigation.TimelineNavigationRoutes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var chatRepository: ChatRepository

    @Inject
    lateinit var syncManager: SyncManager

    @Inject
    lateinit var discordAdapter: DiscordServiceAdapter

    @Inject
    lateinit var biometricAuthenticator: BiometricAuthenticator

    @Inject
    lateinit var securePreferences: SecurePreferences

    private val _isSessionAuthenticated = MutableStateFlow(false)
    private val isSessionAuthenticated = _isSessionAuthenticated.asStateFlow()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle permission result if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)
        checkNotificationPermission()
        checkAppLockOnStart()

        setContent {
            val appearanceViewModel: AppearanceViewModel = hiltViewModel()
            val appearanceState by appearanceViewModel.uiState.collectAsState()

            All_In_One_ChatTheme(
                themeMode = appearanceState.themeMode,
                dynamicColor = appearanceState.dynamicColorEnabled
            ) {
                val authViewModel: AuthViewModel = hiltViewModel()
                val authState by authViewModel.viewState.collectAsState()
                val isUnlocked by isSessionAuthenticated.collectAsState()
                
                var isLockEnabled by remember { mutableStateOf(true) }
                LaunchedEffect(Unit) {
                    isLockEnabled = securePreferences.getBoolean(SecurePreferences.KEY_APP_LOCK_ENABLED, false)
                }

                if (authState.isLoading && authState.currentUser == null) {
                    // Splash Screen
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (isLockEnabled && !isUnlocked && authState.isAuthenticated) {
                    // Lock Screen UI
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    val startDestination = if (authState.isAuthenticated) {
                        TimelineNavigationRoutes.TIMELINE
                    } else {
                        AuthNavigationRoutes.LOGIN
                    }

                    RootNavGraph(startDestination = startDestination)
                }
            }
        }
    }

    private fun checkAppLockOnStart() {
        lifecycleScope.launch {
            if (securePreferences.getBoolean(SecurePreferences.KEY_APP_LOCK_ENABLED, false)) {
                requestBiometricAuth()
            } else {
                _isSessionAuthenticated.value = true
            }
        }
    }

    private fun requestBiometricAuth() {
        biometricAuthenticator.authenticate(
            activity = this,
            onSuccess = {
                _isSessionAuthenticated.value = true
            },
            onError = {
                // Stay locked
            }
        )
    }

    override fun onRestart() {
        super.onRestart()
        lifecycleScope.launch {
            if (securePreferences.getBoolean(SecurePreferences.KEY_APP_LOCK_ENABLED, false)) {
                _isSessionAuthenticated.value = false
                requestBiometricAuth()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            chatRepository.downloadPendingMedia()
            syncManager.requestImmediateSync()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val intentData: Uri? = intent?.data
        if (intentData != null && intentData.scheme == "all-in-one-chat") {
            // 주소창 뒤에 붙어온 일회용 인증 코드(code) 추출
            val authCode = intentData.getQueryParameter("code")
            if (authCode != null) {
                lifecycleScope.launch {
                    discordAdapter.exchangeCodeForToken(authCode)
                }
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}