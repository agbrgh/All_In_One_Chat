package com.example.all_in_one_chat.domain.model

data class User(
    val id: String, // 내부 식별자
    val serviceType: ChatServiceType,
    val serviceUserId: String, // 서비스별 고유 ID
    val userName: String,
    val profileImageUrl: String? = null,
    val statusMessage: String? = null
)
