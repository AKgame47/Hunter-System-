package com.akgaming.huntersystem.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.akgaming.huntersystem.ui.theme.*

@Composable fun HunterRoot(vm: HunterViewModel = viewModel()) {
    when (vm.authState) {
        AuthState.LOADING -> BrandLoading()
        AuthState.SIGNED_OUT -> AuthScreen(vm)
        AuthState.CONFIRM_EMAIL -> ConfirmationScreen(vm)
        AuthState.AUTHENTICATED -> Box(Modifier.fillMaxSize()) {
            HunterApp(vm)
            TextButton(onClick=vm::signOut,modifier=Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(4.dp)){Text("SIGN OUT",color=Muted,fontSize=10.sp)}
        }
    }
}

@Composable private fun BrandLoading(){
    val pulse=rememberInfiniteTransition(label="gymora-pulse").animateFloat(.45f,1f,infiniteRepeatable(tween(900),RepeatMode.Reverse),label="pulse")
    Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Energy.copy(alpha=pulse.value*.22f),Night,Color.Black))),contentAlignment=Alignment.Center){
        Column(horizontalAlignment=Alignment.CenterHorizontally){Text("G",color=Color.White,fontSize=76.sp,fontWeight=FontWeight.Black);Text("GYMORA",color=Energy,fontSize=24.sp,fontWeight=FontWeight.Black,letterSpacing=5.sp);Text("TRAIN · DISCIPLINE · EVOLVE",color=Muted,fontSize=10.sp,letterSpacing=2.sp)}
    }
}

@Composable private fun AuthScreen(vm: HunterViewModel){
    var email by remember{mutableStateOf("")};var password by remember{mutableStateOf("")}
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Night,Color(0xFF071A3A),Night))).verticalScroll(rememberScrollState()).padding(horizontal=24.dp,vertical=56.dp),verticalArrangement=Arrangement.Center){
        Text("GYMORA",color=Energy,fontWeight=FontWeight.Black,letterSpacing=5.sp,fontSize=20.sp)
        Text("AWAKEN",color=HunterText,fontSize=44.sp,fontWeight=FontWeight.Black)
        Text("Train · Discipline · Evolve",color=Muted,letterSpacing=1.sp)
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(email,{email=it},label={Text("Email")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Email),singleLine=true,modifier=Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(password,{password=it},label={Text("Password")},visualTransformation=PasswordVisualTransformation(),singleLine=true,modifier=Modifier.fillMaxWidth())
        vm.authMessage?.let{Spacer(Modifier.height(12.dp));Text(it,color=HunterText)}
        Spacer(Modifier.height(18.dp))
        Button({vm.signIn(email,password)},enabled=!vm.authBusy,modifier=Modifier.fillMaxWidth()){Text(if(vm.authBusy)"CONNECTING…" else "ENTER GYMORA",fontWeight=FontWeight.Black)}
        Spacer(Modifier.height(10.dp))
        OutlinedButton({vm.signUp(email,password)},enabled=!vm.authBusy,modifier=Modifier.fillMaxWidth()){Text("CREATE ACCOUNT")}
        TextButton({vm.sendPasswordReset(email)},enabled=!vm.authBusy,modifier=Modifier.align(Alignment.CenterHorizontally)){Text("Forgot password?")}
    }
}

@Composable private fun ConfirmationScreen(vm:HunterViewModel)=Column(Modifier.fillMaxSize().background(Night).padding(32.dp),verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally){
    Text("VERIFY YOUR EMAIL",color=Energy,fontSize=25.sp,fontWeight=FontWeight.Black);Spacer(Modifier.height(16.dp));Text(vm.authMessage?:"Open the confirmation link sent by GYMORA.",color=HunterText);Spacer(Modifier.height(20.dp));Button(vm::returnToSignIn){Text("RETURN TO SIGN IN")}
}
