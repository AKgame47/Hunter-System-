package com.akgaming.huntersystem.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
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

@Entity(
    tableName = "sync_outbox",
    indices = [Index(value = ["created_at"])],
)
data class SyncOutbox(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    val operation: String,
    val payload: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    val attempts: Int = 0,
    @ColumnInfo(name = "last_error") val lastError: String? = null,
)

@Dao
interface HunterDao {
    @Query("SELECT * FROM cached_profile ORDER BY cached_at DESC LIMIT 1")
    fun observeProfile(): Flow<CachedProfile?>

    @Query("SELECT * FROM cached_profile ORDER BY cached_at DESC LIMIT 1")
    suspend fun currentProfile(): CachedProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: CachedProfile)

    @Query("DELETE FROM cached_profile")
    suspend fun clearProfiles()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: SyncOutbox)

    @Query("SELECT * FROM sync_outbox ORDER BY created_at ASC LIMIT :limit")
    suspend fun pending(limit: Int = 25): List<SyncOutbox>

    @Query("SELECT COUNT(*) FROM sync_outbox")
    suspend fun pendingCount(): Int

    @Query("DELETE FROM sync_outbox WHERE id = :id")
    suspend fun removePending(id: String)

    @Query("UPDATE sync_outbox SET attempts = attempts + 1, last_error = :message WHERE id = :id")
    suspend fun recordFailure(id: String, message: String)

    @Query("DELETE FROM sync_outbox")
    suspend fun clearOutbox()
}

@Database(
    entities = [CachedProfile::class, SyncOutbox::class],
    version = 1,
    exportSchema = true,
)
abstract class HunterDatabase : RoomDatabase() {
    abstract fun hunterDao(): HunterDao
}
