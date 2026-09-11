package com.akgaming.huntersystem

import android.app.Application
import androidx.room.Room
import com.akgaming.huntersystem.data.AuthRepository
import com.akgaming.huntersystem.data.ProfileRepository
import com.akgaming.huntersystem.data.local.HunterDatabase

class HunterApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val database = Room.databaseBuilder(
            applicationContext,
            HunterDatabase::class.java,
            "hunter-system.db",
        ).build()
        container = AppContainer(
            authRepository = AuthRepository(),
            profileRepository = ProfileRepository(applicationContext, database.hunterDao()),
        )
    }
}

data class AppContainer(
    val authRepository: AuthRepository,
    val profileRepository: ProfileRepository,
)
