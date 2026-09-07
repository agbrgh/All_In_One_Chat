package com.example.all_in_one_chat.domain.model

data class User(
    val id: String,
    val serviceType: ChatServiceType,
    val serviceUserId: String,
    val userName: String,
    val profileImageUrl: String? = null,
    val statusMessage: String? = null
)
