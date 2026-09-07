package com.example.all_in_one_chat.domain.model

/**
 * Domain model for authenticated user information
 */
data class AuthUser(
    val uid: String,
    val email: String,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

