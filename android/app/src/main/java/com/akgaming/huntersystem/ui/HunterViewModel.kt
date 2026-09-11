package com.akgaming.huntersystem.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.akgaming.huntersystem.HunterApplication
import com.akgaming.huntersystem.domain.Hunter
import com.akgaming.huntersystem.domain.HunterRank
import com.akgaming.huntersystem.domain.Progression
import com.akgaming.huntersystem.domain.Quest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

enum class AuthState { LOADING, SIGNED_OUT, CONFIRM_EMAIL, AUTHENTICATED }

class HunterViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as HunterApplication).container
    private val auth = container.authRepository
    private val profiles = container.profileRepository

    var hunter by mutableStateOf(Hunter())
        private set
    var displayName by mutableStateOf("Hunter")
        private set
    var quest by mutableStateOf(Quest())
        private set
    var currentObjective by mutableIntStateOf(0)
        private set
    var questComplete by mutableStateOf(false)
        private set
    var authState by mutableStateOf(AuthState.LOADING)
        private set
    var authBusy by mutableStateOf(false)
        private set
    var authMessage by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            profiles.observeProfile().filterNotNull().collect { profile ->
                displayName = profile.displayName
                hunter = Hunter(
                    level = profile.level,
                    xp = profile.xp,
                    rank = runCatching { HunterRank.valueOf(profile.rank) }.getOrDefault(HunterRank.E),
                    streak = profile.streak,
                    completedQuests = profile.completedQuests,
                )
            }
        }
        restoreSession()
    }

    private fun restoreSession() {
        viewModelScope.launch {
            authState = if (auth.hasSession()) AuthState.AUTHENTICATED else AuthState.SIGNED_OUT
            if (authState == AuthState.AUTHENTICATED) refreshProfile()
        }
    }

    fun signIn(email: String, password: String) = authAction {
        require(email.isNotBlank() && password.isNotBlank()) { "Email and password are required" }
        auth.signIn(email, password)
        authState = AuthState.AUTHENTICATED
        refreshProfile()
    }

    fun signUp(email: String, password: String) = authAction {
        require(email.isNotBlank()) { "Email is required" }
        require(password.length >= 8) { "Password must contain at least 8 characters" }
        auth.signUp(email, password)
        if (auth.hasSession()) {
            authState = AuthState.AUTHENTICATED
            refreshProfile()
        } else {
            authState = AuthState.CONFIRM_EMAIL
            authMessage = "Check your email and open the verification link."
        }
    }

    fun sendPasswordReset(email: String) = authAction {
        require(email.isNotBlank()) { "Enter your email first" }
        auth.sendPasswordReset(email)
        authMessage = "Password reset email sent."
    }

    fun signOut() = authAction {
        auth.signOut()
        profiles.clearLocalUserData()
        authState = AuthState.SIGNED_OUT
    }

    fun returnToSignIn() {
        authMessage = null
        authState = AuthState.SIGNED_OUT
    }

    fun saveDisplayName(name: String) {
        viewModelScope.launch {
            runCatching { profiles.updateDisplayNameOffline(name) }
                .onSuccess { authMessage = "Saved locally; sync scheduled." }
                .onFailure { authMessage = it.message ?: "Unable to save profile" }
        }
    }

    fun refreshProfile() {
        viewModelScope.launch {
            runCatching { profiles.refreshFromServer() }
                .onFailure { authMessage = "Offline mode: showing cached profile." }
            profiles.scheduleSync()
        }
    }

    fun clearMessage() {
        authMessage = null
    }

    fun completeCurrent() {
        if (questComplete) return
        if (currentObjective < quest.objectives.lastIndex) currentObjective++
        else {
            hunter = Progression.awardQuest(hunter, quest.rewardXp, .85)
            questComplete = true
        }
    }

    private fun authAction(block: suspend () -> Unit) {
        if (authBusy) return
        viewModelScope.launch {
            authBusy = true
            authMessage = null
            runCatching { block() }
                .onFailure { authMessage = it.message ?: "Authentication failed" }
            authBusy = false
        }
    }
}
