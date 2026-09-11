package com.akgaming.huntersystem

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.akgaming.huntersystem.data.AuthRepository
import com.akgaming.huntersystem.data.ProfileRepository
import com.akgaming.huntersystem.data.local.HunterDatabase

class HunterApplication : Application() {
    lateinit var container: AppContainer
        private set
    override fun onCreate() {
        super.onCreate()
        val database = Room.databaseBuilder(applicationContext, HunterDatabase::class.java, "hunter-system.db")
            .addMigrations(MIGRATION_1_2)
            .build()
        container = AppContainer(AuthRepository(), ProfileRepository(applicationContext, database.hunterDao()))
    }
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `local_workout_sessions` (`id` TEXT NOT NULL, `user_id` TEXT NOT NULL, `status` TEXT NOT NULL, `source` TEXT NOT NULL, `started_at` TEXT NOT NULL, `completed_at` TEXT, `reward_xp` INTEGER NOT NULL, `sync_state` TEXT NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_local_workout_sessions_user_id_started_at` ON `local_workout_sessions` (`user_id`, `started_at`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `local_workout_sets` (`id` TEXT NOT NULL, `session_id` TEXT NOT NULL, `slug` TEXT NOT NULL, `set_number` INTEGER NOT NULL, `completed_reps` INTEGER NOT NULL, `completed_seconds` INTEGER NOT NULL, `verification` TEXT NOT NULL, `confidence` REAL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_local_workout_sets_session_id` ON `local_workout_sets` (`session_id`)")
            }
        }
    }
}
data class AppContainer(val authRepository: AuthRepository, val profileRepository: ProfileRepository)
