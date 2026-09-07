package com.example.all_in_one_chat.data.remote

import com.example.all_in_one_chat.domain.model.AuthUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri

/**
 * Firebase Authentication Service
 * Handles all Firebase Auth operations
 */
@Singleton
class FirebaseAuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    /**
     * Get current authenticated user
     */
    suspend fun getCurrentUser(): AuthUser? {
        return firebaseAuth.currentUser?.toAuthUser()
    }

    /**
     * Sign up with email and password
     */
    suspend fun signUp(email: String, password: String, displayName: String? = null): AuthUser {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw IllegalStateException("User creation failed")

        // Update display name if provided
        if (displayName != null) {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()
        }

        return firebaseUser.toAuthUser()
    }

    /**
     * Sign in with email and password
     */
    suspend fun signIn(email: String, password: String): AuthUser {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw IllegalStateException("Sign in failed")
        return firebaseUser.toAuthUser()
    }

    /**
     * Sign out current user
     */
    suspend fun signOut() {
        firebaseAuth.signOut()
    }

    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    /**
     * Update user profile
     */
    suspend fun updateProfile(displayName: String? = null, photoUrl: String? = null): AuthUser {
        val firebaseUser = firebaseAuth.currentUser ?: throw IllegalStateException("No user signed in")

        val profileUpdates = UserProfileChangeRequest.Builder()
        if (displayName != null) profileUpdates.displayName = displayName
        if (photoUrl != null) profileUpdates.photoUri = photoUrl.toUri()

        firebaseUser.updateProfile(profileUpdates.build()).await()

        // Reload to get updated user
        firebaseUser.reload().await()
        return firebaseAuth.currentUser?.toAuthUser() ?: throw IllegalStateException("Failed to update profile")
    }

    /**
     * Send email verification
     */
    suspend fun sendEmailVerification() {
        val firebaseUser = firebaseAuth.currentUser ?: throw IllegalStateException("No user signed in")
        firebaseUser.sendEmailVerification().await()
    }

    /**
     * Delete user account
     */
    suspend fun deleteAccount() {
        val firebaseUser = firebaseAuth.currentUser ?: throw IllegalStateException("No user signed in")
        firebaseUser.delete().await()
    }

    /**
     * Get ID token for API calls
     */
    suspend fun getIdToken(): String? {
        return firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
    }

    /**
     * Reauthenticate user (required before sensitive operations)
     */
    suspend fun reauthenticate(password: String) {
        val firebaseUser = firebaseAuth.currentUser ?: throw IllegalStateException("No user signed in")
        val email = firebaseUser.email ?: throw IllegalStateException("No email for reauthentication")

        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
        firebaseUser.reauthenticate(credential).await()
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

