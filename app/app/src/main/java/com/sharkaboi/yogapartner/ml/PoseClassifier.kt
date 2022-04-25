package com.sharkaboi.yogapartner.ml

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.common.PointF3D
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.sharkaboi.yogapartner.R
import com.sharkaboi.yogapartner.common.TaskState
import com.sharkaboi.yogapartner.common.extensions.await
import com.sharkaboi.yogapartner.ml.config.DetectorOptions
import com.sharkaboi.yogapartner.ml.pose.PoseEmbedding
import com.sharkaboi.yogapartner.ml.pose.PoseTrainedSample
import com.sharkaboi.yogapartner.ml.util.BitmapUtils
import com.sharkaboi.yogapartner.ml.util.PointF3DUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max

class PoseClassifierImpl(
    private val context: Context
) : PoseClassifier {
    // Whether this processor is already shut down
    private var isShutdown = false
    private var detector: PoseDetector? = null
    private var poseSamples: List<PoseTrainedSample> = emptyList()

    override suspend fun getLandmarks(imageProxy: ImageProxy): TaskState<Landmarks> {
        if (isShutdown) {
            imageProxy.close()
            return TaskState.NoErrorFailure
        }
        var bitmap: Bitmap? = null
        if (!DetectorOptions.isCameraLiveViewportEnabled()) {
            bitmap = BitmapUtils.getBitmap(imageProxy)
        }

        val inputImage = InputImage.fromMediaImage(
            imageProxy.image!!,
            imageProxy.imageInfo.rotationDegrees
        )

        val detector = detector ?: run {
            imageProxy.close()
            return TaskState.Failure("Detector not initialized")
        }

        val result = runCatching { detector.process(inputImage).await() }

        // When the image is from CameraX analysis use case, must call image.close() on received
        // images when finished using them. Otherwise, new images may not be received or the camera
        // may stall.
        imageProxy.close()

        val pose = result.getOrNull()
            ?: return TaskState.Failure("Error occurred ${result.exceptionOrNull()}")

        return TaskState.Success(Landmarks(pose, bitmap))
    }


    /**
     * Given a new [Pose] input, returns a list of formatted [String]s with Pose
     * classification results.
     *
     *
     * Currently it returns up to 2 strings as following:
     * 0: PoseClass : X reps
     * 1: PoseClass : [0.0-1.0] confidence
     */
    override suspend fun getPoseClassification(pose: Pose): TaskState<PoseClassification> {
        val landmarks: MutableList<PointF3D> = ArrayList()
        for (poseLandmark in pose.allPoseLandmarks) {
            landmarks.add(poseLandmark.position3D)
        }

        // Return early if no landmarks detected.
        if (landmarks.isEmpty()) {
            return TaskState.Success(PoseClassification(confidenceRange()))
        }

        // We do flipping on X-axis so we are horizontal (mirror) invariant.
        val flippedLandmarks: MutableList<PointF3D> = ArrayList(landmarks)
        PointF3DUtils.multiplyAll(flippedLandmarks, PointF3D.from(-1f, 1f, 1f))
        val embedding: List<PointF3D> = PoseEmbedding.getPoseEmbedding(landmarks)
        val flippedEmbedding: List<PointF3D> = PoseEmbedding.getPoseEmbedding(flippedLandmarks)

        // Classification is done in two stages:
        //  * First we pick top-K samples by MAX distance. It allows to remove samples that are almost
        //    the same as given pose, but maybe has few joints bent in the other direction.
        //  * Then we pick top-K samples by MEAN distance. After outliers are removed, we pick samples
        //    that are closest by average.

        // Keeps max distance on top so we can pop it when top_k size is reached.
        val maxDistances =
            PriorityQueue<Pair<PoseTrainedSample, Float>>(MAX_DISTANCE_TOP_K) { o1, o2 ->
                -(o1.second).compareTo(o2.second)
            }

        // Retrieve top K poseSamples by least distance to remove outliers.
        for (poseSample in poseSamples) {
            val sampleEmbedding: List<PointF3D> = poseSample.embedding
            var originalMax = 0f
            var flippedMax = 0f
            for (i in embedding.indices) {
                val subtractedEmbedding = PointF3DUtils.subtract(embedding[i], sampleEmbedding[i])
                val multipliedEmbedding = PointF3DUtils.multiply(subtractedEmbedding, AXES_WEIGHTS)
                val maxAbsEmbedding = PointF3DUtils.maxAbs(multipliedEmbedding)
                originalMax = max(originalMax, maxAbsEmbedding)

                val subtractedFlippedEmbedding =
                    PointF3DUtils.subtract(flippedEmbedding[i], sampleEmbedding[i])
                val multipliedFlippedEmbedding =
                    PointF3DUtils.multiply(subtractedFlippedEmbedding, AXES_WEIGHTS)
                val maxAbsFlippedEmbedding = PointF3DUtils.maxAbs(multipliedFlippedEmbedding)
                flippedMax = max(flippedMax, maxAbsFlippedEmbedding)
            }
            // Set the max distance as min of original and flipped max distance.
            maxDistances.add(Pair(poseSample, originalMax.coerceAtMost(flippedMax)))
            // We only want to retain top n so pop the highest distance.
            if (maxDistances.size > MAX_DISTANCE_TOP_K) {
                maxDistances.poll()
            }
        }

        // Keeps higher mean distances on top so we can pop it when top_k size is reached.
        val meanDistances =
            PriorityQueue<Pair<PoseTrainedSample, Float>>(MEAN_DISTANCE_TOP_K) { o1, o2 ->
                -(o1.second).compareTo(o2.second)
            }

        // Retrieve top K poseSamples by least mean distance to remove outliers.
        for (sampleDistances in maxDistances) {
            val poseSample: PoseTrainedSample = sampleDistances.first
            val sampleEmbedding: List<PointF3D> = poseSample.embedding
            var originalSum = 0f
            var flippedSum = 0f
            for (i in embedding.indices) {
                val subtractedEmbedding = PointF3DUtils.subtract(embedding[i], sampleEmbedding[i])
                val multipliedEmbedding = PointF3DUtils.multiply(subtractedEmbedding, AXES_WEIGHTS)
                val sumAbsEmbedding = PointF3DUtils.sumAbs(multipliedEmbedding)
                originalSum += sumAbsEmbedding

                val subtractedFlippedEmbedding =
                    PointF3DUtils.subtract(flippedEmbedding[i], sampleEmbedding[i])
                val multipliedFlippedEmbedding =
                    PointF3DUtils.multiply(subtractedFlippedEmbedding, AXES_WEIGHTS)
                val sumAbsFlippedEmbedding = PointF3DUtils.sumAbs(multipliedFlippedEmbedding)
                flippedSum += sumAbsFlippedEmbedding
            }
            // Set the mean distance as min of original and flipped mean distances.
            val meanDistance = originalSum.coerceAtMost(flippedSum) / (embedding.size * 2)
            meanDistances.add(Pair(poseSample, meanDistance))
            // We only want to retain top k so pop the highest mean distance.
            if (meanDistances.size > MEAN_DISTANCE_TOP_K) {
                meanDistances.poll()
            }
        }

        val classification = PoseClassification(confidenceRange())
        for (sampleDistances in meanDistances) {
            val className: String = sampleDistances.first.className
            classification.incrementClassConfidence(className)
        }

        return TaskState.Success(classification)
    }

    override fun stop() {
        isShutdown = true
        detector?.close()
    }

    override suspend fun init() {
        val options = DetectorOptions.getOption()
        detector = PoseDetection.getClient(options)
        isShutdown = false
        loadPosesFromAssets()
    }

    private suspend fun loadPosesFromAssets() = withContext(Dispatchers.IO) {
        val poseSamples: MutableList<PoseTrainedSample> = ArrayList()
        try {
            val reader = BufferedReader(
                InputStreamReader(context.resources.openRawResource(R.raw.yoga_poses))
            )
            var csvLine = reader.readLine()
            var count = 1
            while (csvLine != null) {
                Timber.d("Reading line $csvLine")
                // If line is not a valid {@link PoseSample}, we'll get null and skip adding to the list.
                val poseSample: PoseTrainedSample? = PoseTrainedSample.getPoseSample(csvLine, ",")
                if (poseSample != null) {
                    poseSamples.add(poseSample)
                }
                csvLine = reader.readLine()
            }
        } catch (e: IOException) {
            Timber.d("Error when loading pose samples.\n$e")
        }

        this@PoseClassifierImpl.poseSamples = poseSamples
    }

    companion object {
        private const val MAX_DISTANCE_TOP_K = 30
        private const val MEAN_DISTANCE_TOP_K = 10

        // Note Z has a lower weight as it is generally less accurate than X & Y.
        private val AXES_WEIGHTS = PointF3D.from(1f, 1f, 0.2f)

        /**
         * Returns the max range of confidence values.
         *
         * Since we calculate confidence by counting [PoseTrainedSample]s that survived
         * outlier-filtering by MAX_DISTANCE_TOP_K and MEAN_DISTANCE_TOP_K, this range is the minimum of two.
         */
        fun confidenceRange(): Int {
            return MAX_DISTANCE_TOP_K.coerceAtMost(MEAN_DISTANCE_TOP_K)
        }
    }
}

interface PoseClassifier {
    suspend fun getLandmarks(imageProxy: ImageProxy): TaskState<Landmarks>
    suspend fun getPoseClassification(pose: Pose): TaskState<PoseClassification>
    fun stop()
    suspend fun init()
}