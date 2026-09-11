package com.akgaming.huntersystem.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.*
import com.akgaming.huntersystem.ai.*
import java.util.concurrent.TimeUnit

@Composable
fun ModelStoreScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val workManager = remember { WorkManager.getInstance(context) }
    val probe = remember { RuntimeCapabilityProbe() }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("LOCAL AI MODEL STORE", style = MaterialTheme.typography.headlineSmall)
        Text("Models stay in app-private storage. Downloads are large and may use significant data, battery, storage, and memory. Local prompts are not uploaded by the model store.")
        ModelCatalog.models.forEach { model ->
            val file = ModelCatalog.finalFile(context, model)
            val capability = probe.capability(model)
            Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(model.name, style = MaterialTheme.typography.titleMedium)
                Text("${model.runtimeFormat} · ${model.bytes / 1_000_000} MB · ${model.license} · ${model.minimumRamGb} GB RAM recommended")
                Text(if (file.exists()) "Downloaded and checksum-verified" else "Not downloaded")
                Text(if (capability.supported) "Runtime ready" else "Runtime unavailable: ${capability.reason}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(enabled = !file.exists(), onClick = {
                        val request = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
                            .setInputData(workDataOf(ModelDownloadWorker.KEY_MODEL_ID to model.id))
                            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).setRequiresStorageNotLow(true).build())
                            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                            .build()
                        workManager.enqueueUniqueWork("model-${model.id}", ExistingWorkPolicy.KEEP, request)
                    }) { Text("Download") }
                    OutlinedButton(enabled = file.exists(), onClick = { file.delete() }) { Text("Remove") }
                }
                Text("Source: Hugging Face / Qwen. Downloading means you accept the model license.", style = MaterialTheme.typography.bodySmall)
            }}
        }
        OutlinedButton(onClick = onBack) { Text("Back") }
    }
}
