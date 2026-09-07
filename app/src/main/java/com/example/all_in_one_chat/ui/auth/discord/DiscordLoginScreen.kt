package com.example.all_in_one_chat.ui.auth.discord

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.all_in_one_chat.ui.theme.All_In_One_ChatTheme

@Composable
fun DiscordLoginScreen(
    onAuthenticated: () -> Unit,
    viewModel: DiscordAuthViewModel? = if (LocalInspectionMode.current) null else hiltViewModel()
) {
    if (LocalInspectionMode.current) {
        DiscordLoginContent(
            onLoginClick = {}
        )
    } else {
        val isConnected by viewModel!!.isConnected.collectAsState()

        LaunchedEffect(isConnected) {
            if (isConnected) {
                onAuthenticated()
            }
        }

        DiscordLoginContent(
            onLoginClick = { viewModel.login() }
        )
    }
}

@Composable
private fun DiscordLoginContent(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Connect Discord",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login to Discord (Demo)")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiscordLoginScreenPreview() {
    All_In_One_ChatTheme {
        DiscordLoginContent(
            onLoginClick = {}
        )
    }
}
