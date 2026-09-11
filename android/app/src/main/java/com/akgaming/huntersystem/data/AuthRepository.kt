package com.akgaming.huntersystem.data

import com.akgaming.huntersystem.data.remote.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email

class AuthRepository {
    private val auth = SupabaseProvider.client.auth
    fun hasSession(): Boolean = auth.currentSessionOrNull() != null
    suspend fun signIn(email: String, password: String) { auth.signInWith(Email) { this.email=email.trim(); this.password=password } }
    suspend fun signUp(email: String, password: String) { auth.signUpWith(Email) { this.email=email.trim(); this.password=password } }
    suspend fun signInAnonymously() = auth.signInAnonymously()
    suspend fun signInWithGoogle() = auth.signInWith(Google, redirectUrl = "huntersystem://auth-callback")
    suspend fun sendPasswordReset(email: String) { auth.resetPasswordForEmail(email.trim(), redirectUrl="huntersystem://auth-callback") }
    suspend fun signOut() = auth.signOut()
}
