package com.example.all_in_one_chat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.all_in_one_chat.ui.auth.mvi.AuthViewModel
import com.example.all_in_one_chat.ui.auth.mvi.AuthViewState
import com.example.all_in_one_chat.ui.auth.navigation.AuthNavigationRoutes
import com.example.all_in_one_chat.ui.auth.navigation.authNavGraph
import com.example.all_in_one_chat.ui.MainScreen
import com.example.all_in_one_chat.ui.theme.All_In_One_ChatTheme
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.composable

object RootNavigationRoutes {
    const val AUTH = "auth_root"
    const val MAIN = "main_root"
}

/**
 * Root navigation graph that handles branching between Auth and Main App
 */
@Composable
fun RootNavGraph(
    startDestination: String = AuthNavigationRoutes.LOGIN,
    authViewModel: AuthViewModel? = if (LocalInspectionMode.current) null else hiltViewModel()
) {
    if (LocalInspectionMode.current) {
        RootNavGraphContent(
            authState = AuthViewState(isAuthenticated = false),
            onLogout = {}
        )
    } else {
        val authState by authViewModel!!.viewState.collectAsState()

        RootNavGraphContent(
            authState = authState,
            onLogout = {
                authViewModel.handleEvent(com.example.all_in_one_chat.ui.auth.mvi.AuthViewEvent.SignOutClicked)
            }
        )
    }
}

@Composable
fun RootNavGraphContent(
    authState: AuthViewState,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    // Global authentication observer to redirect to login if signed out
    LaunchedEffect(authState.isAuthenticated) {
        if (!authState.isAuthenticated) {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != AuthNavigationRoutes.LOGIN && 
                currentRoute != AuthNavigationRoutes.SIGN_UP && 
                currentRoute != AuthNavigationRoutes.PASSWORD_RESET) {
                navController.navigate(AuthNavigationRoutes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            }
        } else {
            // If authenticated and on login screen, go to main
            val currentRoute = navController.currentDestination?.route
            if (currentRoute == AuthNavigationRoutes.LOGIN) {
                navController.navigate(RootNavigationRoutes.MAIN) {
                    popUpTo(AuthNavigationRoutes.LOGIN) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (authState.isAuthenticated) RootNavigationRoutes.MAIN else AuthNavigationRoutes.LOGIN
    ) {
        // Auth graph
        authNavGraph(
            navController = navController,
            onAuthenticationSuccess = {
                navController.navigate(RootNavigationRoutes.MAIN) {
                    popUpTo(AuthNavigationRoutes.LOGIN) { inclusive = true }
                }
            }
        )

        // Main App Screen (Contains Bottom Bar and Sub-NavHost)
        composable(RootNavigationRoutes.MAIN) {
            MainScreen(
                onLogout = onLogout
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RootNavGraphPreview() {
    All_In_One_ChatTheme {
        RootNavGraphContent(
            authState = AuthViewState(isAuthenticated = false),
            onLogout = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RootNavGraphAuthenticatedPreview() {
    All_In_One_ChatTheme {
        RootNavGraphContent(
            authState = AuthViewState(isAuthenticated = true),
            onLogout = {}
        )
    }
}
