package com.example.all_in_one_chat.domain.model

enum class ChatServiceType(val displayName: String, val brandColor: Long) {
    TELEGRAM("Telegram", 0xFF26A5E4),
    DISCORD("Discord", 0xFF5865F2),
    WHATSAPP("WhatsApp", 0xFF25D366),
    SLACK("Slack", 0xFF4A154B),
    KAKAOTALK("KakaoTalk", 0xFFFFCD00),
    LINE("Line", 0xFF00C300)
}
