package com.akgaming.huntersystem.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akgaming.huntersystem.data.CloudSyncState
import com.akgaming.huntersystem.data.SupabaseGateway
import com.akgaming.huntersystem.data.toHunter
import com.akgaming.huntersystem.domain.Hunter
import com.akgaming.huntersystem.domain.Progression
import com.akgaming.huntersystem.domain.Quest
import kotlinx.coroutines.launch

class HunterViewModel : ViewModel() {
    var hunter by mutableStateOf(Hunter())
        private set
    var quest by mutableStateOf(Quest())
        private set
    var currentObjective by mutableIntStateOf(0)
        private set
    var questComplete by mutableStateOf(false)
        private set
    var cloudState by mutableStateOf<CloudSyncState>(CloudSyncState.LocalOnly)
        private set

    fun completeCurrent() {
        if (questComplete) return
        if (currentObjective < quest.objectives.lastIndex) {
            currentObjective++
        } else {
            hunter = Progression.awardQuest(hunter, quest.rewardXp, .85)
            questComplete = true
            if (SupabaseGateway.isAuthenticated()) syncToCloud()
        }
    }

    fun signIn(email: String, password: String, onComplete: (Boolean) -> Unit = {}) {
        cloudState = CloudSyncState.Syncing
        viewModelScope.launch {
            SupabaseGateway.signIn(email, password).fold(
                onSuccess = { profile ->
                    hunter = profile.toHunter()
                    cloudState = CloudSyncState.Synced("Signed in and synced")
                    onComplete(true)
                },
                onFailure = { error ->
                    cloudState = CloudSyncState.Error(error.message ?: "Sign-in failed")
                    onComplete(false)
                },
            )
        }
    }

    fun signUp(email: String, password: String, hunterName: String, onComplete: (Boolean) -> Unit = {}) {
        cloudState = CloudSyncState.Syncing
        viewModelScope.launch {
            SupabaseGateway.signUp(email, password, hunterName).fold(
                onSuccess = { message ->
                    cloudState = CloudSyncState.Synced(message)
                    onComplete(true)
                },
                onFailure = { error ->
                    cloudState = CloudSyncState.Error(error.message ?: "Sign-up failed")
                    onComplete(false)
                },
            )
        }
    }

    fun syncToCloud() {
        if (!SupabaseGateway.configured) {
            cloudState = CloudSyncState.LocalOnly
            return
        }
        cloudState = CloudSyncState.Syncing
        viewModelScope.launch {
            SupabaseGateway.syncProfile(hunter).fold(
                onSuccess = { cloudState = CloudSyncState.Synced() },
                onFailure = { error -> cloudState = CloudSyncState.Error(error.message ?: "Sync failed") },
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            SupabaseGateway.signOut()
            cloudState = CloudSyncState.LocalOnly
        }
    }
}
