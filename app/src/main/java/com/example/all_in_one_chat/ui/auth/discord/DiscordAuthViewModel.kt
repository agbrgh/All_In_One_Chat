package com.example.all_in_one_chat.ui.auth.discord

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.all_in_one_chat.data.repository.discord.DiscordServiceAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.net.toUri

@HiltViewModel
class DiscordAuthViewModel @Inject constructor(
    private val discordAdapter: DiscordServiceAdapter,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val isConnected: StateFlow<Boolean> = discordAdapter.isConnected as StateFlow<Boolean>

    fun login() {
        val clientId = "1541347865308299294" // 1단계에서 복사한 Client ID
        val redirectUri = "all-in-one-chat://discord-login" // 1단계에서 설정한 주소
        
        // 필요한 권한들 지정: 
        // identify(유저 정보 조회), guilds(가입한 서버 목록), channels.read(채널 대화 읽기)
        val scope = "identify guilds channels.read" 
        
        val discordAuthUrl = "https://discord.com/api/oauth2/authorize" +
                "?client_id=$clientId" +
                "&redirect_uri=${Uri.encode(redirectUri)}" +
                "&response_type=code" +
                "&scope=${Uri.encode(scope)}"

        val intent = Intent(Intent.ACTION_VIEW, discordAuthUrl.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun handleAuthCode(code: String) {
        viewModelScope.launch {
            exchangeCodeForToken(code)
        }
    }

    private suspend fun exchangeCodeForToken(authCode: String) {
        // 이 authCode를 가지고 진짜 엑세스 토큰(discordToken)으로 교환해야 합니다!
        discordAdapter.exchangeCodeForToken(authCode)
    }
}
