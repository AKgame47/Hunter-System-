package com.akgaming.huntersystem

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.akgaming.huntersystem.data.remote.SupabaseProvider
import com.akgaming.huntersystem.ui.HunterRoot
import com.akgaming.huntersystem.ui.theme.HunterTheme
import io.github.jan.supabase.auth.handleDeeplinks

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SupabaseProvider.client.handleDeeplinks(intent)
        enableEdgeToEdge()
        setContent {
            HunterTheme {
                HunterRoot()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        SupabaseProvider.client.handleDeeplinks(intent)
        recreate()
    }
}
