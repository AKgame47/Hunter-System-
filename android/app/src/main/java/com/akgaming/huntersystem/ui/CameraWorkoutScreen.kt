package com.akgaming.huntersystem.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.akgaming.huntersystem.camera.PoseAnalyzer
import com.akgaming.huntersystem.camera.PoseFrame
import java.util.concurrent.Executors

@Composable
fun CameraWorkoutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    var frame by remember { mutableStateOf<PoseFrame?>(null) }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("POSE WORKOUT", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
        if (!granted) {
            Text("Camera is optional. Grant access for on-device pose guidance, or continue with manual completion.", modifier = Modifier.padding(16.dp))
            Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }, modifier = Modifier.padding(horizontal = 16.dp)) { Text("Allow camera") }
        } else {
            CameraPreview(frame = frame, onFrame = { frame = it }, modifier = Modifier.fillMaxWidth().weight(1f))
            Text(if (frame?.points.isNullOrEmpty()) "No person detected — move fully into frame" else "Analyzing on device · ${frame!!.points.size} landmarks", modifier = Modifier.padding(horizontal = 16.dp))
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.padding(16.dp).fillMaxWidth()) { Text("Manual mode / Back") }
    }
}

@Composable
private fun CameraPreview(frame: PoseFrame?, onFrame: (PoseFrame) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val analyzer = remember { PoseAnalyzer(onFrame) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    Box(modifier) {
        AndroidView(factory = { PreviewView(it).also { view -> view.scaleType = PreviewView.ScaleType.FILL_CENTER; previewView = view } }, modifier = Modifier.fillMaxSize())
        Canvas(Modifier.fillMaxSize()) {
            val source = frame ?: return@Canvas
            val sx = if (source.width > 0) size.width / source.width else 1f
            val sy = if (source.height > 0) size.height / source.height else 1f
            source.points.filter { it.confidence >= .65f }.forEach { drawCircle(Color.Cyan, 5.dp.toPx(), Offset(it.x * sx, it.y * sy)) }
        }
    }
    DisposableEffect(owner, previewView) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also { it.surfaceProvider = previewView?.surfaceProvider }
            val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also { it.setAnalyzer(executor, analyzer) }
            provider.unbindAll()
            val selector = if (provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            provider.bindToLifecycle(owner, selector, preview, analysis)
        }
        providerFuture.addListener(listener, ContextCompat.getMainExecutor(context))
        onDispose { runCatching { providerFuture.get().unbindAll() }; analyzer.close(); executor.shutdown() }
    }
}
