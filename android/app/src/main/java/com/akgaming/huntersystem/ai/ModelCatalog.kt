package com.akgaming.huntersystem.ai

import android.content.Context
import java.io.File

data class LocalModel(
    val id: String,
    val name: String,
    val license: String,
    val sourceUrl: String,
    val bytes: Long,
    val sha256: String,
    val minimumRamGb: Int,
    val runtimeFormat: String,
)

object ModelCatalog {
    val models = listOf(
        LocalModel(
            id = "qwen2_5_0_5b_q4km",
            name = "Qwen2.5 0.5B Instruct (Q4_K_M)",
            license = "Apache-2.0",
            sourceUrl = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/df5bf01389a39c743ab467d734bf501681e041c5/qwen2.5-0.5b-instruct-q4_k_m.gguf?download=true",
            bytes = 491_000_000L,
            sha256 = "74a4da8c9fdbcd15bd1f6d01d621410d31c6fc00986f5eb687824e7b93d7a9db",
            minimumRamGb = 4,
            runtimeFormat = "GGUF",
        ),
    )
    fun byId(id: String) = models.firstOrNull { it.id == id }
    fun finalFile(context: Context, model: LocalModel): File = File(File(context.noBackupFilesDir, "models"), "${model.id}.gguf")
}

interface LocalAiEngine {
    data class Capability(val supported: Boolean, val reason: String)
    fun capability(model: LocalModel): Capability
    suspend fun generate(prompt: String): String
}

class RuntimeCapabilityProbe : LocalAiEngine {
    override fun capability(model: LocalModel) = LocalAiEngine.Capability(
        supported = false,
        reason = "Model download is supported. A compatible signed LiteRT-LM/GGUF runtime is not bundled yet.",
    )
    override suspend fun generate(prompt: String): String = error("Local inference runtime is not installed")
}
