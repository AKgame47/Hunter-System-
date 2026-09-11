package com.akgaming.huntersystem.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.akgaming.huntersystem.ui.theme.Energy
import com.akgaming.huntersystem.ui.theme.HunterText
import com.akgaming.huntersystem.ui.theme.Muted
import com.akgaming.huntersystem.ui.theme.Night

@Composable
fun HunterRoot(vm: HunterViewModel = viewModel()) {
    when (vm.authState) {
        AuthState.LOADING -> Box(
            Modifier.fillMaxSize().background(Night),
            contentAlignment = Alignment.Center,
        ) { CircularProgressIndicator(color = Energy) }
        AuthState.SIGNED_OUT -> AuthScreen(vm)
        AuthState.CONFIRM_EMAIL -> ConfirmationScreen(vm)
        AuthState.AUTHENTICATED -> Box(Modifier.fillMaxSize()) {
            HunterApp(vm)
            TextButton(
                onClick = vm::signOut,
                modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(4.dp),
            ) { Text("SIGN OUT", color = Muted, fontSize = 10.sp) }
        }
    }
}

@Composable
private fun AuthScreen(vm: HunterViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Column(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Night, androidx.compose.ui.graphics.Color(0xFF06132A), Night)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 64.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("HUNTER SYSTEM", color = Energy, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
        Text("AWAKEN", color = HunterText, fontSize = 42.sp, fontWeight = FontWeight.Black)
        Text("Sign in to synchronize progression securely.", color = Muted)
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        vm.authMessage?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = HunterText)
        }
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = { vm.signIn(email, password) },
            enabled = !vm.authBusy,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (vm.authBusy) "CONNECTING…" else "SIGN IN", fontWeight = FontWeight.Black) }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = { vm.signUp(email, password) },
            enabled = !vm.authBusy,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("CREATE HUNTER ACCOUNT") }
        TextButton(
            onClick = { vm.sendPasswordReset(email) },
            enabled = !vm.authBusy,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) { Text("Forgot password?") }
    }
}

@Composable
private fun ConfirmationScreen(vm: HunterViewModel) {
    Column(
        Modifier.fillMaxSize().background(Night).padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("VERIFY YOUR EMAIL", color = Energy, fontSize = 25.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(16.dp))
        Text(vm.authMessage ?: "Open the confirmation link sent by Supabase.", color = HunterText)
        Spacer(Modifier.height(20.dp))
        Button(onClick = vm::returnToSignIn) { Text("RETURN TO SIGN IN") }
    }
}
