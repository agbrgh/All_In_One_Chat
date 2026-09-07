package com.example.all_in_one_chat.ui.auth.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.compose.ui.platform.LocalInspectionMode
import com.example.all_in_one_chat.ui.auth.mvi.AuthViewModel
import com.example.all_in_one_chat.ui.auth.mvi.AuthViewEvent
import com.example.all_in_one_chat.ui.auth.screen.LoginScreen
import com.example.all_in_one_chat.ui.auth.screen.SignUpScreen
import com.example.all_in_one_chat.ui.auth.screen.PasswordResetScreen
import com.example.all_in_one_chat.ui.auth.screen.ProfileScreen
import com.example.all_in_one_chat.ui.auth.service.ServiceManagementScreen
import com.example.all_in_one_chat.ui.auth.telegram.TelegramLoginScreen
import com.example.all_in_one_chat.ui.auth.discord.DiscordLoginScreen
import com.example.all_in_one_chat.ui.auth.whatsapp.WhatsAppLoginScreen

/**
 * Auth navigation graph builder
 */
fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    onAuthenticationSuccess: () -> Unit
) {
    composable(AuthNavigationRoutes.LOGIN) {
        val viewModel: AuthViewModel = if (LocalInspectionMode.current) hiltViewModel() else hiltViewModel()
        val viewState by viewModel.viewState.collectAsState()

        LoginScreen(
            email = viewState.emailInput,
            password = viewState.passwordInput,
            isLoading = viewState.isLoading,
            error = viewState.error,
            onEmailChange = { viewModel.handleEvent(AuthViewEvent.EmailChanged(it)) },
            onPasswordChange = { viewModel.handleEvent(AuthViewEvent.PasswordChanged(it)) },
            onLoginClick = {
                viewModel.handleEvent(AuthViewEvent.SignInClicked)
            },
            onSignUpClick = {
                navController.navigate(AuthNavigationRoutes.SIGN_UP)
            },
            onForgotPasswordClick = {
                navController.navigate(AuthNavigationRoutes.PASSWORD_RESET)
            },
            onDismissError = {
                viewModel.handleEvent(AuthViewEvent.DismissError)
            }
        )

        // Navigate to timeline when authenticated
        LaunchedEffect(viewState.isAuthenticated) {
            if (viewState.isAuthenticated) {
                onAuthenticationSuccess()
            }
        }
    }

    composable(AuthNavigationRoutes.SIGN_UP) {
        val viewModel: AuthViewModel = if (LocalInspectionMode.current) hiltViewModel() else hiltViewModel()
        val viewState by viewModel.viewState.collectAsState()

        SignUpScreen(
            email = viewState.emailInput,
            password = viewState.passwordInput,
            confirmPassword = viewState.confirmPasswordInput,
            displayName = viewState.displayNameInput,
            isLoading = viewState.isLoading,
            error = viewState.error,
            onEmailChange = { viewModel.handleEvent(AuthViewEvent.EmailChanged(it)) },
            onPasswordChange = { viewModel.handleEvent(AuthViewEvent.PasswordChanged(it)) },
            onConfirmPasswordChange = { viewModel.handleEvent(AuthViewEvent.ConfirmPasswordChanged(it)) },
            onDisplayNameChange = { viewModel.handleEvent(AuthViewEvent.DisplayNameChanged(it)) },
            onSignUpClick = {
                viewModel.handleEvent(AuthViewEvent.SignUpClicked)
            },
            onLoginClick = {
                navController.popBackStack()
            },
            onDismissError = {
                viewModel.handleEvent(AuthViewEvent.DismissError)
            }
        )

        // Navigate to timeline when authenticated
        LaunchedEffect(viewState.isAuthenticated) {
            if (viewState.isAuthenticated) {
                onAuthenticationSuccess()
            }
        }
    }

    composable(AuthNavigationRoutes.PASSWORD_RESET) {
        val viewModel: AuthViewModel = if (LocalInspectionMode.current) hiltViewModel() else hiltViewModel()
        val viewState by viewModel.viewState.collectAsState()

        PasswordResetScreen(
            email = viewState.emailInput,
            isLoading = viewState.isLoading,
            error = viewState.error,
            successMessage = viewState.successMessage,
            onEmailChange = { viewModel.handleEvent(AuthViewEvent.EmailChanged(it)) },
            onSendResetEmailClick = {
                viewModel.handleEvent(AuthViewEvent.SendPasswordResetClicked)
            },
            onBackClick = {
                navController.popBackStack()
            },
            onDismissError = {
                viewModel.handleEvent(AuthViewEvent.DismissError)
            }
        )
    }

    composable(AuthNavigationRoutes.PROFILE) {
        ProfileScreen(
            onLogout = onAuthenticationSuccess, // Should be passed from RootNavGraph
            onBackClick = {
                navController.popBackStack()
            },
            onManageServicesClick = {
                navController.navigate(AuthNavigationRoutes.SERVICE_MANAGEMENT)
            }
        )
    }

    composable(AuthNavigationRoutes.TELEGRAM_LOGIN) {
        TelegramLoginScreen(
            onAuthenticated = {
                navController.popBackStack()
            }
        )
    }

    composable(AuthNavigationRoutes.DISCORD_LOGIN) {
        DiscordLoginScreen(
            onAuthenticated = {
                navController.popBackStack()
            }
        )
    }

    composable(AuthNavigationRoutes.WHATSAPP_LOGIN) {
        WhatsAppLoginScreen(
            onAuthenticated = {
                navController.popBackStack()
            }
        )
    }

    composable(AuthNavigationRoutes.SERVICE_MANAGEMENT) {
        ServiceManagementScreen(
            onBackClick = {
                navController.popBackStack()
            },
            onNavigateToConnect = { serviceType ->
                when (serviceType) {
                    com.example.all_in_one_chat.domain.model.ChatServiceType.TELEGRAM -> {
                        navController.navigate(AuthNavigationRoutes.TELEGRAM_LOGIN)
                    }
                    com.example.all_in_one_chat.domain.model.ChatServiceType.DISCORD -> {
                        navController.navigate(AuthNavigationRoutes.DISCORD_LOGIN)
                    }
                    com.example.all_in_one_chat.domain.model.ChatServiceType.WHATSAPP -> {
                        navController.navigate(AuthNavigationRoutes.WHATSAPP_LOGIN)
                    }
                    else -> {
                        // Other services not implemented yet
                    }
                }
            }
        )
    }
}

