package com.sharkaboi.yogapartner.ml.processor

import android.content.Context
import android.os.Looper
import android.os.SystemClock
import androidx.annotation.WorkerThread
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskExecutors
import com.google.android.odml.image.MediaMlImageBuilder
import com.google.android.odml.image.MlImage
import com.google.common.base.Preconditions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.sharkaboi.yogapartner.R
import com.sharkaboi.yogapartner.common.extensions.showToast
import com.sharkaboi.yogapartner.ml.classification.AsanaClass
import com.sharkaboi.yogapartner.ml.classification.PoseClassifier
import com.sharkaboi.yogapartner.ml.config.DetectorOptions
import com.sharkaboi.yogapartner.ml.log.LatencyLogger
import com.sharkaboi.yogapartner.ml.models.PoseWithAsanaClassification
import com.sharkaboi.yogapartner.ml.models.TrainedPoseSample
import com.sharkaboi.yogapartner.ml.models.TrainedPoseSample.Companion.getPoseSample
import com.sharkaboi.yogapartner.modules.asana_pose.ui.custom.LandMarksOverlay
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors

class AsanaProcessor(
    private val context: Context,
    private val latencyLogger: LatencyLogger
) {
    private var poseSamples: List<TrainedPoseSample>? = null

    // Whether this processor is already shut down
    private var isShutdown = false

    private val mainThreadUiExecutor = DetectorScopedExecutor(TaskExecutors.MAIN_THREAD)

    private val classificationExecutor = Executors.newSingleThreadExecutor()
    private var poseClassifier: PoseClassifier? = null

    private val detector: PoseDetector = PoseDetection.getClient(
        DetectorOptions.getInstance().getOption()
    )

    @ExperimentalGetImage
    fun processImageProxy(
        image: ImageProxy,
        landMarksOverlay: LandMarksOverlay,
        onInference: (PoseWithAsanaClassification) -> Unit,
        isLoading: (Boolean) -> Unit
    ) {
        val frameStartMs = SystemClock.elapsedRealtime()
        if (isShutdown) {
            image.close()
            return
        }

        val task: Task<PoseWithAsanaClassification>
        if (DetectorOptions.getInstance().isMLImageEnabled()) {
            val mlImage = MediaMlImageBuilder(image.image!!)
                .setRotation(image.imageInfo.rotationDegrees)
                .build()
            task = setClassificationCallbacks(
                detectAndClassifyInMLImage(mlImage, isLoading),
                frameStartMs,
                landMarksOverlay,
                onInference
            )
        } else {
            val inputImage =
                InputImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees)
            task = setClassificationCallbacks(
                detectAndClassifyInInputImage(inputImage, isLoading),
                frameStartMs,
                landMarksOverlay,
                onInference
            )
        }

        // When the image is from CameraX analysis use case, must call image.close() on received
        // images when finished using them. Otherwise, new images may not be received or the camera
        // may stall.
        // Currently MlImage doesn't support ImageProxy directly, so we still need to call
        // ImageProxy.close() here.
        task.addOnCompleteListener { image.close() }
    }

    private fun setClassificationCallbacks(
        task: Task<PoseWithAsanaClassification>,
        frameStartMs: Long,
        landMarksOverlay: LandMarksOverlay,
        onInference: (PoseWithAsanaClassification) -> Unit
    ): Task<PoseWithAsanaClassification> {
        val detectorStartMs = SystemClock.elapsedRealtime()
        return task.addOnSuccessListener(mainThreadUiExecutor) { results ->
            latencyLogger.notifyDetectorFinished(
                frameStartMs,
                detectorStartMs
            )
            landMarksOverlay.clear()
            onInference(results)
            landMarksOverlay.setPose(results.pose)
            landMarksOverlay.postInvalidate()
        }.addOnFailureListener(mainThreadUiExecutor) { e: Exception ->
            landMarksOverlay.clear()
            val error = "Failed to process. Error: " + e.localizedMessage
            landMarksOverlay.context.showToast("$error \nCause: ${e.cause}")
            Timber.d("Pose detection failed! $error")
            e.printStackTrace()
        }
    }

    private fun detectAndClassifyInInputImage(
        image: InputImage,
        isLoading: (Boolean) -> Unit
    ): Task<PoseWithAsanaClassification> {
        val detectorStart = SystemClock.elapsedRealtime()
        val processImageTask = detector.process(image).addOnCompleteListener {
            latencyLogger.logDetectionTime(detectorStart)
        }
        return processImageTask.continueWith(classificationExecutor) { task ->
            val pose = task.result
            lazyLoadSamples(isLoading)
            val classifierStart = SystemClock.elapsedRealtime()
            val classificationResult = classifyAsanaFromPose(pose)
            latencyLogger.logClassifierTime(classifierStart)
            PoseWithAsanaClassification(pose, classificationResult)
        }
    }

    private fun detectAndClassifyInMLImage(
        image: MlImage,
        isLoading: (Boolean) -> Unit
    ): Task<PoseWithAsanaClassification> {
        val detectorStart = SystemClock.elapsedRealtime()
        val processImageTask = detector.process(image).addOnCompleteListener {
            latencyLogger.logDetectionTime(detectorStart)
        }
        return processImageTask.continueWith(classificationExecutor) { task ->
            val pose = task.result
            lazyLoadSamples(isLoading)
            val classifierStart = SystemClock.elapsedRealtime()
            val classificationResult = classifyAsanaFromPose(pose)
            latencyLogger.logClassifierTime(classifierStart)
            PoseWithAsanaClassification(pose, classificationResult)
        }
    }

    fun stop() {
        mainThreadUiExecutor.shutdown()
        isShutdown = true
        latencyLogger.reset()
        detector.close()
    }

    private fun lazyLoadSamples(isLoading: (Boolean) -> Unit) {
        if (poseSamples == null) {
            isLoading(true)
            val sampleLoadStart = SystemClock.elapsedRealtime()
            loadPoseSamplesFromDisk()
            latencyLogger.logSampleLoadTime(sampleLoadStart)
        }
        poseClassifier = PoseClassifier(poseSamples)
        isLoading(false)
    }

    @WorkerThread
    private fun classifyAsanaFromPose(pose: Pose): AsanaClass {
        Preconditions.checkState(Looper.myLooper() != Looper.getMainLooper())
        val classification = poseClassifier!!.classify(pose)

        val maxConfidenceClass = classification.getMaxConfidenceClass()
        val confidence = (classification.getClassConfidence(maxConfidenceClass)
                / poseClassifier!!.confidenceRange())
        return maxConfidenceClass
    }

    private fun loadPoseSamplesFromDisk() {
        val trainedPoseSamples: MutableList<TrainedPoseSample> = ArrayList()
        try {
            val reader = BufferedReader(
                InputStreamReader(context.resources.openRawResource(R.raw.yoga_poses))
            )
            var csvLine = reader.readLine()
            while (csvLine != null) {
                // If line is not a valid {@link PoseSample}, we'll get null and skip adding to the list.
                val trainedPoseSample = getPoseSample(csvLine, ",")
                if (trainedPoseSample != null) {
                    trainedPoseSamples.add(trainedPoseSample)
                }
                csvLine = reader.readLine()
            }
        } catch (e: Exception) {
            Timber.d("Error when loading pose samples.\n$e")
        }
        this.poseSamples = trainedPoseSamples
    }
}