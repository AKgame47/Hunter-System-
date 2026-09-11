package com.akgaming.huntersystem.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.akgaming.huntersystem.R
import com.akgaming.huntersystem.ui.theme.*

private enum class Route(val path:String,val label:String){HOME("home","HOME"),QUEST("quest","QUEST"),TRAIN("train","TRAIN"),RANK("rank","RANK"),MENU("menu","MENU")}
private const val CAMERA="camera";private const val MODELS="models";private const val PROFILE="profile";private const val ACHIEVEMENTS="achievements";private const val STATS="statistics";private const val RESTRICTIONS="restrictions";private const val RECOVERY="recovery";private const val SETTINGS="settings";private const val PRIVACY="privacy";private const val ABOUT="about"
@Composable fun HunterApp(vm:HunterViewModel=viewModel()){val nav=rememberNavController();Scaffold(containerColor=Night,bottomBar={NavigationBar(containerColor=Color(0xFF020817)){Route.entries.forEach{r->NavigationBarItem(selected=nav.currentDestination?.route==r.path,onClick={nav.navigate(r.path){launchSingleTop=true;restoreState=true}},icon={Text(r.label.take(1),fontWeight=FontWeight.Black)},label={Text(r.label,fontSize=9.sp)})}}}){pad->NavHost(nav,Route.HOME.path,Modifier.padding(pad)){composable(Route.HOME.path){Home(vm){nav.navigate(Route.QUEST.path)}};composable(Route.QUEST.path){Workout(vm,{nav.navigate(CAMERA)}){nav.navigate(Route.HOME.path)}};composable(Route.TRAIN.path){Training{nav.navigate(CAMERA)}};composable(Route.RANK.path){LeaderboardScreen(vm)};composable(Route.MENU.path){Menu{nav.navigate(it)}};composable(CAMERA){CameraWorkoutScreen{nav.popBackStack()}};composable(MODELS){ModelStoreScreen{nav.popBackStack()}};composable(PROFILE){ProfileScreen(vm)};composable(ACHIEVEMENTS){AchievementsScreen(vm)};composable(STATS){StatisticsScreen(vm)};composable(RESTRICTIONS){RestrictionsScreen(vm)};composable(RECOVERY){RecoveryScreen(vm)};composable(SETTINGS){SettingsScreen()};composable(PRIVACY){PrivacyScreen(vm)};composable(ABOUT){AboutScreen()}}}}
@Composable private fun Backdrop(content:@Composable ColumnScope.()->Unit){val drift=rememberInfiniteTransition(label="aura").animateFloat(0f,1f,infiniteRepeatable(tween(4200,easing=LinearEasing),RepeatMode.Reverse),label="drift");Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF020611),Color(0xFF061A39),Color(0xFF020611)),startY=drift.value*280f)).verticalScroll(rememberScrollState()).windowInsetsPadding(WindowInsets.safeDrawing).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp),content=content)}
@Composable private fun Label(v:String,c:Color=Energy)=Text(v,color=c,fontSize=12.sp,fontWeight=FontWeight.Black,letterSpacing=1.8.sp)
@Composable private fun SystemCard(content:@Composable ColumnScope.()->Unit){val p=rememberInfiniteTransition(label="edge").animateFloat(.35f,.9f,infiniteRepeatable(tween(1800),RepeatMode.Reverse),label="alpha");Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xEE07152B)).border(1.dp,Energy.copy(alpha=p.value),RoundedCornerShape(16.dp)).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp),content=content)}
@Composable private fun EnergyButton(v:String,onClick:()->Unit,secondary:Boolean=false)=Button(onClick,Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=if(secondary)Raised else Energy,contentColor=if(secondary)HunterText else Night)){Text(v,fontWeight=FontWeight.Black)}
@Composable private fun XpBar(v:Float)=LinearProgressIndicator({v.coerceIn(0f,1f)},Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(8.dp)),color=EnergySoft,trackColor=Color(0xFF13253F))
@Composable private fun Home(vm:HunterViewModel,start:()->Unit){val h=vm.hunter;Backdrop{Image(painterResource(R.drawable.gymora_hero),"GYMORA crystalline shield training artwork",Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(16.dp)),contentScale=ContentScale.Crop);Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween){Column{Label("GYMORA");Text(vm.displayName,color=HunterText,fontSize=25.sp,fontWeight=FontWeight.Black)};Label("${h.rank}-RANK",Warning)};SystemCard{Text("LV. ${h.level}",color=HunterText,fontWeight=FontWeight.Black);XpBar(h.xp/500f);Text("🔥 ${h.streak} DAY STREAK · ${h.completedQuests} QUESTS",color=HunterText)};SystemCard{Label(if(vm.questComplete)"QUEST COMPLETE" else "TODAY'S QUEST");AnimatedContent(vm.questComplete,label="quest"){Text(if(it)"SYSTEM REWARD SECURED" else vm.quest.title,color=HunterText,fontSize=25.sp,fontWeight=FontWeight.Black)};vm.quest.objectives.forEach{Text("${if(vm.questComplete)"✓" else "○"} ${it.name} · ${if(vm.questComplete)"Done" else it.target}",color=HunterText)};EnergyButton(if(vm.questComplete)"VIEW SUMMARY" else "START TODAY'S QUEST",start)}}}
@Composable private fun Workout(vm:HunterViewModel,camera:()->Unit,home:()->Unit){val o=vm.quest.objectives[vm.currentObjective];Backdrop{SystemCard{Label("WORKOUT PROGRESS");XpBar(if(vm.questComplete)1f else vm.currentObjective/4f)};SystemCard{Text(if(vm.questComplete)"Complete" else o.name,color=HunterText,fontSize=32.sp,fontWeight=FontWeight.Black);ExerciseAnimation(if(vm.questComplete)"Recovery" else o.name);Text("Stop for sharp pain, dizziness, or unusual discomfort.",color=Muted)};if(!vm.questComplete){EnergyButton("OPEN AI CAMERA GUIDANCE",camera);EnergyButton(if(vm.currentObjective==3)"SUBMIT WORKOUT" else "COMPLETE EXERCISE",vm::completeCurrent,true)};EnergyButton("RETURN HOME",home,true)}}
@Composable private fun Training(open:()->Unit)=Backdrop{Label("TRAINING LIBRARY");SystemCard{Text("LIVE AI FORM COACH",color=HunterText,fontWeight=FontWeight.Black);Text("Camera frames stay on device.",color=Muted);EnergyButton("OPEN CAMERA TRAINING",open)};listOf("Squat","Push-up","Plank","Lunge").forEach{SystemCard{Text("$it · Guided",color=HunterText,fontWeight=FontWeight.Bold)}}}
@Composable private fun Menu(go:(String)->Unit)=Backdrop{Label("GYMORA SYSTEM");listOf("Local AI Model Store" to MODELS,"Profile" to PROFILE,"Achievements" to ACHIEVEMENTS,"Statistics" to STATS,"App Restrictions" to RESTRICTIONS,"Leave / Recovery" to RECOVERY,"Settings" to SETTINGS,"Privacy & Data" to PRIVACY,"About GYMORA" to ABOUT).forEach{(name,route)->Card(onClick={go(route)},modifier=Modifier.fillMaxWidth()){Text(name,Modifier.padding(18.dp),color=HunterText,fontWeight=FontWeight.Bold)}}}
