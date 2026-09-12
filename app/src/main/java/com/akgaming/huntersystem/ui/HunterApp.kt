package com.akgaming.huntersystem.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.akgaming.huntersystem.data.CloudSyncState
import com.akgaming.huntersystem.data.SupabaseGateway
import com.akgaming.huntersystem.domain.Hunter
import com.akgaming.huntersystem.ui.theme.Danger
import com.akgaming.huntersystem.ui.theme.Energy
import com.akgaming.huntersystem.ui.theme.EnergySoft
import com.akgaming.huntersystem.ui.theme.HunterText
import com.akgaming.huntersystem.ui.theme.Muted
import com.akgaming.huntersystem.ui.theme.Night
import com.akgaming.huntersystem.ui.theme.Raised
import com.akgaming.huntersystem.ui.theme.Success
import com.akgaming.huntersystem.ui.theme.Surface
import com.akgaming.huntersystem.ui.theme.Warning

private enum class Route(val path: String, val label: String, val glyph: String) {
    HOME("home", "HOME", "⌂"), QUEST("quest", "QUEST", "✓"), TRAIN("train", "TRAIN", "✦"),
    RANK("rank", "RANK", "♜"), MENU("menu", "MENU", "≡"), PROFILE("profile", "PROFILE", "◉"),
    ACHIEVEMENTS("achievements", "ACHIEVEMENTS", "★"), STATISTICS("statistics", "STATISTICS", "▥"),
    COACH("coach", "AI COACH", "◌"), RESTRICTIONS("restrictions", "APP RESTRICTIONS", "⊘"),
    LEAVE("leave", "LEAVE / RECOVERY", "☾"), SETTINGS("settings", "SETTINGS", "⚙"),
    ACCOUNT("account", "CLOUD ACCOUNT", "☁"), DEVELOPER("developer", "DEVELOPER SETTINGS", "⌘"),
    ABOUT("about", "ABOUT", "ⓘ"),
}

@Composable
fun HunterApp(vm: HunterViewModel = viewModel()) {
    var entered by rememberSaveable { mutableStateOf(false) }
    if (!entered) Onboarding { entered = true } else {
        val nav = rememberNavController()
        val entry by nav.currentBackStackEntryAsState()
        Scaffold(containerColor = Night, bottomBar = { BottomBar(entry?.destination?.route, nav) }) { pad ->
            NavHost(nav, Route.HOME.path, Modifier.padding(pad)) {
                composable(Route.HOME.path) { Home(vm, nav) }
                composable(Route.QUEST.path) { Workout(vm, nav) }
                composable(Route.TRAIN.path) { Training(nav) }
                composable(Route.RANK.path) { Leaderboard(nav) }
                composable(Route.MENU.path) { Menu(nav) }
                composable(Route.PROFILE.path) { Profile(vm, nav) }
                composable(Route.ACHIEVEMENTS.path) { Achievements(nav) }
                composable(Route.STATISTICS.path) { Statistics(vm, nav) }
                composable(Route.COACH.path) { Coach(nav) }
                composable(Route.RESTRICTIONS.path) { Restrictions(nav) }
                composable(Route.LEAVE.path) { Leave(nav) }
                composable(Route.SETTINGS.path) { Settings(nav) }
                composable(Route.ACCOUNT.path) { Account(vm, nav) }
                composable(Route.DEVELOPER.path) { Developer(nav) }
                composable(Route.ABOUT.path) { About(nav) }
            }
        }
    }
}

@Composable private fun Onboarding(onEnter: () -> Unit) = Column(
    Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Night, Color(0xFF061A38), Night))).windowInsetsPadding(WindowInsets.safeDrawing).padding(24.dp),
    Arrangement.Center, Alignment.CenterHorizontally,
) {
    Text("HUNTER SYSTEM", color = EnergySoft, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
    Spacer(Modifier.height(18.dp)); Text("GYMORA", color = HunterText, fontSize = 48.sp, fontWeight = FontWeight.Black, letterSpacing = 5.sp)
    Text("TRAIN · DISCIPLINE · EVOLVE", color = Energy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(28.dp)); Text("Level up in real life.", color = HunterText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(10.dp)); Text("A safe, guided fitness RPG. Start locally and connect cloud sync when you are ready.", color = Muted, textAlign = TextAlign.Center, lineHeight = 23.sp)
    Spacer(Modifier.height(30.dp)); EnergyButton("ENTER GYMORA", onEnter); Spacer(Modifier.height(10.dp)); Text("Guest mode · local-first", color = Muted, fontSize = 12.sp)
}

@Composable private fun BottomBar(current: String?, nav: NavHostController) = NavigationBar(containerColor = Color(0xFF030A18)) {
    listOf(Route.HOME, Route.QUEST, Route.TRAIN, Route.RANK, Route.MENU).forEach { route ->
        NavigationBarItem(current == route.path, { nav.navigate(route.path) { launchSingleTop = true } }, { Text(route.glyph, fontSize = 18.sp, fontWeight = FontWeight.Black) }, { Text(route.label, fontSize = 10.sp) })
    }
}

@Composable private fun Backdrop(content: @Composable ColumnScope.() -> Unit) = Column(
    Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Night, Color(0xFF06132A), Night))).verticalScroll(rememberScrollState()).windowInsetsPadding(WindowInsets.safeDrawing).padding(16.dp),
    Arrangement.spacedBy(12.dp), content = content,
)

@Composable private fun Header(title: String, nav: NavHostController) = Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    OutlinedButton({ nav.popBackStack() }, Modifier.size(44.dp), contentPadding = ButtonDefaults.ContentPadding) { Text("‹", fontSize = 24.sp) }
    Spacer(Modifier.width(12.dp)); Text(title, color = HunterText, fontSize = 22.sp, fontWeight = FontWeight.Black)
}

@Composable private fun Label(value: String, color: Color = Energy) = Text(value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)

@Composable private fun SystemCard(content: @Composable ColumnScope.() -> Unit) = Column(
    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Surface.copy(alpha = .96f)).border(1.dp, Color(0xFF214A7B), RoundedCornerShape(14.dp)).padding(16.dp),
    Arrangement.spacedBy(10.dp), content = content,
)

@Composable private fun EnergyButton(value: String, onClick: () -> Unit, secondary: Boolean = false) = Button(
    onClick, Modifier.fillMaxWidth().heightIn(min = 52.dp),
    colors = ButtonDefaults.buttonColors(containerColor = if (secondary) Raised else Energy, contentColor = if (secondary) HunterText else Night), shape = RoundedCornerShape(10.dp),
) { Text(value, fontWeight = FontWeight.Black) }

@Composable private fun XpBar(value: Float) = LinearProgressIndicator({ value.coerceIn(0f, 1f) }, Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(8.dp)), EnergySoft, Color(0xFF13253F))

@Composable private fun Home(vm: HunterViewModel, nav: NavHostController) = Backdrop {
    val h = vm.hunter
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) { Column { Label("GYMORA // HUNTER SYSTEM"); Text("Shadow Hunter", color = HunterText, fontSize = 24.sp, fontWeight = FontWeight.Black) }; Label("${h.rank}-RANK", Warning) }
    SystemCard { Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("LV. ${h.level}", color = HunterText, fontSize = 19.sp, fontWeight = FontWeight.Black); Text("${h.xp} / 500 XP", color = Muted) }; XpBar(h.xp / 500f); Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("🔥 ${h.streak} DAY STREAK", color = HunterText, fontWeight = FontWeight.Bold); Text("✓ ${h.completedQuests} QUESTS", color = HunterText, fontWeight = FontWeight.Bold) } }
    SystemCard { Label(if (vm.questComplete) "QUEST COMPLETE" else "TODAY'S QUEST"); Text(if (vm.questComplete) "SYSTEM REWARD SECURED" else vm.quest.title, color = HunterText, fontSize = 28.sp, fontWeight = FontWeight.Black); Text("Complete a balanced session. Stop for sharp or unusual pain.", color = Muted); vm.quest.objectives.forEach { o -> Row(Modifier.fillMaxWidth().heightIn(min = 36.dp), Alignment.CenterVertically) { Text(if (vm.questComplete) "✓" else "○", color = if (vm.questComplete) Success else EnergySoft, fontSize = 21.sp); Spacer(Modifier.width(10.dp)); Text(o.name, color = HunterText, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text(if (vm.questComplete) "Done" else o.target, color = Muted) } }; Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("+120 XP", color = Warning, fontWeight = FontWeight.Black); Text("+5 STR", color = Warning, fontWeight = FontWeight.Black); Text("+5 VIT", color = Warning, fontWeight = FontWeight.Black) }; EnergyButton(if (vm.questComplete) "VIEW SUMMARY" else "START TODAY'S QUEST") { nav.navigate(Route.QUEST.path) } }
    CloudCard(vm, nav)
}

@Composable private fun CloudCard(vm: HunterViewModel, nav: NavHostController) = SystemCard {
    Label("CLOUD SYNC", if (SupabaseGateway.configured) Energy else Warning)
    val text = when (val state = vm.cloudState) { CloudSyncState.LocalOnly -> if (SupabaseGateway.configured) "Supabase is configured. Sign in to sync across devices." else "Local-only mode. Add Supabase build properties to enable sync."; CloudSyncState.Syncing -> "Syncing your hunter profile…"; is CloudSyncState.Synced -> state.message; is CloudSyncState.Error -> state.message }
    Text(text, color = Muted, lineHeight = 21.sp)
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) { EnergyButton("ACCOUNT", { nav.navigate(Route.ACCOUNT.path) }, secondary = true); EnergyButton("SYNC", vm::syncToCloud) }
}

@Composable private fun Workout(vm: HunterViewModel, nav: NavHostController) = Backdrop {
    val objective = vm.quest.objectives[vm.currentObjective]
    Header("TODAY'S QUEST", nav); SystemCard { Label("WORKOUT PROGRESS"); Text("${if (vm.questComplete) 4 else vm.currentObjective + 1} / 4", color = HunterText, fontSize = 28.sp, fontWeight = FontWeight.Black); XpBar(if (vm.questComplete) 1f else vm.currentObjective / 4f) }
    SystemCard { Label("CURRENT EXERCISE"); Text(if (vm.questComplete) "Complete" else objective.name, color = HunterText, fontSize = 34.sp, fontWeight = FontWeight.Black); Text("Use controlled form. Stop for sharp pain, dizziness, or unusual discomfort.", color = Muted, fontSize = 16.sp, lineHeight = 24.sp); Text("TARGET  ${objective.target}     MODE  GUIDED", color = EnergySoft, fontWeight = FontWeight.Black) }
    SystemCard { Label("CAMERA ASSIST · MANUAL FALLBACK"); Text("Manual guided mode is active. No visual-verification claim is made until the camera pipeline is calibrated on supported devices.", color = Muted, lineHeight = 22.sp) }
    if (!vm.questComplete) EnergyButton(if (vm.currentObjective == vm.quest.objectives.lastIndex) "COMPLETE QUEST · +120 XP" else "COMPLETE EXERCISE", vm::completeCurrent) else SystemCard { Label("REWARD SECURED", Success); Text("Progress calculated and sent to cloud when an account is connected.", color = HunterText) }
    EnergyButton("RETURN HOME", { nav.navigate(Route.HOME.path) }, true)
}

@Composable private fun Training(nav: NavHostController) = ListScreen("TRAINING LIBRARY", nav, listOf("Bodyweight Squat · beginner", "Push-up · beginner", "Forearm Plank · timed", "Reverse Lunge · beginner"))
@Composable private fun Leaderboard(nav: NavHostController) = ListScreen("WEEKLY LEADERBOARD", nav, listOf("#1 Shadow Monarch · 98,420 XP", "#2 Iron Wolf · 96,810 XP", "#3 Nightfall · 94,230 XP", "#4 You · 91,540 XP"))

@Composable private fun ListScreen(title: String, nav: NavHostController, items: List<String>) = Backdrop { Header(title, nav); items.forEach { item -> SystemCard { Text(item, color = HunterText, fontSize = 18.sp, fontWeight = FontWeight.Black); Text("Safety and verification details appear before training.", color = Muted, fontSize = 12.sp) } } }

@Composable private fun Menu(nav: NavHostController) = Backdrop { Label("SYSTEM MENU"); Text("GYMORA", color = HunterText, fontSize = 30.sp, fontWeight = FontWeight.Black); Text("Hunter System controls and progress", color = Muted); listOf(Route.PROFILE, Route.ACHIEVEMENTS, Route.STATISTICS, Route.COACH, Route.RESTRICTIONS, Route.LEAVE, Route.SETTINGS, Route.ACCOUNT, Route.DEVELOPER, Route.ABOUT).forEach { r -> MenuRow(r.label, r.glyph) { nav.navigate(r.path) } } }

@Composable private fun MenuRow(label: String, glyph: String, onClick: () -> Unit) = Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Raised).clickable(onClick = onClick).padding(16.dp), Alignment.CenterVertically) { Text(glyph, color = EnergySoft, fontSize = 22.sp, modifier = Modifier.width(36.dp)); Text(label, color = HunterText, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text("›", color = Muted, fontSize = 24.sp) }

@Composable private fun Profile(vm: HunterViewModel, nav: NavHostController) = Backdrop { Header("HUNTER PROFILE", nav); SystemCard { Label("SHADOW HUNTER"); Text("${vm.hunter.rank}-RANK · LEVEL ${vm.hunter.level}", color = HunterText, fontSize = 24.sp, fontWeight = FontWeight.Black); XpBar(vm.hunter.xp / 500f); Text("${vm.hunter.xp} / 500 XP", color = Muted) }; SystemCard { Label("RPG ATTRIBUTES"); Stat("STR", "31"); Stat("VIT", "27"); Stat("END", "29"); Stat("AGI", "22") }; SystemCard { Label("REAL FITNESS"); Stat("Completed quests", vm.hunter.completedQuests.toString()); Stat("Current streak", "${vm.hunter.streak} days"); Stat("Mode", "Guided/manual") } }
@Composable private fun Stat(name: String, value: String) = Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text(name, color = Muted); Text(value, color = HunterText, fontWeight = FontWeight.Black) }

@Composable private fun Achievements(nav: NavHostController) = ListScreen("ACHIEVEMENTS", nav, listOf("★ First Step · Complete your first workout", "★ 7 Day Streak · Maintain consistency", "★ Discipline Master · Complete 30 workouts", "☆ Early Riser · Start before 7 AM", "☆ Shadow Rank · Reach A rank"))
@Composable private fun Statistics(vm: HunterViewModel, nav: NavHostController) = Backdrop { Header("STATISTICS", nav); SystemCard { Label("TRAINING OVERVIEW"); Stat("Workout days", vm.hunter.completedQuests.toString()); Stat("Current streak", "${vm.hunter.streak} days"); Stat("Average session", "18:24"); Stat("Form mode", "Guided/manual") }; SystemCard { Label("MUSCLE FOCUS"); Stat("Chest", "32%"); Stat("Legs", "28%"); Stat("Core", "22%"); Stat("Back", "18%") } }
@Composable private fun Coach(nav: NavHostController) = ListScreen("AI COACH", nav, listOf("How can I help you today?", "Suggest tomorrow's workout", "I'm feeling sore", "Adjust my plan", "Explain this exercise"))
@Composable private fun Restrictions(nav: NavHostController) = Backdrop { Header("APP RESTRICTIONS", nav); SystemCard { Label("CAPABILITY NOTICE", Warning); Text("Android app blocking is device-, permission-, and Play-policy-dependent. This build does not silently block applications.", color = HunterText, lineHeight = 23.sp); Text("A compliant adapter and emergency bypass must be validated before enablement.", color = Muted) }; listOf("Instagram", "YouTube", "TikTok", "Games", "Browser").forEach { SettingRow(it, false) } }
@Composable private fun Leave(nav: NavHostController) = Backdrop { Header("LEAVE / RECOVERY", nav); SystemCard { Label("RECOVERY FIRST"); Text("Never train through illness or injury to protect a streak.", color = HunterText); Text("Leave requests will pause requirements without deleting account data.", color = Muted) }; listOf("Vacation", "Travel", "Exams / Studies", "Work", "Recovery", "Unwell").forEach { SettingRow(it, false) } }

@Composable private fun Settings(nav: NavHostController) { var reducedMotion by rememberSaveable { mutableStateOf(false) }; var notifications by rememberSaveable { mutableStateOf(true) }; Backdrop { Header("SETTINGS", nav); MenuRow("Cloud account", "☁") { nav.navigate(Route.ACCOUNT.path) }; SettingRow("Notifications", notifications) { notifications = it }; SettingRow("Reduced motion", reducedMotion) { reducedMotion = it }; MenuRow("Privacy and data", "◈") {}; MenuRow("Permissions", "◉") {}; SystemCard { Label("ACCOUNT"); Text("Use Cloud account to sign in with Supabase or continue locally as a guest.", color = Muted) } } }
@Composable private fun SettingRow(label: String, enabled: Boolean, onChange: (Boolean) -> Unit = {}) = Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Raised).padding(16.dp), Alignment.CenterVertically) { Text(label, color = HunterText, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Switch(enabled, onChange) }

@Composable private fun Account(vm: HunterViewModel, nav: NavHostController) {
    var email by rememberSaveable { mutableStateOf("") }; var password by rememberSaveable { mutableStateOf("") }; var hunterName by rememberSaveable { mutableStateOf("Shadow Hunter") }; var create by rememberSaveable { mutableStateOf(false) }
    Backdrop { Header("CLOUD ACCOUNT", nav); SystemCard { Label("SUPABASE AUTH", if (SupabaseGateway.configured) Energy else Warning); Text(if (SupabaseGateway.configured) "Use your Supabase account to sync GYMORA across devices." else "Add supabase.url and supabase.publishableKey Gradle properties to enable this screen.", color = Muted, lineHeight = 22.sp); OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true); OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Password") }, singleLine = true); if (create) OutlinedTextField(hunterName, { hunterName = it }, Modifier.fillMaxWidth(), label = { Text("Hunter name") }, singleLine = true); EnergyButton(if (create) "CREATE ACCOUNT" else "SIGN IN") { if (create) vm.signUp(email, password, hunterName) else vm.signIn(email, password) }; TextButtonLike(if (create) "Already have an account? Sign in" else "New hunter? Create an account") { create = !create } }
    when (val state = vm.cloudState) { CloudSyncState.LocalOnly -> Text("Local mode", color = Muted); CloudSyncState.Syncing -> Text("Contacting Supabase…", color = EnergySoft); is CloudSyncState.Synced -> Text(state.message, color = Success); is CloudSyncState.Error -> Text(state.message, color = Danger) }
    if (SupabaseGateway.isAuthenticated()) EnergyButton("SIGN OUT", vm::signOut, secondary = true)
}

@Composable private fun TextButtonLike(value: String, onClick: () -> Unit) = Text(value, color = EnergySoft, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onClick).padding(8.dp))
@Composable private fun Developer(nav: NavHostController) = ListScreen("DEVELOPER SETTINGS", nav, listOf("AI Agent Configuration", "Model Source", "API Settings", "Local Models", "Model Marketplace", "Advanced Options", "Logs", "Experimental Features"))
@Composable private fun About(nav: NavHostController) = Backdrop { Header("ABOUT", nav); SystemCard { Label("GYMORA"); Text("v0.1.0", color = HunterText, fontSize = 28.sp, fontWeight = FontWeight.Black); Text("TRAIN · DISCIPLINE · EVOLVE", color = EnergySoft); Text("Project Hunter System", color = Muted) }; SystemCard { Label("ORIGINAL PRODUCT"); Text("GYMORA is an original fitness RPG identity. Production characters, artwork, music, and terminology remain original.", color = Muted, lineHeight = 22.sp) }; Text("Built with Kotlin and Jetpack Compose.", color = Muted, fontSize = 12.sp) }
