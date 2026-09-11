package com.akgaming.huntersystem.camera

import kotlin.math.acos
import kotlin.math.sqrt

enum class Joint { LEFT_SHOULDER, RIGHT_SHOULDER, LEFT_ELBOW, RIGHT_ELBOW, LEFT_WRIST, RIGHT_WRIST, LEFT_HIP, RIGHT_HIP, LEFT_KNEE, RIGHT_KNEE, LEFT_ANKLE, RIGHT_ANKLE }
enum class ExerciseKind { SQUAT, PUSH_UP, LUNGE, PLANK }
data class JointPoint(val x: Float, val y: Float, val confidence: Float)
data class PoseSample(val points: Map<Joint, JointPoint>, val timestampMs: Long)
data class TrackingResult(val reps: Int, val phase: String, val feedback: String, val confident: Boolean, val holdMillis: Long = 0)

object PoseMath {
    fun angle(a: JointPoint, b: JointPoint, c: JointPoint): Float {
        val bax = a.x - b.x; val bay = a.y - b.y
        val bcx = c.x - b.x; val bcy = c.y - b.y
        val denominator = sqrt((bax * bax + bay * bay) * (bcx * bcx + bcy * bcy))
        if (denominator <= 0.0001f) return Float.NaN
        return Math.toDegrees(acos(((bax * bcx + bay * bcy) / denominator).coerceIn(-1f, 1f)).toDouble()).toFloat()
    }
}

class PoseTracker(private val kind: ExerciseKind, private val minConfidence: Float = 0.65f) {
    private var reps = 0
    private var phase = "READY"
    private var candidate: String? = null
    private var candidateFrames = 0
    private var plankStartedAt: Long? = null

    fun update(sample: PoseSample): TrackingResult {
        val required = when (kind) {
            ExerciseKind.SQUAT, ExerciseKind.LUNGE -> listOf(Joint.LEFT_HIP, Joint.LEFT_KNEE, Joint.LEFT_ANKLE, Joint.RIGHT_HIP, Joint.RIGHT_KNEE, Joint.RIGHT_ANKLE)
            ExerciseKind.PUSH_UP -> listOf(Joint.LEFT_SHOULDER, Joint.LEFT_ELBOW, Joint.LEFT_WRIST, Joint.LEFT_HIP, Joint.LEFT_ANKLE)
            ExerciseKind.PLANK -> listOf(Joint.LEFT_SHOULDER, Joint.LEFT_HIP, Joint.LEFT_ANKLE)
        }
        if (required.any { (sample.points[it]?.confidence ?: 0f) < minConfidence }) {
            candidate = null; candidateFrames = 0; plankStartedAt = null
            return TrackingResult(reps, phase, "Move fully into frame", false)
        }
        return when (kind) {
            ExerciseKind.SQUAT -> repExercise(sample, downBelow = 100f, upAbove = 158f, "Keep knees tracking over toes")
            ExerciseKind.LUNGE -> repExercise(sample, downBelow = 105f, upAbove = 155f, "Keep the front knee stable")
            ExerciseKind.PUSH_UP -> pushUp(sample)
            ExerciseKind.PLANK -> plank(sample)
        }
    }

    private fun repExercise(sample: PoseSample, downBelow: Float, upAbove: Float, cue: String): TrackingResult {
        val left = PoseMath.angle(sample.points.getValue(Joint.LEFT_HIP), sample.points.getValue(Joint.LEFT_KNEE), sample.points.getValue(Joint.LEFT_ANKLE))
        val right = PoseMath.angle(sample.points.getValue(Joint.RIGHT_HIP), sample.points.getValue(Joint.RIGHT_KNEE), sample.points.getValue(Joint.RIGHT_ANKLE))
        val knee = (left + right) / 2f
        val next = when { knee < downBelow -> "DOWN"; knee > upAbove -> "UP"; else -> phase }
        commitPhase(next)
        return TrackingResult(reps, phase, if (phase == "DOWN") "Drive up with control" else cue, true)
    }

    private fun pushUp(sample: PoseSample): TrackingResult {
        val elbow = PoseMath.angle(sample.points.getValue(Joint.LEFT_SHOULDER), sample.points.getValue(Joint.LEFT_ELBOW), sample.points.getValue(Joint.LEFT_WRIST))
        val body = PoseMath.angle(sample.points.getValue(Joint.LEFT_SHOULDER), sample.points.getValue(Joint.LEFT_HIP), sample.points.getValue(Joint.LEFT_ANKLE))
        if (body < 150f) return TrackingResult(reps, phase, "Keep shoulders, hips, and ankles aligned", true)
        val next = when { elbow < 95f -> "DOWN"; elbow > 155f -> "UP"; else -> phase }
        commitPhase(next)
        return TrackingResult(reps, phase, "Lower with control", true)
    }

    private fun plank(sample: PoseSample): TrackingResult {
        val body = PoseMath.angle(sample.points.getValue(Joint.LEFT_SHOULDER), sample.points.getValue(Joint.LEFT_HIP), sample.points.getValue(Joint.LEFT_ANKLE))
        if (body < 160f) { plankStartedAt = null; return TrackingResult(reps, "ADJUST", "Straighten your body line", true) }
        val start = plankStartedAt ?: sample.timestampMs.also { plankStartedAt = it }
        return TrackingResult(reps, "HOLD", "Hold steady", true, (sample.timestampMs - start).coerceAtLeast(0))
    }

    private fun commitPhase(next: String) {
        if (next == phase) { candidate = null; candidateFrames = 0; return }
        if (candidate == next) candidateFrames++ else { candidate = next; candidateFrames = 1 }
        if (candidateFrames >= 3) {
            val previous = phase; phase = next
            if (previous == "DOWN" && next == "UP") reps++
            candidate = null; candidateFrames = 0
        }
    }
}
