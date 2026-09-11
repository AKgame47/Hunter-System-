package com.akgaming.huntersystem.ui

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.akgaming.huntersystem.HunterApplication
import com.akgaming.huntersystem.data.remote.SupabaseProvider
import com.akgaming.huntersystem.domain.*
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable data class LeaderboardEntry(@SerialName("leaderboard_position") val position:Long,@SerialName("display_name") val displayName:String,val level:Int,val rank:String,@SerialName("total_xp") val totalXp:Long,@SerialName("completed_quests") val completedQuests:Int)
enum class AuthState { LOADING, SIGNED_OUT, CONFIRM_EMAIL, AUTHENTICATED }
class HunterViewModel(application: Application) : AndroidViewModel(application) {
    private val container=(application as HunterApplication).container; private val auth=container.authRepository; private val profiles=container.profileRepository
    var hunter by mutableStateOf(Hunter()); private set
    var displayName by mutableStateOf("Hunter"); private set
    var quest by mutableStateOf(Quest()); private set
    var currentObjective by mutableIntStateOf(0); private set
    var questComplete by mutableStateOf(false); private set
    var completionBusy by mutableStateOf(false); private set
    var authState by mutableStateOf(AuthState.LOADING); private set
    var authBusy by mutableStateOf(false); private set
    var authMessage by mutableStateOf<String?>(null); private set
    var leaderboard by mutableStateOf<List<LeaderboardEntry>>(emptyList()); private set
    var recoveryMode by mutableStateOf(false); private set
    var focusLock by mutableStateOf(false); private set
    init { viewModelScope.launch { profiles.observeProfile().filterNotNull().collect { p -> displayName=p.displayName; hunter=Hunter(p.level,p.xp,runCatching{HunterRank.valueOf(p.rank)}.getOrDefault(HunterRank.E),p.streak,p.completedQuests) } }; restoreSession() }
    private fun restoreSession(){viewModelScope.launch{authState=if(auth.hasSession())AuthState.AUTHENTICATED else AuthState.SIGNED_OUT;if(authState==AuthState.AUTHENTICATED){refreshProfile();loadLeaderboard()}}}
    fun signIn(email:String,password:String)=authAction{require(email.isNotBlank()&&password.isNotBlank()){ "Email and password are required" };auth.signIn(email,password);authenticated()}
    fun signUp(email:String,password:String)=authAction{require(email.isNotBlank()){ "Email is required" };require(password.length>=8){ "Password must contain at least 8 characters" };auth.signUp(email,password);if(auth.hasSession())authenticated() else {authState=AuthState.CONFIRM_EMAIL;authMessage="Check your email and open the verification link."}}
    fun signInGuest()=authAction{auth.signInAnonymously();authenticated()}
    fun signInGoogle()=authAction{auth.signInWithGoogle();authMessage="Complete Google sign-in in the secure browser."}
    private suspend fun authenticated(){authState=AuthState.AUTHENTICATED;refreshProfile();loadLeaderboard()}
    fun sendPasswordReset(email:String)=authAction{require(email.isNotBlank()){ "Enter your email first" };auth.sendPasswordReset(email);authMessage="Password reset email sent."}
    fun signOut()=authAction{auth.signOut();profiles.clearLocalUserData();authState=AuthState.SIGNED_OUT}
    fun returnToSignIn(){authMessage=null;authState=AuthState.SIGNED_OUT}
    fun saveDisplayName(name:String){viewModelScope.launch{runCatching{profiles.updateDisplayNameOffline(name)}.onSuccess{authMessage="Saved locally; sync scheduled."}.onFailure{authMessage=it.message?:"Unable to save profile"}}}
    fun refreshProfile(){viewModelScope.launch{runCatching{profiles.refreshFromServer()}.onFailure{authMessage="Offline mode: showing cached profile."};profiles.scheduleSync()}}
    fun loadLeaderboard(){viewModelScope.launch{runCatching{SupabaseProvider.client.postgrest.rpc("get_leaderboard",buildJsonObject{put("p_limit",50)}).decodeList<LeaderboardEntry>()}.onSuccess{leaderboard=it}.onFailure{authMessage="Leaderboard unavailable: ${it.message}"}}}
    fun setRecoveryMode(value:Boolean){recoveryMode=value}
    fun setFocusLock(value:Boolean){focusLock=value}
    fun clearMessage(){authMessage=null}
    fun completeCurrent(){if(questComplete||completionBusy)return;if(currentObjective<quest.objectives.lastIndex) currentObjective++ else viewModelScope.launch {completionBusy=true;runCatching { profiles.queueWorkoutCompletion(quest.rewardXp) }.onSuccess { questComplete=true;authMessage="Workout saved. Server reward sync scheduled." }.onFailure { authMessage=it.message?:"Unable to save workout" };completionBusy=false}}
    private fun authAction(block:suspend()->Unit){if(authBusy)return;viewModelScope.launch{authBusy=true;authMessage=null;runCatching{block()}.onFailure{authMessage=it.message?:"Authentication failed"};authBusy=false}}
}
