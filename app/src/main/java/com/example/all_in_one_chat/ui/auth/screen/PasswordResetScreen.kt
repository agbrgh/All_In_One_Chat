package com.example.all_in_one_chat.ui.auth.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.all_in_one_chat.ui.theme.All_In_One_ChatTheme

/**
 * Password Reset Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordResetScreen(
    email: String,
    isLoading: Boolean,
    error: String?,
    successMessage: String?,
    onEmailChange: (String) -> Unit,
    onSendResetEmailClick: () -> Unit,
    onBackClick: () -> Unit,
    onDismissError: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("비밀번호 재설정") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "비밀번호 재설정",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "계정에 등록된 이메일 주소를 입력하세요.\n비밀번호 재설정 이메일이 발송됩니다.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("이메일") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Send reset email button
            Button(
                onClick = onSendResetEmailClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading && email.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(24.dp)
                            .padding(end = 8.dp),
                        strokeWidth = 2.dp
                    )
                }
                Text("재설정 링크 전송")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PasswordResetScreenPreview() {
    All_In_One_ChatTheme {
        PasswordResetScreen(
            email = "example@email.com",
            isLoading = false,
            error = null,
            successMessage = null,
            onEmailChange = {},
            onSendResetEmailClick = {},
            onBackClick = {},
            onDismissError = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PasswordResetScreenLoadingPreview() {
    All_In_One_ChatTheme {
        PasswordResetScreen(
            email = "example@email.com",
            isLoading = true,
            error = null,
            successMessage = null,
            onEmailChange = {},
            onSendResetEmailClick = {},
            onBackClick = {},
            onDismissError = {}
        )
    }
}
