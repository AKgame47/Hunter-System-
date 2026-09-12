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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    HOME("home", "HOME", "⌂"),
    QUEST("quest", "QUEST", "✓"),
    TRAIN("train", "TRAIN", "✦"),
    RANK("rank", "RANK", "♜"),
    MENU("menu", "MENU", "≡"),
    PROFILE("profile", "PROFILE", "◉"),
    ACHIEVEMENTS("achievements", "ACHIEVEMENTS", "★"),
    STATISTICS("statistics", "STATISTICS", "▥"),
    COACH("coach", "AI COACH", "◌"),
    RESTRICTIONS("restrictions", "APP RESTRICTIONS", "⊘"),
    LEAVE("leave", "LEAVE / RECOVERY", "☾"),
    SETTINGS("settings", "SETTINGS", "⚙"),
    DEVELOPER("developer", "DEVELOPER SETTINGS", "⌘"),
    ABOUT("about", "ABOUT", "ⓘ"),
}

@Composable
fun HunterApp(vm: HunterViewModel = viewModel()) {
    var entered by rememberSaveable { mutableStateOf(false) }
    if (!entered) {
        Onboarding(onEnter = { entered = true })
    } else {
        val nav = rememberNavController()
        val entry by nav.currentBackStackEntryAsState()
        val currentRoute = entry?.destination?.route
        Scaffold(
            containerColor = Night,
            bottomBar = { BottomBar(currentRoute = currentRoute, nav = nav) },
        ) { padding ->
            NavHost(
                navController = nav,
                startDestination = Route.HOME.path,
                modifier = Modifier.padding(padding),
            ) {
                composable(Route.HOME.path) { Home(vm, nav) }
                composable(Route.QUEST.path) { Workout(vm, nav) }
                composable(Route.TRAIN.path) { TrainingLibrary(nav) }
                composable(Route.RANK.path) { Leaderboard(nav) }
                composable(Route.MENU.path) { SystemMenu(nav) }
                composable(Route.PROFILE.path) { Profile(vm, nav) }
                composable(Route.ACHIEVEMENTS.path) { Achievements(nav) }
                composable(Route.STATISTICS.path) { Statistics(vm, nav) }
                composable(Route.COACH.path) { Coach(nav) }
                composable(Route.RESTRICTIONS.path) { Restrictions(nav) }
                composable(Route.LEAVE.path) { LeaveRecovery(nav) }
                composable(Route.SETTINGS.path) { Settings(nav) }
                composable(Route.DEVELOPER.path) { DeveloperSettings(nav) }
                composable(Route.ABOUT.path) { About(nav) }
            }
        }
    }
}

@Composable
private fun Onboarding(onEnter: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Night, Color(0xFF061A38), Night)))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("HUNTER SYSTEM", color = EnergySoft, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
        Spacer(Modifier.height(18.dp))
        Text("GYMORA", color = HunterText, fontSize = 48.sp, fontWeight = FontWeight.Black, letterSpacing = 5.sp)
        Text("TRAIN · DISCIPLINE · EVOLVE", color = Energy, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
        Spacer(Modifier.height(28.dp))
        Text("Level up in real life.", color = HunterText, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text("A safe, guided fitness RPG. Complete your first quest manually; camera and cloud features are optional expansions.", color = Muted, textAlign = TextAlign.Center, lineHeight = 23.sp)
        Spacer(Modifier.height(32.dp))
        EnergyButton("ENTER GYMORA", onEnter)
        Spacer(Modifier.height(10.dp))
        Text("Guest mode · local-first preview", color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun BottomBar(currentRoute: String?, nav: NavHostController) {
    NavigationBar(containerColor = Color(0xFF030A18)) {
        listOf(Route.HOME, Route.QUEST, Route.TRAIN, Route.RANK, Route.MENU).forEach { route ->
            NavigationBarItem(
                selected = currentRoute == route.path,
                onClick = { nav.navigate(route.path) { launchSingleTop = true } },
                icon = { Text(route.glyph, fontSize = 18.sp, fontWeight = FontWeight.Black) },
                label = { Text(route.label, fontSize = 10.sp) },
            )
        }
    }
}

@Composable
private fun Backdrop(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Night, Color(0xFF06132A), Night)))
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
private fun Header(title: String, nav: NavHostController) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(onClick = { nav.popBackStack() }, modifier = Modifier.size(44.dp), contentPadding = ButtonDefaults.ContentPadding) {
            Text("‹", fontSize = 24.sp, color = HunterText)
        }
        Spacer(Modifier.width(12.dp))
        Text(title, color = HunterText, fontSize = 22.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun Label(value: String, color: Color = Energy) = Text(value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)

@Composable
private fun SystemCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface.copy(alpha = .96f))
            .border(1.dp, Color(0xFF214A7B), RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

@Composable
private fun EnergyButton(value: String, onClick: () -> Unit, secondary: Boolean = false) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (secondary) Raised else Energy, contentColor = if (secondary) HunterText else Night),
        shape = RoundedCornerShape(10.dp),
    ) { Text(value, fontWeight = FontWeight.Black) }
}

@Composable
private fun XpBar(value: Float) = LinearProgressIndicator(
    progress = { value.coerceIn(0f, 1f) },
    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(8.dp)),
    color = EnergySoft,
    trackColor = Color(0xFF13253F),
)

@Composable
private fun Home(vm: HunterViewModel, nav: NavHostController) {
    val h = vm.hunter
    Backdrop {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column { Label("GYMORA // HUNTER SYSTEM"); Text("Shadow Hunter", color = HunterText, fontSize = 24.sp, fontWeight = FontWeight.Black) }
            Label("${h.rank}-RANK", Warning)
        }
        SystemCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("LV. ${h.level}", color = HunterText, fontSize = 19.sp, fontWeight = FontWeight.Black)
                Text("${h.xp} / 500 XP", color = Muted)
            }
            XpBar(h.xp / 500f)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("🔥 ${h.streak} DAY STREAK", color = HunterText, fontWeight = FontWeight.Bold)
                Text("✓ ${h.completedQuests} QUESTS", color = HunterText, fontWeight = FontWeight.Bold)
            }
        }
        SystemCard {
            Label(if (vm.questComplete) "QUEST COMPLETE" else "TODAY'S QUEST")
            Text(if (vm.questComplete) "SYSTEM REWARD SECURED" else vm.quest.title, color = HunterText, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Text("Complete a balanced session. Stop for sharp or unusual pain.", color = Muted)
            vm.quest.objectives.forEach { objective ->
                Row(Modifier.fillMaxWidth().heightIn(min = 36.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (vm.questComplete) "✓" else "○", color = if (vm.questComplete) Success else EnergySoft, fontSize = 21.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(objective.name, color = HunterText, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(if (vm.questComplete) "Done" else objective.target, color = Muted)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("+120 XP", color = Warning, fontWeight = FontWeight.Black)
                Text("+5 STR", color = Warning, fontWeight = FontWeight.Black)
                Text("+5 VIT", color = Warning, fontWeight = FontWeight.Black)
            }
            EnergyButton(if (vm.questComplete) "VIEW SUMMARY" else "START TODAY'S QUEST") { nav.navigate(Route.QUEST.path) }
        }
        SystemCard {
            Label("REAL FITNESS / RPG SEPARATION")
            Text("RPG rank is a game signal. Train at a safe pace and use the stop control whenever something feels wrong.", color = Muted, lineHeight = 22.sp)
        }
    }
}

@Composable
private fun Workout(vm: HunterViewModel, nav: NavHostController) {
    val index = vm.currentObjective
    val objective = vm.quest.objectives[index]
    Backdrop {
        Header("TODAY'S QUEST", nav)
        SystemCard {
            Label("WORKOUT PROGRESS")
            Text("${if (vm.questComplete) 4 else index + 1} / 4", color = HunterText, fontSize = 28.sp, fontWeight = FontWeight.Black)
            XpBar(if (vm.questComplete) 1f else index / 4f)
        }
        SystemCard {
            Label("CURRENT EXERCISE")
            Text(if (vm.questComplete) "Complete" else objective.name, color = HunterText, fontSize = 34.sp, fontWeight = FontWeight.Black)
            Text("Use controlled form. Stop for sharp pain, dizziness, or unusual discomfort.", color = Muted, fontSize = 16.sp, lineHeight = 24.sp)
            Text("TARGET  ${objective.target}     MODE  GUIDED", color = EnergySoft, fontWeight = FontWeight.Black)
        }
        SystemCard {
            Label("CAMERA ASSIST · MANUAL FALLBACK")
            Text("Manual guided mode is active in this build. No visual-verification claim is made until the camera pipeline is calibrated on supported devices.", color = Muted, lineHeight = 22.sp)
        }
        if (!vm.questComplete) {
            EnergyButton(if (index == vm.quest.objectives.lastIndex) "COMPLETE QUEST · +120 XP" else "COMPLETE EXERCISE") { vm.completeCurrent() }
        } else {
            SystemCard { Label("REWARD SECURED", Success); Text("Your progress was calculated with verification weighting.", color = HunterText) }
        }
        EnergyButton("RETURN HOME", { nav.navigate(Route.HOME.path) { popUpTo(Route.HOME.path) } }, secondary = true)
    }
}

@Composable
private fun TrainingLibrary(nav: NavHostController) = Backdrop {
    Header("TRAINING LIBRARY", nav)
    Label("GUIDED EXERCISES")
    listOf("Bodyweight Squat" to "Lower body · beginner", "Push-up" to "Upper body · beginner", "Forearm Plank" to "Core · timed", "Reverse Lunge" to "Balance · beginner").forEach { (name, detail) ->
        SystemCard { Text(name, color = HunterText, fontSize = 18.sp, fontWeight = FontWeight.Black); Text(detail, color = Muted); Text("Safety notes and camera capability are shown before training.", color = EnergySoft, fontSize = 12.sp) }
    }
}

@Composable
private fun Leaderboard(nav: NavHostController) = Backdrop {
    Header("WEEKLY LEADERBOARD", nav)
    Label("SERVER-VALIDATED PREVIEW")
    listOf("#1  Shadow Monarch" to "98,420 XP", "#2  Iron Wolf" to "96,810 XP", "#3  Nightfall" to "94,230 XP", "#4  You" to "91,540 XP", "#5  Void Runner" to "89,770 XP").forEach { (name, score) ->
        SystemCard { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(name, color = if (name.contains("You")) EnergySoft else HunterText, fontWeight = FontWeight.Bold); Text(score, color = Warning, fontWeight = FontWeight.Black) } }
    }
    Text("Competitive scores must be calculated and privacy-filtered on the server before release.", color = Muted, fontSize = 12.sp)
}

@Composable
private fun SystemMenu(nav: NavHostController) = Backdrop {
    Label("SYSTEM MENU")
    Text("GYMORA", color = HunterText, fontSize = 30.sp, fontWeight = FontWeight.Black)
    Text("Hunter System controls and progress", color = Muted)
    listOf(Route.PROFILE, Route.ACHIEVEMENTS, Route.STATISTICS, Route.COACH, Route.RESTRICTIONS, Route.LEAVE, Route.SETTINGS, Route.DEVELOPER, Route.ABOUT).forEach { route ->
        MenuRow(route.label, route.glyph) { nav.navigate(route.path) }
    }
}

@Composable
private fun MenuRow(label: String, glyph: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Raised).clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(glyph, color = EnergySoft, fontSize = 22.sp, modifier = Modifier.width(36.dp))
        Text(label, color = HunterText, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text("›", color = Muted, fontSize = 24.sp)
    }
}

@Composable
private fun Profile(vm: HunterViewModel, nav: NavHostController) = Backdrop {
    Header("HUNTER PROFILE", nav)
    SystemCard { Label("SHADOW HUNTER"); Text("${vm.hunter.rank}-RANK · LEVEL ${vm.hunter.level}", color = HunterText, fontSize = 24.sp, fontWeight = FontWeight.Black); XpBar(vm.hunter.xp / 500f); Text("${vm.hunter.xp} / 500 XP", color = Muted) }
    SystemCard { Label("RPG ATTRIBUTES"); StatLine("STR", "31"); StatLine("VIT", "27"); StatLine("END", "29"); StatLine("AGI", "22") }
    SystemCard { Label("REAL FITNESS"); StatLine("Completed quests", vm.hunter.completedQuests.toString()); StatLine("Current streak", "${vm.hunter.streak} days"); StatLine("Verification", "Manual guided") }
}

@Composable
private fun StatLine(name: String, value: String) = Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(name, color = Muted); Text(value, color = HunterText, fontWeight = FontWeight.Black) }

@Composable
private fun Achievements(nav: NavHostController) = Backdrop {
    Header("ACHIEVEMENTS", nav)
    listOf("First Step" to "Complete your first workout.", "7 Day Streak" to "Maintain a seven-day streak.", "Discipline Master" to "Complete 30 workouts.", "Early Riser" to "Start a workout before 7 AM.", "Iron Will" to "Train consistently for 30 days.", "Shadow Rank" to "Reach A rank.").forEachIndexed { index, (name, detail) ->
        SystemCard { Row(verticalAlignment = Alignment.CenterVertically) { Text(if (index < 3) "★" else "☆", color = if (index < 3) Warning else Muted, fontSize = 24.sp); Spacer(Modifier.width(12.dp)); Column { Text(name, color = HunterText, fontWeight = FontWeight.Black); Text(detail, color = Muted, fontSize = 12.sp) } } }
    }
}

@Composable
private fun Statistics(vm: HunterViewModel, nav: NavHostController) = Backdrop {
    Header("STATISTICS", nav)
    Label("TRAINING OVERVIEW")
    SystemCard { StatLine("Workout days", vm.hunter.completedQuests.toString()); StatLine("Current streak", "${vm.hunter.streak} days"); StatLine("Average session", "18:24"); StatLine("Form mode", "Guided/manual") }
    SystemCard { Label("MUSCLE FOCUS"); StatLine("Chest", "32%"); StatLine("Legs", "28%"); StatLine("Core", "22%"); StatLine("Back", "18%") }
    Text("Statistics become server-backed after the sync and workout history phases.", color = Muted, fontSize = 12.sp)
}

@Composable
private fun Coach(nav: NavHostController) = Backdrop {
    Header("AI COACH", nav)
    SystemCard { Label("COACH PREVIEW"); Text("How can I help you today, Hunter?", color = HunterText, fontSize = 22.sp, fontWeight = FontWeight.Black); Text("This build uses safe, deterministic guidance. Connect an approved provider only after output validation and privacy controls are implemented.", color = Muted, lineHeight = 22.sp) }
    listOf("Suggest tomorrow's workout", "I'm feeling sore", "Adjust my plan", "Explain this exercise", "Motivate me").forEach { action -> MenuRow(action, "•") { } }
}

@Composable
private fun Restrictions(nav: NavHostController) = Backdrop {
    Header("APP RESTRICTIONS", nav)
    SystemCard { Label("CAPABILITY NOTICE", Warning); Text("Android app blocking is device-, permission-, and Play-policy-dependent. This build does not silently block applications.", color = HunterText, lineHeight = 23.sp); Text("A compliant restriction adapter and emergency bypass must be validated before this feature is enabled.", color = Muted) }
    listOf("Instagram", "YouTube", "TikTok", "Games", "Browser").forEach { app -> SettingRow(app, enabled = false) }
}

@Composable
private fun LeaveRecovery(nav: NavHostController) = Backdrop {
    Header("LEAVE / RECOVERY", nav)
    SystemCard { Label("RECOVERY FIRST"); Text("A missed workout should never encourage training through illness or injury.", color = HunterText, lineHeight = 23.sp); Text("Leave requests will pause requirements according to a documented policy without deleting your account or progress.", color = Muted) }
    listOf("Vacation", "Travel", "Exams / Studies", "Work", "Personal", "Recovery", "Unwell").forEach { reason -> SettingRow(reason, enabled = false) }
}

@Composable
private fun Settings(nav: NavHostController) {
    var reducedMotion by rememberSaveable { mutableStateOf(false) }
    var notifications by rememberSaveable { mutableStateOf(true) }
    Backdrop {
        Header("SETTINGS", nav)
        SettingRow("Notifications", notifications) { notifications = it }
        SettingRow("Reduced motion", reducedMotion) { reducedMotion = it }
        MenuRow("Privacy and data", "◈") { }
        MenuRow("Permissions", "◉") { }
        MenuRow("Language · English", "文") { }
        SystemCard { Label("ACCOUNT"); Text("Guest mode is active in this vertical slice. Account linking will be added with the durable identity phase.", color = Muted) }
    }
}

@Composable
private fun SettingRow(label: String, enabled: Boolean, onChange: (Boolean) -> Unit = {}) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Raised).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = HunterText, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Switch(checked = enabled, onCheckedChange = onChange)
    }
}

@Composable
private fun DeveloperSettings(nav: NavHostController) = Backdrop {
    Header("DEVELOPER SETTINGS", nav)
    listOf("AI Agent Configuration", "Model Source", "API Settings", "Local Models", "Model Marketplace", "Advanced Options", "Logs", "Experimental Features").forEach { setting -> MenuRow(setting, "⌘") { } }
    SystemCard { Label("SAFE DEFAULTS", Success); Text("Secrets are not shown in this screen. Local inference, model signatures, diagnostics, and cloud provider calls require their own validated implementations.", color = Muted, lineHeight = 22.sp) }
}

@Composable
private fun About(nav: NavHostController) = Backdrop {
    Header("ABOUT", nav)
    SystemCard { Label("GYMORA"); Text("v0.1.0", color = HunterText, fontSize = 28.sp, fontWeight = FontWeight.Black); Text("TRAIN · DISCIPLINE · EVOLVE", color = EnergySoft); Text("Project Hunter System", color = Muted) }
    SystemCard { Label("ORIGINAL PRODUCT"); Text("GYMORA is an original fitness RPG identity. It may use a cinematic leveling mood, but production characters, artwork, music, and terminology must remain original.", color = Muted, lineHeight = 22.sp) }
    Text("Built with Kotlin and Jetpack Compose.", color = Muted, fontSize = 12.sp)
}
