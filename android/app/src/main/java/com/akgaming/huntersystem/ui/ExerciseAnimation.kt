package com.akgaming.huntersystem.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.akgaming.huntersystem.ui.theme.Energy
import com.akgaming.huntersystem.ui.theme.Night

@Composable
fun ExerciseAnimation(exerciseName: String, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "exercise")
    val phase = transition.animateFloat(0f, 1f, infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Reverse), label = "phase").value
    Canvas(modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(14.dp)).background(Night)) {
        val cx = size.width / 2f
        val ground = size.height * .82f
        val crouch = if (exerciseName.contains("Squat", true)) phase * size.height * .18f else 0f
        val plank = exerciseName.contains("Plank", true) || exerciseName.contains("Push", true)
        val head = if (plank) Offset(cx - size.width * .22f, ground - size.height * .22f - phase * 8f) else Offset(cx, size.height * .2f + crouch)
        val shoulder = if (plank) Offset(cx - size.width * .12f, ground - size.height * .18f) else Offset(cx, size.height * .34f + crouch)
        val hip = if (plank) Offset(cx + size.width * .10f, ground - size.height * .14f + phase * 8f) else Offset(cx, size.height * .55f + crouch)
        fun limb(a: Offset, b: Offset, color: Color = Energy) = drawLine(color, a, b, strokeWidth = 9f, cap = StrokeCap.Round)
        drawCircle(Color(0xFFF4F8FF), size.height * .055f, head)
        limb(shoulder, hip)
        if (plank) {
            val hand = Offset(cx - size.width * .14f, ground)
            limb(shoulder, hand)
            limb(hip, Offset(cx + size.width * .30f, ground))
        } else {
            val elbow = Offset(cx - size.width * .13f, size.height * (.45f + phase * .03f) + crouch)
            limb(shoulder, elbow); limb(elbow, Offset(cx - size.width * .06f, size.height * .58f + crouch))
            limb(shoulder, Offset(cx + size.width * .13f, size.height * .55f + crouch))
            val kneeSpread = if (exerciseName.contains("Squat", true)) size.width * (.08f + phase * .08f) else size.width * .08f
            val leftKnee = Offset(cx - kneeSpread, size.height * .68f + crouch * .25f)
            val rightKnee = Offset(cx + kneeSpread, size.height * .68f + crouch * .25f)
            limb(hip, leftKnee); limb(leftKnee, Offset(cx - size.width * .13f, ground))
            limb(hip, rightKnee); limb(rightKnee, Offset(cx + size.width * .13f, ground))
        }
        drawLine(Color(0xFF214A7B), Offset(size.width * .1f, ground), Offset(size.width * .9f, ground), 3f)
    }
}
