package com.akgaming.huntersystem.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cached_profile")
data class CachedProfile(
    @PrimaryKey @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "hunter_name") val hunterName: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    val level: Int,
    val rank: String,
    val xp: Int,
    val streak: Int,
    @ColumnInfo(name = "completed_quests") val completedQuests: Int,
    val version: Long,
    @ColumnInfo(name = "cached_at") val cachedAt: Long,
)

@Entity(tableName = "sync_outbox", indices = [Index(value = ["created_at"])])
data class SyncOutbox(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    val operation: String,
    val payload: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    val attempts: Int = 0,
    @ColumnInfo(name = "last_error") val lastError: String? = null,
)

@Entity(tableName = "local_workout_sessions", indices = [Index(value = ["user_id", "started_at"])])
data class LocalWorkoutSession(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    val status: String,
    val source: String,
    @ColumnInfo(name = "started_at") val startedAt: String,
    @ColumnInfo(name = "completed_at") val completedAt: String?,
    @ColumnInfo(name = "reward_xp") val rewardXp: Int,
    @ColumnInfo(name = "sync_state") val syncState: String,
)

@Entity(tableName = "local_workout_sets", indices = [Index(value = ["session_id"])])
data class LocalWorkoutSet(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "session_id") val sessionId: String,
    val slug: String,
    @ColumnInfo(name = "set_number") val setNumber: Int,
    @ColumnInfo(name = "completed_reps") val completedReps: Int,
    @ColumnInfo(name = "completed_seconds") val completedSeconds: Int,
    val verification: String,
    val confidence: Float?,
)

@Dao
interface HunterDao {
    @Query("SELECT * FROM cached_profile ORDER BY cached_at DESC LIMIT 1") fun observeProfile(): Flow<CachedProfile?>
    @Query("SELECT * FROM cached_profile ORDER BY cached_at DESC LIMIT 1") suspend fun currentProfile(): CachedProfile?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertProfile(profile: CachedProfile)
    @Query("DELETE FROM cached_profile") suspend fun clearProfiles()
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun enqueue(item: SyncOutbox)
    @Query("SELECT * FROM sync_outbox ORDER BY created_at ASC LIMIT :limit") suspend fun pending(limit: Int = 25): List<SyncOutbox>
    @Query("SELECT COUNT(*) FROM sync_outbox") suspend fun pendingCount(): Int
    @Query("DELETE FROM sync_outbox WHERE id = :id") suspend fun removePending(id: String)
    @Query("UPDATE sync_outbox SET attempts = attempts + 1, last_error = :message WHERE id = :id") suspend fun recordFailure(id: String, message: String)
    @Query("DELETE FROM sync_outbox") suspend fun clearOutbox()
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertWorkoutSession(session: LocalWorkoutSession)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertWorkoutSets(sets: List<LocalWorkoutSet>)
    @Query("UPDATE local_workout_sessions SET status = 'completed', sync_state = 'synced' WHERE id = :sessionId") suspend fun markWorkoutSynced(sessionId: String)
    @Query("DELETE FROM local_workout_sessions WHERE user_id = :userId") suspend fun clearWorkoutSessions(userId: String)
}

@Database(entities = [CachedProfile::class, SyncOutbox::class, LocalWorkoutSession::class, LocalWorkoutSet::class], version = 2, exportSchema = true)
abstract class HunterDatabase : RoomDatabase() { abstract fun hunterDao(): HunterDao }
