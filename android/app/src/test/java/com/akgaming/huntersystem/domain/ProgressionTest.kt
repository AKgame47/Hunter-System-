package com.akgaming.huntersystem.domain
import org.junit.Assert.*
import org.junit.Test
class ProgressionTest{private val base=Hunter(level=1,xp=450,rank=HunterRank.E,streak=2,completedQuests=4);@Test fun questLevelsAndCarriesXp(){val r=Progression.awardQuest(base,120,1.0);assertEquals(2,r.level);assertEquals(70,r.xp);assertEquals(3,r.streak)};@Test fun unverifiedEarnsReducedReward(){assertEquals(10,Progression.awardQuest(base,100,0.0).xp)};@Test(expected=IllegalArgumentException::class)fun negativeRewardRejected(){Progression.awardQuest(base,-1,1.0)}}
