package com.akgaming.huntersystem.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.akgaming.huntersystem.HunterApplication

class ProfileSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as HunterApplication
        if (!app.container.authRepository.hasSession()) return Result.success()
        return try {
            val synced = app.container.profileRepository.syncPending()
            if (synced || app.container.profileRepository.pendingCount() == 0) Result.success()
            else Result.retry()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "hunter-profile-sync"
    }
}
