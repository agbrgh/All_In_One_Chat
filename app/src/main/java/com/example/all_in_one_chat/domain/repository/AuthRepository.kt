package com.example.all_in_one_chat.domain.repository

import com.example.all_in_one_chat.domain.model.AuthState
import com.example.all_in_one_chat.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations
 */
interface AuthRepository {

    /**
     * Get current authentication state as a Flow for reactive updates
     */
    fun observeAuthState(): Flow<AuthState>

    /**
     * Get the current authenticated user
     */
    suspend fun getCurrentUser(): AuthUser?

    /**
     * Sign up with email and password
     */
    suspend fun signUp(email: String, password: String, displayName: String? = null): Result<AuthUser>

    /**
     * Sign in with email and password
     */
    suspend fun signIn(email: String, password: String): Result<AuthUser>

    /**
     * Sign out current user
     */
    suspend fun signOut(): Result<Unit>

    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    /**
     * Update user profile (display name, photo URL)
     */
    suspend fun updateProfile(displayName: String? = null, photoUrl: String? = null): Result<AuthUser>

    /**
     * Send email verification
     */
    suspend fun sendEmailVerification(): Result<Unit>

    /**
     * Delete current user account
     */
    suspend fun deleteAccount(): Result<Unit>

    /**
     * Reauthenticate user (required before sensitive operations)
     */
    suspend fun reauthenticate(password: String): Result<Unit>
}

