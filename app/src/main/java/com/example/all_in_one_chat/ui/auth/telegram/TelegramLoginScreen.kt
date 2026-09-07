package com.example.all_in_one_chat.ui.auth.telegram

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.all_in_one_chat.data.repository.telegram.TelegramAuthState
import com.example.all_in_one_chat.ui.theme.All_In_One_ChatTheme

@Composable
fun TelegramLoginScreen(
    onAuthenticated: () -> Unit,
    viewModel: TelegramAuthViewModel? = if (LocalInspectionMode.current) null else hiltViewModel()
) {
    if (LocalInspectionMode.current) {
        TelegramLoginContent(
            authState = TelegramAuthState.WaitPhoneNumber,
            onPhoneNumberEntered = {},
            onCodeEntered = {},
            onPasswordEntered = {},
            onRetry = {}
        )
    } else {
        val authState by viewModel!!.authState.collectAsState()

        LaunchedEffect(authState) {
            if (authState is TelegramAuthState.Authenticated) {
                onAuthenticated()
            }
        }

        TelegramLoginContent(
            authState = authState,
            onPhoneNumberEntered = viewModel::onPhoneNumberEntered,
            onCodeEntered = viewModel::onCodeEntered,
            onPasswordEntered = viewModel::onPasswordEntered,
            onRetry = viewModel::retry
        )
    }
}

@Composable
fun TelegramLoginContent(
    authState: TelegramAuthState,
    onPhoneNumberEntered: (String) -> Unit,
    onCodeEntered: (String) -> Unit,
    onPasswordEntered: (String) -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Connect Telegram",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        when (authState) {
            is TelegramAuthState.Initial -> {
                CircularProgressIndicator()
            }
            is TelegramAuthState.WaitPhoneNumber -> {
                PhoneNumberInput(onPhoneNumberEntered = onPhoneNumberEntered)
            }
            is TelegramAuthState.WaitCode -> {
                CodeInput(onCodeEntered = onCodeEntered)
            }
            is TelegramAuthState.WaitPassword -> {
                PasswordInput(onPasswordEntered = onPasswordEntered)
            }
            is TelegramAuthState.Error -> {
                Text(text = "Error: ${authState.message}", color = MaterialTheme.colorScheme.error)
                Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Retry")
                }
            }
            is TelegramAuthState.Authenticated -> {
                Text("Authenticated!")
            }
        }
    }
}

@Composable
fun PhoneNumberInput(onPhoneNumberEntered: (String) -> Unit) {
    var phoneNumber by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number (+8210...)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { onPhoneNumberEntered(phoneNumber) },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Send Code")
        }
    }
}

@Composable
fun CodeInput(onCodeEntered: (String) -> Unit) {
    var code by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Verification Code") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { onCodeEntered(code) },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Verify")
        }
    }
}

@Composable
fun PasswordInput(onPasswordEntered: (String) -> Unit) {
    var password by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("2FA Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { onPasswordEntered(password) },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Submit Password")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TelegramLoginPreview_PhoneNumber() {
    All_In_One_ChatTheme {
        Surface {
            TelegramLoginContent(
                authState = TelegramAuthState.WaitPhoneNumber,
                onPhoneNumberEntered = {},
                onCodeEntered = {},
                onPasswordEntered = {},
                onRetry = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TelegramLoginPreview_WaitCode() {
    All_In_One_ChatTheme {
        Surface {
            TelegramLoginContent(
                authState = TelegramAuthState.WaitCode,
                onPhoneNumberEntered = {},
                onCodeEntered = {},
                onPasswordEntered = {},
                onRetry = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TelegramLoginPreview_Error() {
    All_In_One_ChatTheme {
        Surface {
            TelegramLoginContent(
                authState = TelegramAuthState.Error("Invalid phone number"),
                onPhoneNumberEntered = {},
                onCodeEntered = {},
                onPasswordEntered = {},
                onRetry = {}
            )
        }
    }
}
