package com.sharkaboi.yogapartner.ml.detector

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskExecutors
import com.google.android.odml.image.MediaMlImageBuilder
import com.google.android.odml.image.MlImage
import com.google.mlkit.vision.common.InputImage
import com.sharkaboi.yogapartner.common.extensions.showToast
import com.sharkaboi.yogapartner.ml.config.DetectorOptions
import com.sharkaboi.yogapartner.ml.utils.BitmapUtils
import com.sharkaboi.yogapartner.modules.asana_pose.camera.GraphicOverlay
import com.sharkaboi.yogapartner.modules.asana_pose.camera.InferenceInfoGraphic
import com.sharkaboi.yogapartner.modules.asana_pose.ui.CameraImageGraphic
import timber.log.Timber
import java.lang.Math.max
import java.lang.Math.min
import java.util.*

/**
 * Abstract base class for ML Kit frame processors. Subclasses need to implement {@link
 * #onSuccess(T, FrameMetadata, GraphicOverlay)} to define what they want to with the detection
 * results and {@link #detectInImage(VisionImage)} to specify the detector object.
 *
 * @param <T> The type of the detected feature.
 */
abstract class VisionProcessorBase<T>(context: Context) {
    private var activityManager: ActivityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val fpsTimer = Timer()
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
    private var framesPerSecond = 0

    init {
        fpsTimer.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    framesPerSecond = frameProcessedInOneSecondInterval
                    frameProcessedInOneSecondInterval = 0
                }
            },
            0,
            1000
        )
    }

    // -----------------Code for processing live preview frame from CameraX API-----------------------
    @ExperimentalGetImage
    fun processImageProxy(image: ImageProxy, graphicOverlay: GraphicOverlay) {
        val frameStartMs = SystemClock.elapsedRealtime()
        if (isShutdown) {
            image.close()
            return
        }
        var bitmap: Bitmap? = null
        if (!DetectorOptions.isCameraLiveViewportEnabled()) {
            bitmap = BitmapUtils.getBitmap(image)
        }

        if (DetectorOptions.isMLImageEnabled()) {
            val mlImage =
                MediaMlImageBuilder(image.image!!).setRotation(image.imageInfo.rotationDegrees)
                    .build()
            requestDetectInImage(
                mlImage,
                graphicOverlay,
                /* originalCameraImage= */ bitmap,
                /* shouldShowFps= */ true,
                frameStartMs
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
            graphicOverlay,
            /* originalCameraImage= */ bitmap,
            /* shouldShowFps= */ true,
            frameStartMs
        )
            // When the image is from CameraX analysis use case, must call image.close() on received
            // images when finished using them. Otherwise, new images may not be received or the camera
            // may stall.
            .addOnCompleteListener { image.close() }
    }

    // For InputImage input
    private fun requestDetectInImage(
        image: InputImage,
        graphicOverlay: GraphicOverlay,
        originalCameraImage: Bitmap?,
        shouldShowFps: Boolean,
        frameStartMs: Long
    ): Task<T> {
        return setUpListener(
            detectInImage(image),
            graphicOverlay,
            originalCameraImage,
            shouldShowFps,
            frameStartMs
        )
    }

    // For MlImage input
    private fun requestDetectInImage(
        image: MlImage,
        graphicOverlay: GraphicOverlay,
        originalCameraImage: Bitmap?,
        shouldShowFps: Boolean,
        frameStartMs: Long
    ): Task<T> {
        return setUpListener(
            detectInImage(image),
            graphicOverlay,
            originalCameraImage,
            shouldShowFps,
            frameStartMs
        )
    }

    private fun setUpListener(
        task: Task<T>,
        graphicOverlay: GraphicOverlay,
        originalCameraImage: Bitmap?,
        shouldShowFps: Boolean,
        frameStartMs: Long
    ): Task<T> {
        val detectorStartMs = SystemClock.elapsedRealtime()
        return task
            .addOnSuccessListener(
                executor,
                OnSuccessListener { results: T ->
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
                        val mi = ActivityManager.MemoryInfo()
                        activityManager.getMemoryInfo(mi)
                        val availableMegs: Long = mi.availMem / 0x100000L
                        Timber.d("Memory available in system: $availableMegs MB")
                    }
                    graphicOverlay.clear()
                    if (originalCameraImage != null) {
                        graphicOverlay.add(
                            CameraImageGraphic(
                                graphicOverlay,
                                originalCameraImage
                            )
                        )
                    }
                    this@VisionProcessorBase.onSuccess(results, graphicOverlay)
                    if (!DetectorOptions.shouldHideDetectionInfo()) {
                        graphicOverlay.add(
                            InferenceInfoGraphic(
                                graphicOverlay,
                                currentFrameLatencyMs,
                                currentDetectorLatencyMs,
                                if (shouldShowFps) framesPerSecond else null
                            )
                        )
                    }
                    graphicOverlay.postInvalidate()
                }
            )
            .addOnFailureListener(
                executor,
                { e: Exception ->
                    graphicOverlay.clear()
                    graphicOverlay.postInvalidate()
                    val error = "Failed to process. Error: " + e.localizedMessage
                    graphicOverlay.context.showToast("$error \nCause: ${e.cause}")
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
        fpsTimer.cancel()
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

    protected abstract fun detectInImage(image: InputImage): Task<T>

    protected abstract fun detectInImage(image: MlImage): Task<T>

    protected abstract fun onSuccess(results: T, graphicOverlay: GraphicOverlay)

    protected abstract fun onFailure(e: Exception)
}
