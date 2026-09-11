package com.akgaming.huntersystem.data

import android.content.Context
import androidx.work.*
import com.akgaming.huntersystem.data.local.*
import com.akgaming.huntersystem.data.remote.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable data class ProfileDto(
    @SerialName("user_id") val userId: String,
    @SerialName("hunter_name") val hunterName: String,
    @SerialName("display_name") val displayName: String,
    val level: Int, val rank: String, val xp: Int, val streak: Int,
    @SerialName("completed_quests") val completedQuests: Int,
    val version: Long,
)

class ProfileRepository(private val context: Context, private val dao: HunterDao) {
    private val client = SupabaseProvider.client
    private val json = Json { ignoreUnknownKeys = true }
    fun observeProfile(): Flow<CachedProfile?> = dao.observeProfile()
    suspend fun refreshFromServer() { dao.upsertProfile(client.from("profiles").select().decodeSingle<ProfileDto>().toLocal()) }

    suspend fun updateDisplayNameOffline(displayName: String) {
        val clean = displayName.trim().take(80); require(clean.isNotBlank()) { "Display name is required" }
        val current = requireNotNull(dao.currentProfile()) { "Profile is not loaded" }
        dao.upsertProfile(current.copy(displayName = clean, cachedAt = System.currentTimeMillis()))
        dao.enqueue(SyncOutbox(UUID.randomUUID().toString(), current.userId, "profile_display_name", clean, System.currentTimeMillis()))
        scheduleSync()
    }

    suspend fun queueWorkoutCompletion(rewardXp: Int) {
        val current = requireNotNull(dao.currentProfile()) { "Profile is not loaded" }
        val sessionId = UUID.randomUUID().toString(); val eventId = UUID.randomUUID().toString(); val now = Instant.now().toString()
        val reps = listOf("bodyweight-squat" to 12, "push-up" to 10, "plank" to 0, "reverse-lunge" to 12)
        val sets = reps.mapIndexed { index, pair -> WorkoutSetPayload(UUID.randomUUID().toString(), pair.first, 1, pair.second, if (pair.first == "plank") 30 else 0, "manual") }
        val payload = WorkoutCompletionPayload(sessionId, eventId, current.userId, now, now, rewardXp.coerceIn(0, 1000), sets)
        dao.upsertWorkoutSession(LocalWorkoutSession(sessionId, current.userId, "completed_pending_sync", "quest", now, now, payload.rewardXp, "pending"))
        dao.upsertWorkoutSets(sets.map { LocalWorkoutSet(it.id, sessionId, it.slug, it.setNumber, it.completedReps, it.completedSeconds, it.verification, it.confidence) })
        dao.enqueue(SyncOutbox(eventId, current.userId, "workout_complete", json.encodeToString(payload), System.currentTimeMillis()))
        scheduleSync()
    }

    suspend fun syncPending(): Boolean {
        var allSucceeded = true
        for (item in dao.pending()) {
            try {
                when (item.operation) {
                    "profile_display_name" -> client.from("profiles").update({ set("display_name", item.payload) }) { filter { eq("user_id", item.userId) } }
                    "workout_complete" -> syncWorkout(json.decodeFromString(item.payload))
                    else -> error("Unsupported outbox operation: ${item.operation}")
                }
                dao.removePending(item.id)
            } catch (error: Exception) {
                allSucceeded = false; dao.recordFailure(item.id, error.message?.take(300) ?: "sync failed")
            }
        }
        if (allSucceeded) refreshFromServer()
        return allSucceeded
    }

    private suspend fun syncWorkout(payload: WorkoutCompletionPayload) {
        val exerciseIds = client.from("exercises").select().decodeList<ExerciseIdDto>().associate { it.slug to it.id }
        client.from("workout_sessions").upsert(SessionUploadDto(payload.sessionId, payload.userId, "active", "quest", payload.startedAt, payload.rewardXp))
        val rows = payload.sets.map { set ->
            SetUploadDto(set.id, payload.userId, payload.sessionId, requireNotNull(exerciseIds[set.slug]) { "Exercise unavailable: ${set.slug}" }, set.setNumber, set.completedReps, set.completedSeconds, set.verification, set.confidence, payload.completedAt)
        }
        client.from("workout_sets").upsert(rows)
        val authoritative = client.postgrest.rpc("complete_workout", buildJsonObject {
            put("p_session_id", payload.sessionId); put("p_idempotency_key", payload.idempotencyKey)
        }).decodeSingle<ProfileDto>()
        dao.upsertProfile(authoritative.toLocal()); dao.markWorkoutSynced(payload.sessionId)
    }

    suspend fun pendingCount() = dao.pendingCount()
    suspend fun clearLocalUserData() { dao.currentProfile()?.let { dao.clearWorkoutSessions(it.userId) }; dao.clearOutbox(); dao.clearProfiles() }
    fun scheduleSync() {
        val request = OneTimeWorkRequestBuilder<ProfileSyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(ProfileSyncWorker.WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }
    private fun ProfileDto.toLocal() = CachedProfile(userId, hunterName, displayName, level, rank, xp, streak, completedQuests, version, System.currentTimeMillis())
}
