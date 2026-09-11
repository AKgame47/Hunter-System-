package com.akgaming.huntersystem.camera

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.util.concurrent.atomic.AtomicBoolean

class PoseAnalyzer(private val onPose: (PoseFrame) -> Unit) : ImageAnalysis.Analyzer, AutoCloseable {
    private val busy = AtomicBoolean(false)
    private val detector = PoseDetection.getClient(PoseDetectorOptions.Builder().setDetectorMode(PoseDetectorOptions.STREAM_MODE).build())

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val image = imageProxy.image
        if (image == null || !busy.compareAndSet(false, true)) {
            imageProxy.close(); return
        }
        detector.process(InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees))
            .addOnSuccessListener { pose -> onPose(pose.toFrame()) }
            .addOnCompleteListener { busy.set(false); imageProxy.close() }
    }

    override fun close() = detector.close()
}

data class PosePoint(val type: Int, val x: Float, val y: Float, val confidence: Float)
data class PoseFrame(val points: List<PosePoint>)
private fun Pose.toFrame() = PoseFrame(allPoseLandmarks.map { PosePoint(it.landmarkType, it.position.x, it.position.y, it.inFrameLikelihood) })
