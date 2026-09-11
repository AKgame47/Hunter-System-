package com.akgaming.huntersystem.camera

import org.junit.Assert.*
import org.junit.Test

class PoseTrackingTest {
    private fun p(x: Float, y: Float, c: Float = .99f) = JointPoint(x, y, c)
    private fun squat(kneeBent: Boolean, time: Long): PoseSample {
        val kneeY = 1f; val ankle = if (kneeBent) p(1f, 1f) else p(0f, 2f)
        return PoseSample(mapOf(
            Joint.LEFT_HIP to p(0f,0f), Joint.LEFT_KNEE to p(0f,kneeY), Joint.LEFT_ANKLE to ankle,
            Joint.RIGHT_HIP to p(2f,0f), Joint.RIGHT_KNEE to p(2f,kneeY), Joint.RIGHT_ANKLE to if(kneeBent)p(1f,1f) else p(2f,2f)
        ), time)
    }
    @Test fun `squat counts only after stable down and up phases`() {
        val tracker = PoseTracker(ExerciseKind.SQUAT)
        repeat(3) { tracker.update(squat(true, it.toLong())) }
        repeat(2) { assertEquals(0, tracker.update(squat(false, (it+3).toLong())).reps) }
        assertEquals(1, tracker.update(squat(false, 6)).reps)
    }
    @Test fun `low confidence never awards a rep`() {
        val points = squat(true, 0).points.toMutableMap(); points[Joint.LEFT_KNEE] = p(0f,1f,.2f)
        val result = PoseTracker(ExerciseKind.SQUAT).update(PoseSample(points, 0))
        assertFalse(result.confident); assertEquals(0, result.reps)
    }
    @Test fun `angle returns expected right angle`() { assertEquals(90f, PoseMath.angle(p(1f,0f),p(0f,0f),p(0f,1f)), .01f) }
}
