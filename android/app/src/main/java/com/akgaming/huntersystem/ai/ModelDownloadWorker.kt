package com.akgaming.huntersystem.ai

import android.content.Context
import android.os.StatFs
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.DigestInputStream
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ModelDownloadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork() = withContext(Dispatchers.IO) {
        val model = ModelCatalog.byId(inputData.getString(KEY_MODEL_ID).orEmpty())
            ?: return@withContext Result.failure(workDataOf("error" to "Unknown model"))
        val target = ModelCatalog.finalFile(applicationContext, model)
        val directory = target.parentFile ?: return@withContext Result.failure()
        directory.mkdirs()
        if (StatFs(directory.absolutePath).availableBytes < model.bytes + 128L * 1024 * 1024) {
            return@withContext Result.failure(workDataOf("error" to "Not enough free storage"))
        }
        val temp = File(directory, "${model.id}.download")
        try {
            val initial = URL(model.sourceUrl)
            require(initial.protocol == "https" && initial.host == "huggingface.co") { "Unapproved model source" }
            val connection = (initial.openConnection() as HttpURLConnection).apply {
                connectTimeout = 20_000
                readTimeout = 30_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "GYMORA-Android")
            }
            connection.connect()
            val allowedHosts = setOf("huggingface.co", "cdn-lfs.huggingface.co", "cas-bridge.xethub.hf.co")
            require(connection.url.protocol == "https" && connection.url.host in allowedHosts) { "Download redirected to an unapproved host" }
            if (connection.responseCode !in 200..299) error("Download failed: HTTP ${connection.responseCode}")
            val total = connection.contentLengthLong.takeIf { it > 0 } ?: model.bytes
            val messageDigest = MessageDigest.getInstance("SHA-256")
            DigestInputStream(connection.inputStream.buffered(), messageDigest).use { input ->
                temp.outputStream().buffered().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var copied = 0L
                    while (true) {
                        if (isStopped) error("Download cancelled")
                        val count = input.read(buffer)
                        if (count < 0) break
                        output.write(buffer, 0, count)
                        copied += count
                        setProgress(workDataOf("bytes" to copied, "total" to total))
                    }
                }
            }
            val actual = messageDigest.digest().joinToString("") { "%02x".format(it) }
            require(actual.equals(model.sha256, ignoreCase = true)) { "Model checksum verification failed" }
            Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            Result.success(workDataOf("path" to target.absolutePath))
        } catch (error: Exception) {
            temp.delete()
            Result.failure(workDataOf("error" to (error.message ?: "Model download failed")))
        }
    }

    companion object { const val KEY_MODEL_ID = "model_id" }
}
