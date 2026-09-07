package com.example.all_in_one_chat.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.all_in_one_chat.ui.auth.navigation.AuthNavigationRoutes
import com.example.all_in_one_chat.ui.auth.screen.ProfileScreen
import com.example.all_in_one_chat.ui.auth.service.ServiceManagementScreen
import androidx.compose.ui.tooling.preview.Preview
import com.example.all_in_one_chat.ui.theme.All_In_One_ChatTheme
import com.example.all_in_one_chat.ui.chat.navigation.ChatNavigationRoutes
import com.example.all_in_one_chat.ui.chat.navigation.chatNavGraph
import com.example.all_in_one_chat.ui.chat.AdaptiveChatScreen
import com.example.all_in_one_chat.ui.media.navigation.mediaNavGraph
import com.example.all_in_one_chat.ui.navigation.mainNavItems
import com.example.all_in_one_chat.ui.timeline.navigation.timelineNavGraph

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val useAdaptiveLayout = if (isPreview) {
        LocalConfiguration.current.screenWidthDp >= 600
    } else {
        val activity = context.findActivity()
        if (activity != null) {
            calculateWindowSizeClass(activity).widthSizeClass != WindowWidthSizeClass.Compact
        } else {
            false
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                mainNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ChatNavigationRoutes.CHAT_LIST,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Chat graph
            if (useAdaptiveLayout) {
                composable(ChatNavigationRoutes.CHAT_LIST) {
                    AdaptiveChatScreen(
                        onProfileClick = {
                            navController.navigate(AuthNavigationRoutes.PROFILE)
                        }
                    )
                }
            } else {
                chatNavGraph(
                    navController = navController,
                    onProfileClick = {
                        navController.navigate(AuthNavigationRoutes.PROFILE)
                    }
                )
            }

            // Timeline graph
            timelineNavGraph(
                navController = navController,
                onProfileClick = {
                    navController.navigate(AuthNavigationRoutes.PROFILE)
                }
            )

            // Media graph
            mediaNavGraph(
                navController = navController
            )

            // Profile Screen (Standalone or part of Auth graph)
            composable(AuthNavigationRoutes.PROFILE) {
                ProfileScreen(
                    onLogout = onLogout,
                    onBackClick = { navController.popBackStack() },
                    onManageServicesClick = {
                        navController.navigate(AuthNavigationRoutes.SERVICE_MANAGEMENT)
                    },
                    onStorageManagementClick = {
                        navController.navigate(AuthNavigationRoutes.STORAGE_MANAGEMENT)
                    },
                    onSecuritySettingsClick = {
                        navController.navigate(AuthNavigationRoutes.SECURITY_SETTINGS)
                    },
                    onServiceStabilityClick = {
                        navController.navigate(AuthNavigationRoutes.SERVICE_STABILITY)
                    }
                )
            }

            composable(AuthNavigationRoutes.STORAGE_MANAGEMENT) {
                com.example.all_in_one_chat.ui.settings.StorageScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(AuthNavigationRoutes.SECURITY_SETTINGS) {
                com.example.all_in_one_chat.ui.settings.SecuritySettingsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(AuthNavigationRoutes.SERVICE_STABILITY) {
                com.example.all_in_one_chat.ui.settings.ServiceStabilityScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(AuthNavigationRoutes.SERVICE_MANAGEMENT) {
                val context = LocalContext.current
                ServiceManagementScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onNavigateToConnect = { serviceType ->
                        when (serviceType) {
                            com.example.all_in_one_chat.domain.model.ChatServiceType.TELEGRAM -> {
                                navController.navigate(AuthNavigationRoutes.TELEGRAM_LOGIN)
                            }
                            com.example.all_in_one_chat.domain.model.ChatServiceType.WHATSAPP,
                            com.example.all_in_one_chat.domain.model.ChatServiceType.DISCORD -> {
                                try {
                                    val intent = android.content.Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    // Fallback for older versions or if intent fails
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_SETTINGS)
                                    context.startActivity(intent)
                                }
                            }
                            else -> {}
                        }
                    }
                )
            }

            composable(AuthNavigationRoutes.TELEGRAM_LOGIN) {
                com.example.all_in_one_chat.ui.auth.telegram.TelegramLoginScreen(
                    onAuthenticated = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun MainScreenPreview() {
    All_In_One_ChatTheme {
        MainScreen(onLogout = {})
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
