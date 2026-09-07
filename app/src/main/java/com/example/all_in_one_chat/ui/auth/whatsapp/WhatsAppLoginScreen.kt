package com.example.all_in_one_chat.ui.auth.whatsapp

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
fun WhatsAppLoginScreen(
    onAuthenticated: () -> Unit,
    viewModel: WhatsAppAuthViewModel? = if (LocalInspectionMode.current) null else hiltViewModel()
) {
    if (LocalInspectionMode.current) {
        WhatsAppLoginScreen(
            isConnected = false,
            onAuthenticated = onAuthenticated,
            onLoginClick = {}
        )
    } else {
        val isConnected by viewModel!!.isConnected.collectAsState()

        WhatsAppLoginScreen(
            isConnected = isConnected,
            onAuthenticated = onAuthenticated,
            onLoginClick = { viewModel.login() }
        )
    }
}

@Composable
fun WhatsAppLoginScreen(
    isConnected: Boolean,
    onAuthenticated: () -> Unit,
    onLoginClick: () -> Unit
) {
    LaunchedEffect(isConnected) {
        if (isConnected) {
            onAuthenticated()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Connect WhatsApp",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login to WhatsApp (Demo)")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WhatsAppLoginScreenPreview() {
    All_In_One_ChatTheme {
        WhatsAppLoginScreen(
            isConnected = false,
            onAuthenticated = {},
            onLoginClick = {}
        )
    }
}
