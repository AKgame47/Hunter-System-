package com.akgaming.huntersystem.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.akgaming.huntersystem.domain.Hunter
import com.akgaming.huntersystem.domain.Progression
import com.akgaming.huntersystem.domain.Quest

class HunterViewModel:ViewModel(){
    var hunter by mutableStateOf(Hunter());private set
    var quest by mutableStateOf(Quest());private set
    var currentObjective by mutableIntStateOf(0);private set
    var questComplete by mutableStateOf(false);private set
    fun completeCurrent(){
        if(questComplete)return
        if(currentObjective<quest.objectives.lastIndex) currentObjective++
        else { hunter=Progression.awardQuest(hunter,quest.rewardXp,.85);questComplete=true }
    }
}
