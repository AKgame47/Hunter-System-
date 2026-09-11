package com.akgaming.huntersystem
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.akgaming.huntersystem.ui.HunterApp
import com.akgaming.huntersystem.ui.theme.HunterTheme
class MainActivity : ComponentActivity() { override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState);enableEdgeToEdge();setContent { HunterTheme { HunterApp() } } } }
