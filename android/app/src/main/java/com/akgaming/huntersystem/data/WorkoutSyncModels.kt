package com.akgaming.huntersystem.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class WorkoutCompletionPayload(
    val sessionId: String,
    val idempotencyKey: String,
    val userId: String,
    val startedAt: String,
    val completedAt: String,
    val rewardXp: Int,
    val sets: List<WorkoutSetPayload>,
)
@Serializable data class WorkoutSetPayload(val id: String, val slug: String, val setNumber: Int, val completedReps: Int, val completedSeconds: Int, val verification: String, val confidence: Float? = null)
@Serializable data class ExerciseIdDto(val id: String, val slug: String)
@Serializable data class SessionUploadDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val status: String,
    val source: String,
    @SerialName("started_at") val startedAt: String,
    @SerialName("reward_xp") val rewardXp: Int,
)
@Serializable data class SetUploadDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("session_id") val sessionId: String,
    @SerialName("exercise_id") val exerciseId: String,
    @SerialName("set_number") val setNumber: Int,
    @SerialName("completed_reps") val completedReps: Int,
    @SerialName("completed_seconds") val completedSeconds: Int,
    val verification: String,
    val confidence: Float?,
    @SerialName("completed_at") val completedAt: String,
)
