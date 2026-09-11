package com.akgaming.huntersystem.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.akgaming.huntersystem.data.local.CachedProfile
import com.akgaming.huntersystem.data.local.HunterDao
import com.akgaming.huntersystem.data.local.SyncOutbox
import com.akgaming.huntersystem.data.remote.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
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

class ProfileRepository(
    private val context: Context,
    private val dao: HunterDao,
) {
    private val client = SupabaseProvider.client

    fun observeProfile(): Flow<CachedProfile?> = dao.observeProfile()

    suspend fun refreshFromServer() {
        val dto = client.from("profiles").select().decodeSingle<ProfileDto>()
        dao.upsertProfile(dto.toLocal())
    }

    suspend fun updateDisplayNameOffline(displayName: String) {
        val clean = displayName.trim().take(80)
        require(clean.isNotBlank()) { "Display name is required" }
        val current = requireNotNull(dao.currentProfile()) { "Profile is not loaded" }
        dao.upsertProfile(current.copy(displayName = clean, cachedAt = System.currentTimeMillis()))
        dao.enqueue(
            SyncOutbox(
                id = UUID.randomUUID().toString(),
                userId = current.userId,
                operation = "profile_display_name",
                payload = clean,
                createdAt = System.currentTimeMillis(),
            ),
        )
        scheduleSync()
    }

    suspend fun syncPending(): Boolean {
        var allSucceeded = true
        for (item in dao.pending()) {
            try {
                when (item.operation) {
                    "profile_display_name" -> client.from("profiles").update({
                        set("display_name", item.payload)
                    }) {
                        filter { eq("user_id", item.userId) }
                    }
                    else -> error("Unsupported outbox operation")
                }
                dao.removePending(item.id)
            } catch (error: Exception) {
                allSucceeded = false
                dao.recordFailure(item.id, error.message?.take(300) ?: "sync failed")
            }
        }
        if (allSucceeded) refreshFromServer()
        return allSucceeded
    }

    suspend fun pendingCount(): Int = dao.pendingCount()

    suspend fun clearLocalUserData() {
        dao.clearOutbox()
        dao.clearProfiles()
    }

    fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<ProfileSyncWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            ProfileSyncWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    private fun ProfileDto.toLocal() = CachedProfile(
        userId = userId,
        hunterName = hunterName,
        displayName = displayName,
        level = level,
        rank = rank,
        xp = xp,
        streak = streak,
        completedQuests = completedQuests,
        version = version,
        cachedAt = System.currentTimeMillis(),
    )
}
