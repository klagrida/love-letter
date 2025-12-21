package com.loveletter.network

import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val email: String?,
    val displayName: String,
    val avatarUrl: String? = null
)

class AuthRepository {
    private val client = SupabaseClientProvider.client

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: Flow<UserProfile?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: Flow<Boolean> = _isLoggedIn.asStateFlow()

    suspend fun signUp(
        email: String,
        password: String,
        displayName: String
    ): Result<UserProfile> = runCatching {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            this.data = mapOf("display_name" to displayName)
        }

        val user = client.auth.currentUserOrNull()
            ?: throw Exception("Failed to create user")

        val profile = UserProfile(
            id = user.id,
            email = user.email,
            displayName = displayName
        )

        _currentUser.value = profile
        _isLoggedIn.value = true
        profile
    }

    suspend fun signIn(email: String, password: String): Result<UserProfile> = runCatching {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }

        val user = client.auth.currentUserOrNull()
            ?: throw Exception("Failed to sign in")

        val profile = UserProfile(
            id = user.id,
            email = user.email,
            displayName = user.userMetadata?.get("display_name")?.toString() ?: "Player"
        )

        _currentUser.value = profile
        _isLoggedIn.value = true
        profile
    }

    suspend fun signInAnonymously(displayName: String): Result<UserProfile> = runCatching {
        // For anonymous play, we generate a unique ID
        val anonymousId = "anon_${kotlin.random.Random.nextLong()}"

        val profile = UserProfile(
            id = anonymousId,
            email = null,
            displayName = displayName
        )

        _currentUser.value = profile
        _isLoggedIn.value = true
        profile
    }

    suspend fun signOut(): Result<Unit> = runCatching {
        try {
            client.auth.signOut()
        } catch (e: Exception) {
            // Ignore errors for anonymous users
        }
        _currentUser.value = null
        _isLoggedIn.value = false
    }

    suspend fun getCurrentUser(): UserProfile? {
        if (_currentUser.value != null) return _currentUser.value

        val user = client.auth.currentUserOrNull() ?: return null

        val profile = UserProfile(
            id = user.id,
            email = user.email,
            displayName = user.userMetadata?.get("display_name")?.toString() ?: "Player"
        )

        _currentUser.value = profile
        _isLoggedIn.value = true
        return profile
    }

    fun getCurrentUserSync(): UserProfile? = _currentUser.value
}
