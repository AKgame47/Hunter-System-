package com.akgaming.huntersystem.ui.theme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
val Night=Color(0xFF030815);val Surface=Color(0xFF071225);val Raised=Color(0xFF0B1B34);val Energy=Color(0xFF52A8FF);val EnergySoft=Color(0xFF82C7FF);val HunterText=Color(0xFFF4F8FF);val Muted=Color(0xFFA7B7D2);val Warning=Color(0xFFFFB454);val Success=Color(0xFF42D392);val Danger=Color(0xFFFF5D67)
private val colors=darkColorScheme(primary=Energy,onPrimary=Night,secondary=EnergySoft,background=Night,onBackground=HunterText,surface=Surface,onSurface=HunterText,error=Danger)
@Composable fun HunterTheme(content:@Composable()->Unit)=MaterialTheme(colorScheme=colors,content=content)
