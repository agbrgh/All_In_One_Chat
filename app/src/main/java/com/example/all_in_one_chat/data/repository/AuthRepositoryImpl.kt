package com.example.all_in_one_chat.data.repository

import com.example.all_in_one_chat.data.remote.FirebaseAuthService
import com.example.all_in_one_chat.domain.model.AuthState
import com.example.all_in_one_chat.domain.model.AuthUser
import com.example.all_in_one_chat.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AuthRepository with Firebase Authentication
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthService: FirebaseAuthService,
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    private val _authStateFlow = MutableSharedFlow<AuthState>(replay = 1)

    init {
        // Initialize with current auth state
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            _authStateFlow.tryEmit(AuthState.Authenticated(
                currentUser.toAuthUser()
            ))
        } else {
            _authStateFlow.tryEmit(AuthState.Unauthenticated)
        }

        // Listen to auth state changes
        firebaseAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            val state = if (user != null) {
                AuthState.Authenticated(user.toAuthUser())
            } else {
                AuthState.Unauthenticated
            }
            _authStateFlow.tryEmit(state)
        }
    }

    override fun observeAuthState(): Flow<AuthState> = _authStateFlow.asSharedFlow()

    override suspend fun getCurrentUser(): AuthUser? {
        return try {
            firebaseAuthService.getCurrentUser()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String?
    ): Result<AuthUser> {
        return try {
            _authStateFlow.emit(AuthState.Loading)
            val user = firebaseAuthService.signUp(email, password, displayName)
            // AuthStateListener will emit Authenticated
            Result.success(user)
        } catch (e: Exception) {
            _authStateFlow.emit(AuthState.Error(e.message ?: "Sign up failed"))
            Result.failure(e)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<AuthUser> {
        return try {
            _authStateFlow.emit(AuthState.Loading)
            val user = firebaseAuthService.signIn(email, password)
            // AuthStateListener will emit Authenticated
            Result.success(user)
        } catch (e: Exception) {
            _authStateFlow.emit(AuthState.Error(e.message ?: "Sign in failed"))
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuthService.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuthService.sendPasswordResetEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(
        displayName: String?,
        photoUrl: String?
    ): Result<AuthUser> {
        return try {
            val user = firebaseAuthService.updateProfile(displayName, photoUrl)
            _authStateFlow.emit(AuthState.Authenticated(user))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            firebaseAuthService.sendEmailVerification()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            firebaseAuthService.deleteAccount()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reauthenticate(password: String): Result<Unit> {
        return try {
            firebaseAuthService.reauthenticate(password)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Extension function to convert Firebase User to AuthUser
 */
private fun com.google.firebase.auth.FirebaseUser.toAuthUser(): AuthUser {
    return AuthUser(
        uid = this.uid,
        email = this.email ?: "",
        displayName = this.displayName,
        photoUrl = this.photoUrl?.toString(),
        isEmailVerified = this.isEmailVerified,
        createdAt = this.metadata?.creationTimestamp ?: System.currentTimeMillis()
    )
}

