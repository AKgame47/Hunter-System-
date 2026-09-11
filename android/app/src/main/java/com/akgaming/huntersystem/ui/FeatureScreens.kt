package com.akgaming.huntersystem.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akgaming.huntersystem.ui.theme.*

@Composable fun LeaderboardScreen(vm:HunterViewModel){LaunchedEffect(Unit){vm.loadLeaderboard()};FeaturePage("LIVE LEADERBOARD"){if(vm.leaderboard.isEmpty())Text("Loading live ranks…",color=Muted) else vm.leaderboard.forEach{Card(Modifier.fillMaxWidth()){Row(Modifier.padding(14.dp).fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("#${it.position} ${it.displayName}",fontWeight=FontWeight.Bold);Text("${it.totalXp} XP · ${it.rank}")}}};OutlinedButton(vm::loadLeaderboard,Modifier.fillMaxWidth()){Text("REFRESH")}}}
@Composable fun ProfileScreen(vm:HunterViewModel){var name by remember(vm.displayName){mutableStateOf(vm.displayName)};FeaturePage("PROFILE"){OutlinedTextField(name,{name=it},label={Text("Display name")},modifier=Modifier.fillMaxWidth());Button({vm.saveDisplayName(name)},Modifier.fillMaxWidth()){Text("SAVE PROFILE")};Text("Level ${vm.hunter.level} · ${vm.hunter.rank}-Rank · ${vm.hunter.xp} XP")}}
@Composable fun AchievementsScreen(vm:HunterViewModel)=FeaturePage("ACHIEVEMENTS"){val q=vm.hunter.completedQuests;Achievement("First Awakening",q>=1);Achievement("Quest Hunter",q>=10);Achievement("Century Discipline",q>=100);Achievement("Seven-day Flame",vm.hunter.streak>=7)}
@Composable fun StatisticsScreen(vm:HunterViewModel)=FeaturePage("STATISTICS"){Metric("Completed quests",vm.hunter.completedQuests.toString());Metric("Current streak","${vm.hunter.streak} days");Metric("Level",vm.hunter.level.toString());Metric("Total progression XP",(vm.hunter.level*500+vm.hunter.xp).toString())}
@Composable fun RecoveryScreen(vm:HunterViewModel)=FeaturePage("RECOVERY"){Text("Recovery mode pauses pressure-focused messaging. It never replaces medical care.");SwitchRow("Recovery mode",vm.recoveryMode,vm::setRecoveryMode);Text("Suggested: hydration, sleep, gentle mobility, and professional advice for pain or injury.",color=Muted)}
@Composable fun RestrictionsScreen(vm:HunterViewModel)=FeaturePage("APP RESTRICTIONS"){SwitchRow("Workout focus lock",vm.focusLock,vm::setFocusLock);Text("Voluntary in-app soft lock. Android only permits hard app blocking for managed device-owner deployments.",color=Muted)}
@Composable fun PrivacyScreen(vm:HunterViewModel)=FeaturePage("PRIVACY & DATA"){Text("Camera frames are processed on-device and are not intentionally uploaded.");Text("Account and workout data sync to Supabase under row-level security.");Text("Local AI models remain in app-private storage.");SelectionContainer{Text("Export summary: {name:${vm.displayName}, level:${vm.hunter.level}, xp:${vm.hunter.xp}, rank:${vm.hunter.rank}, quests:${vm.hunter.completedQuests}}")};Text("Server-side account deletion is a release gate.",color=Muted)}
@Composable fun SettingsScreen()=FeaturePage("SETTINGS"){Text("Theme: GYMORA dark");Text("Camera: optional");Text("AI downloads: explicit action only");Text("Health data sharing: disabled by default")}
@Composable fun AboutScreen()=FeaturePage("ABOUT GYMORA"){Text("GYMORA 0.3.0",fontWeight=FontWeight.Bold);Text("TRAIN · DISCIPLINE · EVOLVE");Text("Fitness guidance is educational and not medical advice.",color=Muted)}
@Composable private fun FeaturePage(title:String,content:@Composable ColumnScope.()->Unit)=Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text(title,color=Energy,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Black);content()}
@Composable private fun Achievement(name:String,unlocked:Boolean)=Card(Modifier.fillMaxWidth()){Text((if(unlocked)"✓ UNLOCKED · " else "LOCKED · ")+name,Modifier.padding(16.dp),color=if(unlocked)Success else Muted)}
@Composable private fun Metric(name:String,value:String)=Card(Modifier.fillMaxWidth()){Row(Modifier.padding(16.dp).fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(name);Text(value,fontWeight=FontWeight.Bold)}}
@Composable private fun SwitchRow(label:String,value:Boolean,onChange:(Boolean)->Unit)=Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(label);Switch(value,onChange)}
