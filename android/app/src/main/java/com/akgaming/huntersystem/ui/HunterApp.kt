package com.akgaming.huntersystem.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.akgaming.huntersystem.ui.theme.*

private enum class Route(val path:String,val label:String){HOME("home","HOME"),QUEST("quest","QUEST"),TRAIN("train","TRAIN"),RANK("rank","RANK"),MENU("menu","MENU")}
private const val CAMERA="camera_workout"
private const val MODELS="model_store"

@Composable fun HunterApp(vm:HunterViewModel=viewModel()){
    val nav=rememberNavController()
    Scaffold(containerColor=Night,bottomBar={NavigationBar(containerColor=Color(0xFF020817)){Route.entries.forEach{r->NavigationBarItem(selected=nav.currentDestination?.route==r.path,onClick={nav.navigate(r.path){launchSingleTop=true;restoreState=true}},icon={Text(r.label.take(1),fontWeight=FontWeight.Black)},label={Text(r.label,fontSize=9.sp)})}}}){pad->
        NavHost(nav,Route.HOME.path,Modifier.padding(pad)){
            composable(Route.HOME.path){Home(vm){nav.navigate(Route.QUEST.path)}}
            composable(Route.QUEST.path){Workout(vm,{nav.navigate(CAMERA)}){nav.navigate(Route.HOME.path){popUpTo(Route.HOME.path){inclusive=true}}}}
            composable(Route.TRAIN.path){Training{nav.navigate(CAMERA)}}
            composable(Route.RANK.path){SimpleList("WEEKLY LEADERBOARD",listOf("#3 Nightfall · 94,230 XP","#4 You · 91,540 XP","#5 Void Runner · 89,770 XP"))}
            composable(Route.MENU.path){Menu({nav.navigate(MODELS)},{nav.navigate(CAMERA)})}
            composable(CAMERA){CameraWorkoutScreen{nav.popBackStack()}}
            composable(MODELS){ModelStoreScreen{nav.popBackStack()}}
        }
    }
}

@Composable private fun Backdrop(content:@Composable ColumnScope.()->Unit){
    val drift=rememberInfiniteTransition(label="aura").animateFloat(0f,1f,infiniteRepeatable(tween(4200,easing=LinearEasing),RepeatMode.Reverse),label="drift")
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF020611),Color(0xFF061A39),Color(0xFF020611)),startY=drift.value*280f)).verticalScroll(rememberScrollState()).windowInsetsPadding(WindowInsets.safeDrawing).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp),content=content)
}
@Composable private fun Label(value:String,color:Color=Energy)=Text(value,color=color,fontSize=12.sp,fontWeight=FontWeight.Black,letterSpacing=1.8.sp)
@Composable private fun SystemCard(content:@Composable ColumnScope.()->Unit){
    val pulse=rememberInfiniteTransition(label="edge").animateFloat(.35f,.9f,infiniteRepeatable(tween(1800),RepeatMode.Reverse),label="edgeAlpha")
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Brush.verticalGradient(listOf(Color(0xEE07152B),Color(0xEE030A17)))).border(1.dp,Energy.copy(alpha=pulse.value),RoundedCornerShape(16.dp)).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp),content=content)
}
@Composable private fun EnergyButton(value:String,onClick:()->Unit,secondary:Boolean=false)=Button(onClick,Modifier.fillMaxWidth().heightIn(min=52.dp),colors=ButtonDefaults.buttonColors(containerColor=if(secondary)Raised else Energy,contentColor=if(secondary)HunterText else Night),shape=RoundedCornerShape(10.dp)){Text(value,fontWeight=FontWeight.Black)}
@Composable private fun XpBar(value:Float)=LinearProgressIndicator({value.coerceIn(0f,1f)},Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(8.dp)),color=EnergySoft,trackColor=Color(0xFF13253F))

@Composable private fun Home(vm:HunterViewModel,start:()->Unit){val h=vm.hunter;Backdrop{
    Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween,Alignment.CenterVertically){Column{Label("GYMORA");Text(vm.displayName,color=HunterText,fontSize=25.sp,fontWeight=FontWeight.Black);Text("TRAIN · DISCIPLINE · EVOLVE",color=Muted,fontSize=9.sp,letterSpacing=1.3.sp)};Label("${h.rank}-RANK",Warning)}
    SystemCard{Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween){Text("LV. ${h.level}",color=HunterText,fontSize=19.sp,fontWeight=FontWeight.Black);Text("${h.xp} / 500 XP",color=Muted)};XpBar(h.xp/500f);Text("🔥 ${h.streak} DAY STREAK · ${h.completedQuests} QUESTS",color=HunterText,fontWeight=FontWeight.Bold)}
    SystemCard{Label(if(vm.questComplete)"QUEST COMPLETE" else "TODAY'S QUEST");AnimatedContent(vm.questComplete,label="quest-title"){done->Text(if(done)"SYSTEM REWARD SECURED" else vm.quest.title,color=HunterText,fontSize=27.sp,fontWeight=FontWeight.Black)};Text("Complete a balanced session. Stop for sharp or unusual pain.",color=Muted);vm.quest.objectives.forEach{o->Row(Modifier.fillMaxWidth().heightIn(min=36.dp),verticalAlignment=Alignment.CenterVertically){Text(if(vm.questComplete)"✓" else "○",color=if(vm.questComplete)Success else EnergySoft,fontSize=21.sp);Spacer(Modifier.width(10.dp));Text(o.name,color=HunterText,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));Text(if(vm.questComplete)"Done" else o.target,color=Muted)}};EnergyButton(if(vm.questComplete)"VIEW SUMMARY" else "START TODAY'S QUEST",start)}
}}

@Composable private fun Workout(vm:HunterViewModel,camera:()->Unit,home:()->Unit){val i=vm.currentObjective;val o=vm.quest.objectives[i];Backdrop{
    SystemCard{Label("WORKOUT PROGRESS");Text("${if(vm.questComplete)4 else i+1} / 4",color=HunterText,fontSize=28.sp,fontWeight=FontWeight.Black);XpBar(if(vm.questComplete)1f else i/4f)}
    SystemCard{Label("CURRENT EXERCISE");Text(if(vm.questComplete)"Complete" else o.name,color=HunterText,fontSize=34.sp,fontWeight=FontWeight.Black);ExerciseAnimation(if(vm.questComplete)"Recovery" else o.name);Text("Use controlled form. Stop for sharp pain, dizziness, or unusual discomfort.",color=Muted);Text("TARGET ${o.target} · GUIDED",color=EnergySoft,fontWeight=FontWeight.Black)}
    if(!vm.questComplete){EnergyButton("OPEN AI CAMERA GUIDANCE",camera);EnergyButton(if(i==vm.quest.objectives.lastIndex)"SUBMIT WORKOUT" else "COMPLETE EXERCISE",vm::completeCurrent,true)}
    EnergyButton("RETURN HOME",home,true)
}}

@Composable private fun Training(openCamera:()->Unit)=Backdrop{Label("TRAINING LIBRARY");SystemCard{Text("LIVE AI FORM COACH",color=HunterText,fontSize=20.sp,fontWeight=FontWeight.Black);Text("Camera frames stay on device. Manual mode remains available.",color=Muted);EnergyButton("OPEN CAMERA TRAINING",openCamera)};listOf("Squat · Guided","Push-up · Guided","Plank · Timed","Lunge · Guided").forEach{SystemCard{Text(it,color=HunterText,fontSize=17.sp,fontWeight=FontWeight.Bold)}}}
@Composable private fun Menu(models:()->Unit,camera:()->Unit)=Backdrop{Label("GYMORA SYSTEM");SystemCard{Text("AI & DEVELOPER",color=HunterText,fontSize=18.sp,fontWeight=FontWeight.Black);EnergyButton("LOCAL AI MODEL STORE",models);EnergyButton("CAMERA DIAGNOSTICS",camera,true)};listOf("Profile","Achievements","Statistics","AI Coach","App Restrictions","Leave / Recovery","Settings","Privacy & Data","About GYMORA").forEach{SystemCard{Text(it,color=HunterText,fontSize=17.sp,fontWeight=FontWeight.Bold)}}}
@Composable private fun SimpleList(title:String,items:List<String>)=Backdrop{Label(title);items.forEach{SystemCard{Text(it,color=HunterText,fontSize=17.sp,fontWeight=FontWeight.Bold)}}}
