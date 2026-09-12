package com.akgaming.huntersystem.data

import com.akgaming.huntersystem.BuildConfig
import com.akgaming.huntersystem.domain.Hunter
import com.akgaming.huntersystem.domain.HunterRank
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.decodeSingle
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface CloudSyncState {
    data object LocalOnly : CloudSyncState
    data object Syncing : CloudSyncState
    data class Synced(val message: String = "Cloud sync complete") : CloudSyncState
    data class Error(val message: String) : CloudSyncState
}

@Serializable
data class ProfileRow(
    @SerialName("user_id") val userId: String,
    @SerialName("hunter_name") val hunterName: String,
    @SerialName("display_name") val displayName: String,
    val level: Int,
    val rank: String,
    val xp: Int,
    val streak: Int,
    @SerialName("completed_quests") val completedQuests: Int,
    val version: Long,
)

@Serializable
private data class ProfileUpdate(
    @SerialName("hunter_name") val hunterName: String,
    @SerialName("display_name") val displayName: String,
    val level: Int,
    val rank: String,
    val xp: Int,
    val streak: Int,
    @SerialName("completed_quests") val completedQuests: Int,
)

object SupabaseGateway {
    val configured: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_PUBLISHABLE_KEY.isNotBlank()

    private val client: SupabaseClient? by lazy {
        if (!configured) return@lazy null
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY,
        ) {
            install(Auth)
            install(Postgrest)
        }
    }

    fun isAuthenticated(): Boolean = client?.auth?.currentUserOrNull() != null

    suspend fun signIn(email: String, password: String): Result<ProfileRow> = runCatching {
        val supabase = requireClient()
        require(email.isNotBlank()) { "Email is required" }
        require(password.length >= 8) { "Password is too short" }
        supabase.auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
        loadProfile().getOrThrow()
    }

    suspend fun signUp(email: String, password: String, hunterName: String): Result<String> = runCatching {
        val supabase = requireClient()
        require(email.isNotBlank()) { "Email is required" }
        require(password.length >= 12) { "Use at least 12 characters" }
        require(hunterName.isNotBlank()) { "Hunter name is required" }
        supabase.auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password
            data = kotlinx.serialization.json.buildJsonObject {
                put("hunter_name", hunterName.trim())
                put("display_name", hunterName.trim())
            }
        }
        "Account created. Confirm your email if Supabase requires verification."
    }

    suspend fun loadProfile(): Result<ProfileRow> = runCatching {
        val supabase = requireClient()
        val userId = supabase.auth.currentUserOrNull()?.id ?: error("Sign in to sync")
        supabase.from("profiles").select {
            filter { eq("user_id", userId) }
        }.decodeSingle<ProfileRow>()
    }

    suspend fun syncProfile(hunter: Hunter): Result<Unit> = runCatching {
        val supabase = requireClient()
        val userId = supabase.auth.currentUserOrNull()?.id ?: error("Sign in to sync")
        supabase.from("profiles").update(
            ProfileUpdate(
                hunterName = "Shadow Hunter",
                displayName = "Shadow Hunter",
                level = hunter.level,
                rank = hunter.rank.name,
                xp = hunter.xp.coerceIn(0, 499),
                streak = hunter.streak.coerceAtLeast(0),
                completedQuests = hunter.completedQuests.coerceAtLeast(0),
            )
        ) {
            filter { eq("user_id", userId) }
        }
    }

    suspend fun signOut() {
        client?.auth?.signOut()
    }

    private fun requireClient(): SupabaseClient = client ?: error("Supabase is not configured")
}

fun ProfileRow.toHunter(): Hunter = Hunter(
    level = level,
    xp = xp,
    rank = runCatching { HunterRank.valueOf(rank) }.getOrDefault(HunterRank.E),
    streak = streak,
    completedQuests = completedQuests,
)
