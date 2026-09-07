package com.example.all_in_one_chat.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.all_in_one_chat.domain.model.ChatMessage
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.model.User
import com.example.all_in_one_chat.ui.search.mvi.SearchEffect
import com.example.all_in_one_chat.ui.search.mvi.SearchIntent
import com.example.all_in_one_chat.ui.search.mvi.SearchViewState
import com.example.all_in_one_chat.ui.theme.All_In_One_ChatTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNavigateToChat: (String, String) -> Unit
) {
    val viewState by viewModel.viewState.collectAsState()
    val viewEffect = viewModel.viewEffect

    LaunchedEffect(Unit) {
        viewEffect.collect { effect ->
            when (effect) {
                is SearchEffect.NavigateToChat -> {
                    onNavigateToChat(effect.serviceType, effect.roomId)
                }
            }
        }
    }

    SearchContent(
        viewState = viewState,
        onQueryChange = { viewModel.sendIntent(SearchIntent.UpdateQuery(it)) },
        onClearClick = { viewModel.sendIntent(SearchIntent.ClearSearch) },
        onMessageClick = { viewModel.sendIntent(SearchIntent.SelectMessage(it)) },
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    viewState: SearchViewState,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    onMessageClick: (ChatMessage) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("메시지 검색") },
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
        ) {
            SearchInputArea(
                query = viewState.query,
                onQueryChange = onQueryChange,
                onClearClick = onClearClick
            )

            if (viewState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (viewState.results.isEmpty() && viewState.query.isNotBlank()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("검색 결과가 없습니다", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                SearchResultsList(
                    results = viewState.results,
                    onMessageClick = onMessageClick
                )
            }
        }
    }
}

@Composable
fun SearchInputArea(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text("검색어를 입력하세요 (예: 회의, 안녕)") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearClick) {
                    Icon(Icons.Default.Clear, contentDescription = "지우기")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

@Composable
fun SearchResultsList(
    results: List<ChatMessage>,
    onMessageClick: (ChatMessage) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(results) { message ->
            SearchResultItem(
                message = message,
                onClick = { onMessageClick(message) }
            )
        }
    }
}

@Composable
fun SearchResultItem(
    message: ChatMessage,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.sender.userName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = SimpleDateFormat("MM/dd HH:mm", LocalLocale.current.platformLocale).format(Date(message.timestamp)),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.content,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.serviceType.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    val sampleResults = listOf(
        ChatMessage(
            messageId = "m1",
            roomId = "r1",
            sender = User("u1", ChatServiceType.TELEGRAM, "su1", "홍길동", null, null),
            content = "회의록 작성 부탁드립니다.",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 30,
            serviceType = ChatServiceType.TELEGRAM,
            isFromMe = false
        ),
        ChatMessage(
            messageId = "m2",
            roomId = "r2",
            sender = User("u2", ChatServiceType.DISCORD, "su2", "김철수", null, null),
            content = "내일 점심 같이 드실 분?",
            timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
            serviceType = ChatServiceType.DISCORD,
            isFromMe = false
        )
    )

    All_In_One_ChatTheme {
        SearchContent(
            viewState = SearchViewState(
                query = "회의",
                results = sampleResults,
                isLoading = false
            ),
            onQueryChange = {},
            onClearClick = {},
            onMessageClick = {},
            onBackClick = {}
        )
    }
}
