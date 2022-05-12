package com.sharkaboi.yogapartner.ml.detector

import android.app.ActivityManager
import android.content.Context
import android.os.SystemClock
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskExecutors
import com.google.android.odml.image.MediaMlImageBuilder
import com.google.android.odml.image.MlImage
import com.google.mlkit.vision.common.InputImage
import com.sharkaboi.yogapartner.common.extensions.showToast
import com.sharkaboi.yogapartner.ml.config.DetectorOptions
import com.sharkaboi.yogapartner.modules.asana_pose.ui.custom.LandMarksOverlay
import timber.log.Timber
import java.lang.Math.max
import java.lang.Math.min
import java.util.*

abstract class VisionProcessorBase<T>(context: Context) {
    private val executor = DetectorScopedExecutor(TaskExecutors.MAIN_THREAD)

    // Whether this processor is already shut down
    private var isShutdown = false

    // Used to calculate latency, running in the same thread, no sync needed.
    private var numRuns = 0
    private var totalFrameMs = 0L
    private var maxFrameMs = 0L
    private var minFrameMs = Long.MAX_VALUE
    private var totalDetectorMs = 0L
    private var maxDetectorMs = 0L
    private var minDetectorMs = Long.MAX_VALUE

    // Frame count that have been processed so far in an one second interval to calculate FPS.
    private var frameProcessedInOneSecondInterval = 0

    @ExperimentalGetImage
    fun processImageProxy(
        image: ImageProxy,
        landMarksOverlay: LandMarksOverlay,
        onInference: (PoseDetectorProcessor.PoseWithClassification) -> Unit,
        isLoading: (Boolean) -> Unit
    ) {
        val frameStartMs = SystemClock.elapsedRealtime()
        if (isShutdown) {
            image.close()
            return
        }

        if (DetectorOptions.getInstance().isMLImageEnabled()) {
            val mlImage =
                MediaMlImageBuilder(image.image!!).setRotation(image.imageInfo.rotationDegrees)
                    .build()
            requestDetectInImage(
                mlImage,
                landMarksOverlay,
                frameStartMs,
                onInference,
                isLoading
            )
                // When the image is from CameraX analysis use case, must call image.close() on received
                // images when finished using them. Otherwise, new images may not be received or the camera
                // may stall.
                // Currently MlImage doesn't support ImageProxy directly, so we still need to call
                // ImageProxy.close() here.
                .addOnCompleteListener { image.close() }

            return
        }

        requestDetectInImage(
            InputImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees),
            landMarksOverlay,
            frameStartMs,
            onInference,
            isLoading
        )
            // When the image is from CameraX analysis use case, must call image.close() on received
            // images when finished using them. Otherwise, new images may not be received or the camera
            // may stall.
            .addOnCompleteListener { image.close() }
    }

    // For InputImage input
    private fun requestDetectInImage(
        image: InputImage,
        landMarksOverlay: LandMarksOverlay,
        frameStartMs: Long,
        onInference: (PoseDetectorProcessor.PoseWithClassification) -> Unit,
        isLoading: (Boolean) -> Unit
    ): Task<T> {
        return setUpListener(
            detectInImage(image, isLoading),
            landMarksOverlay,
            frameStartMs,
            onInference
        )
    }

    // For MlImage input
    private fun requestDetectInImage(
        image: MlImage,
        landMarksOverlay: LandMarksOverlay,
        frameStartMs: Long,
        onInference: (PoseDetectorProcessor.PoseWithClassification) -> Unit,
        isLoading: (Boolean) -> Unit
    ): Task<T> {
        return setUpListener(
            detectInImage(image, isLoading),
            landMarksOverlay,
            frameStartMs,
            onInference
        )
    }

    private fun setUpListener(
        task: Task<T>,
        landMarksOverlay: LandMarksOverlay,
        frameStartMs: Long,
        onInference: (PoseDetectorProcessor.PoseWithClassification) -> Unit
    ): Task<T> {
        val detectorStartMs = SystemClock.elapsedRealtime()
        return task
            .addOnSuccessListener(
                executor,
                { results: T ->
                    val endMs = SystemClock.elapsedRealtime()
                    val currentFrameLatencyMs = endMs - frameStartMs
                    val currentDetectorLatencyMs = endMs - detectorStartMs
                    if (numRuns >= 500) {
                        resetLatencyStats()
                    }
                    numRuns++
                    frameProcessedInOneSecondInterval++
                    totalFrameMs += currentFrameLatencyMs
                    maxFrameMs = max(currentFrameLatencyMs, maxFrameMs)
                    minFrameMs = min(currentFrameLatencyMs, minFrameMs)
                    totalDetectorMs += currentDetectorLatencyMs
                    maxDetectorMs = max(currentDetectorLatencyMs, maxDetectorMs)
                    minDetectorMs = min(currentDetectorLatencyMs, minDetectorMs)

                    // Only log inference info once per second. When frameProcessedInOneSecondInterval is
                    // equal to 1, it means this is the first frame processed during the current second.
                    if (frameProcessedInOneSecondInterval == 1) {
                        Timber.d("Num of Runs: $numRuns")
                        Timber.d(
                            "Frame latency: max=" +
                                    maxFrameMs +
                                    ", min=" +
                                    minFrameMs +
                                    ", avg=" +
                                    totalFrameMs / numRuns
                        )
                        Timber.d(
                            "Detector latency: max=" +
                                    maxDetectorMs +
                                    ", min=" +
                                    minDetectorMs +
                                    ", avg=" +
                                    totalDetectorMs / numRuns
                        )
                    }
                    landMarksOverlay.clear()
                    this@VisionProcessorBase.onSuccess(results, landMarksOverlay, onInference)
                    landMarksOverlay.postInvalidate()
                }
            )
            .addOnFailureListener(
                executor,
                { e: Exception ->
                    landMarksOverlay.clear()
                    landMarksOverlay.postInvalidate()
                    val error = "Failed to process. Error: " + e.localizedMessage
                    landMarksOverlay.context.showToast("$error \nCause: ${e.cause}")
                    Timber.d(error)
                    e.printStackTrace()
                    this@VisionProcessorBase.onFailure(e)
                }
            )
    }

    open fun stop() {
        executor.shutdown()
        isShutdown = true
        resetLatencyStats()
    }

    private fun resetLatencyStats() {
        numRuns = 0
        totalFrameMs = 0
        maxFrameMs = 0
        minFrameMs = Long.MAX_VALUE
        totalDetectorMs = 0
        maxDetectorMs = 0
        minDetectorMs = Long.MAX_VALUE
    }

    protected abstract fun detectInImage(image: InputImage, isLoading: (Boolean) -> Unit): Task<T>

    protected abstract fun detectInImage(image: MlImage, isLoading: (Boolean) -> Unit): Task<T>

    protected abstract fun onSuccess(
        results: T,
        landMarksOverlay: LandMarksOverlay,
        onInference: (PoseDetectorProcessor.PoseWithClassification) -> Unit
    )

    protected abstract fun onFailure(e: Exception)
}
