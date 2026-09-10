package com.akgaming.huntersystem.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.akgaming.huntersystem.domain.Progression
import com.akgaming.huntersystem.ui.theme.*

private enum class Route(val path:String,val label:String){HOME("home","HOME"),QUEST("quest","QUEST"),TRAIN("train","TRAIN"),RANK("rank","RANK"),MENU("menu","MENU")}
@Composable fun HunterApp(vm:HunterViewModel=viewModel()){
 val nav=rememberNavController()
 Scaffold(containerColor=Night,bottomBar={NavigationBar(containerColor=Color(0xFF030A18)){Route.entries.forEach{r->NavigationBarItem(selected=false,onClick={nav.navigate(r.path){launchSingleTop=true}},icon={Text(r.label.take(1),fontWeight=FontWeight.Black)},label={Text(r.label,fontSize=10.sp)})}}}){pad->
  NavHost(nav,Route.HOME.path,Modifier.padding(pad)){
   composable(Route.HOME.path){Home(vm){nav.navigate(Route.QUEST.path)}}
   composable(Route.QUEST.path){Workout(vm){nav.navigate(Route.HOME.path){popUpTo(Route.HOME.path){inclusive=true}}}}
   composable(Route.TRAIN.path){SimpleList("TRAINING LIBRARY",listOf("Squat · Guided","Push-up · Guided","Plank · Timed","Lunge · Guided"))}
   composable(Route.RANK.path){SimpleList("WEEKLY LEADERBOARD",listOf("#3 Nightfall · 94,230 XP","#4 You · 91,540 XP","#5 Void Runner · 89,770 XP"))}
   composable(Route.MENU.path){SimpleList("SYSTEM MENU",listOf("Profile","Achievements","Statistics","AI Coach","App Restrictions","Leave / Recovery","Settings","Developer Settings","About"))}
  }
 }
}
@Composable private fun Backdrop(content:@Composable ColumnScope.()->Unit)=Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Night,Color(0xFF06132A),Night))).verticalScroll(rememberScrollState()).windowInsetsPadding(WindowInsets.safeDrawing).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp),content=content)
@Composable private fun Label(value:String,color:Color=Energy)=Text(value,color=color,fontSize=12.sp,fontWeight=FontWeight.Black,letterSpacing=1.5.sp)
@Composable private fun SystemCard(content:@Composable ColumnScope.()->Unit)=Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Surface.copy(.95f)).border(1.dp,Color(0xFF214A7B),RoundedCornerShape(14.dp)).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp),content=content)
@Composable private fun EnergyButton(value:String,onClick:()->Unit,secondary:Boolean=false)=Button(onClick,Modifier.fillMaxWidth().heightIn(min=52.dp),colors=ButtonDefaults.buttonColors(containerColor=if(secondary)Raised else Energy,contentColor=if(secondary)HunterText else Night),shape=RoundedCornerShape(10.dp)){Text(value,fontWeight=FontWeight.Black)}
@Composable private fun XpBar(value:Float)=LinearProgressIndicator({value.coerceIn(0f,1f)},Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(8.dp)),color=EnergySoft,trackColor=Color(0xFF13253F))
@Composable private fun Home(vm:HunterViewModel,start:()->Unit){val h=vm.hunter;Backdrop{
 Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween,Alignment.CenterVertically){Column{Label("HUNTER SYSTEM");Text("Shadow Hunter",color=HunterText,fontSize=24.sp,fontWeight=FontWeight.Black)};Label("${h.rank}-RANK",Warning)}
 SystemCard{Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween){Text("LV. ${h.level}",color=HunterText,fontSize=19.sp,fontWeight=FontWeight.Black);Text("${h.xp} / 500 XP",color=Muted)};XpBar(h.xp/500f);Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween){Text("🔥 ${h.streak} DAY STREAK",color=HunterText,fontWeight=FontWeight.Bold);Text("✓ ${h.completedQuests} QUESTS",color=HunterText,fontWeight=FontWeight.Bold)}}
 SystemCard{Label(if(vm.questComplete)"QUEST COMPLETE" else "TODAY'S QUEST");Text(if(vm.questComplete)"SYSTEM REWARD SECURED" else vm.quest.title,color=HunterText,fontSize=28.sp,fontWeight=FontWeight.Black);Text("Complete a balanced session. Stop for sharp or unusual pain.",color=Muted);vm.quest.objectives.forEach{o->Row(Modifier.fillMaxWidth().heightIn(min=36.dp),verticalAlignment=Alignment.CenterVertically){Text(if(vm.questComplete)"✓" else "○",color=if(vm.questComplete)Success else EnergySoft,fontSize=21.sp);Spacer(Modifier.width(10.dp));Text(o.name,color=HunterText,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));Text(if(vm.questComplete)"Done" else o.target,color=Muted)}};Row(Modifier.fillMaxWidth(),Arrangement.SpaceBetween){Text("+120 XP",color=Warning,fontWeight=FontWeight.Black);Text("+5 STR",color=Warning,fontWeight=FontWeight.Black);Text("+5 VIT",color=Warning,fontWeight=FontWeight.Black)};EnergyButton(if(vm.questComplete)"VIEW SUMMARY" else "START TODAY'S QUEST",start)}
}}
@Composable private fun Workout(vm:HunterViewModel,home:()->Unit){val i=vm.currentObjective;val o=vm.quest.objectives[i];Backdrop{SystemCard{Label("WORKOUT PROGRESS");Text("${if(vm.questComplete)4 else i+1} / 4",color=HunterText,fontSize=28.sp,fontWeight=FontWeight.Black);XpBar(if(vm.questComplete)1f else i/4f)};SystemCard{Label("CURRENT EXERCISE");Text(if(vm.questComplete)"Complete" else o.name,color=HunterText,fontSize=34.sp,fontWeight=FontWeight.Black);Text("Use controlled form. Stop for sharp pain, dizziness, or unusual discomfort.",color=Muted,fontSize=16.sp,lineHeight=24.sp);Text("TARGET  ${o.target}     MODE  GUIDED",color=EnergySoft,fontWeight=FontWeight.Black)};SystemCard{Label("CAMERA ASSIST");Text("Not enabled in this build. Manual guided mode is active; no visual-verification claim is made.",color=Muted)};if(!vm.questComplete)EnergyButton(if(i==vm.quest.objectives.lastIndex)"COMPLETE QUEST · +120 XP" else "COMPLETE EXERCISE",vm::completeCurrent);EnergyButton("RETURN HOME",home,true)}}
@Composable private fun SimpleList(title:String,items:List<String>)=Backdrop{Label(title);items.forEach{SystemCard{Text(it,color=HunterText,fontSize=17.sp,fontWeight=FontWeight.Bold)}}}
