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
import com.akgaming.huntersystem.camera.*
import com.google.mlkit.vision.pose.PoseLandmark
import java.util.concurrent.Executors

@Composable
fun CameraWorkoutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    var frame by remember { mutableStateOf<PoseFrame?>(null) }
    var exercise by remember { mutableStateOf(ExerciseKind.SQUAT) }
    var tracker by remember(exercise) { mutableStateOf(PoseTracker(exercise)) }
    var tracking by remember { mutableStateOf(TrackingResult(0, "READY", "Move fully into frame", false)) }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("GYMORA AI FORM COACH", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
        ScrollableTabRow(selectedTabIndex = exercise.ordinal) {
            ExerciseKind.entries.forEach { kind ->
                Tab(selected = exercise == kind, onClick = {
                    exercise = kind
                    tracker = PoseTracker(kind)
                    tracking = TrackingResult(0, "READY", "Move fully into frame", false)
                }, text = { Text(kind.name.replace('_', ' ')) })
            }
        }
        if (!granted) {
            Text("Camera is optional. Grant access for on-device guidance, or continue with manual completion.", modifier = Modifier.padding(16.dp))
            Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }, modifier = Modifier.padding(horizontal = 16.dp)) { Text("ALLOW CAMERA") }
        } else {
            CameraPreview(frame = frame, onFrame = { newFrame ->
                frame = newFrame
                tracking = tracker.update(newFrame.toSample())
            }, modifier = Modifier.fillMaxWidth().weight(1f))
            Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(if (exercise == ExerciseKind.PLANK) "${tracking.holdMillis / 1000}s" else "${tracking.reps} REPS", style = MaterialTheme.typography.headlineMedium)
                    Text("${tracking.phase} · ${tracking.feedback}")
                    Text(if (tracking.confident) "Tracking confidence accepted" else "Uncertain — no progress awarded", color = if (tracking.confident) Color(0xFF42E79C) else Color(0xFFFFB74D))
                    Text("Pose processing stays on this device.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.padding(16.dp).fillMaxWidth()) { Text("MANUAL MODE / BACK") }
    }
}

private fun PoseFrame.toSample(): PoseSample {
    fun point(type: Int) = points.firstOrNull { it.type == type }?.let { JointPoint(it.x, it.y, it.confidence) }
    val pairs = listOf(
        Joint.LEFT_SHOULDER to PoseLandmark.LEFT_SHOULDER, Joint.RIGHT_SHOULDER to PoseLandmark.RIGHT_SHOULDER,
        Joint.LEFT_ELBOW to PoseLandmark.LEFT_ELBOW, Joint.RIGHT_ELBOW to PoseLandmark.RIGHT_ELBOW,
        Joint.LEFT_WRIST to PoseLandmark.LEFT_WRIST, Joint.RIGHT_WRIST to PoseLandmark.RIGHT_WRIST,
        Joint.LEFT_HIP to PoseLandmark.LEFT_HIP, Joint.RIGHT_HIP to PoseLandmark.RIGHT_HIP,
        Joint.LEFT_KNEE to PoseLandmark.LEFT_KNEE, Joint.RIGHT_KNEE to PoseLandmark.RIGHT_KNEE,
        Joint.LEFT_ANKLE to PoseLandmark.LEFT_ANKLE, Joint.RIGHT_ANKLE to PoseLandmark.RIGHT_ANKLE,
    )
    return PoseSample(pairs.mapNotNull { (joint, type) -> point(type)?.let { joint to it } }.toMap(), android.os.SystemClock.elapsedRealtime())
}

@Composable
private fun CameraPreview(frame: PoseFrame?, onFrame: (PoseFrame) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val latestCallback by rememberUpdatedState(onFrame)
    val executor = remember { Executors.newSingleThreadExecutor() }
    val analyzer = remember { PoseAnalyzer { latestCallback(it) } }
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
        val future = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            val provider = future.get()
            val preview = Preview.Builder().build().also { it.surfaceProvider = previewView?.surfaceProvider }
            val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also { it.setAnalyzer(executor, analyzer) }
            provider.unbindAll()
            val selector = if (provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            provider.bindToLifecycle(owner, selector, preview, analysis)
        }
        future.addListener(listener, ContextCompat.getMainExecutor(context))
        onDispose { runCatching { future.get().unbindAll() }; analyzer.close(); executor.shutdown() }
    }
}
