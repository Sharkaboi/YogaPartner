package com.sharkaboi.yogapartner.ml.detector

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.android.odml.image.MlImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import com.sharkaboi.yogapartner.ml.classification.PoseClassifierProcessor
import com.sharkaboi.yogapartner.modules.asana_pose.ui.custom.LandMarksOverlay
import timber.log.Timber
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/** A processor to run pose detector. */
class PoseDetectorProcessor(
    private val context: Context,
    options: PoseDetectorOptionsBase
) : VisionProcessorBase<PoseDetectorProcessor.PoseWithClassification>(context) {

    private val detector: PoseDetector = PoseDetection.getClient(options)
    private val classificationExecutor: Executor

    private var poseClassifierProcessor: PoseClassifierProcessor? = null

    /** Internal class to hold Pose and classification results. */
    class PoseWithClassification(val pose: Pose, val classificationResult: List<String>)

    init {
        classificationExecutor = Executors.newSingleThreadExecutor()
    }

    override fun stop() {
        super.stop()
        detector.close()
    }

    // For InputImage type
    override fun detectInImage(
        image: InputImage,
        isLoading: (Boolean) -> Unit
    ): Task<PoseWithClassification> {
        return detector
            .process(image)
            .continueWith(
                classificationExecutor,
                { task ->
                    val pose = task.result
                    if (poseClassifierProcessor == null) {
                        isLoading(true)
                        poseClassifierProcessor = PoseClassifierProcessor(context)
                    }
                    isLoading(false)
                    val classificationResult = poseClassifierProcessor!!.getPoseResult(pose)
                    PoseWithClassification(pose, classificationResult)
                }
            )
    }

    // For MlImage type
    override fun detectInImage(
        image: MlImage,
        isLoading: (Boolean) -> Unit
    ): Task<PoseWithClassification> {
        return detector
            .process(image)
            .continueWith(
                classificationExecutor,
                { task ->
                    val pose = task.result
                    if (poseClassifierProcessor == null) {
                        isLoading(true)
                        poseClassifierProcessor = PoseClassifierProcessor(context)
                    }
                    isLoading(false)
                    val classificationResult = poseClassifierProcessor!!.getPoseResult(pose)
                    PoseWithClassification(pose, classificationResult)
                }
            )
    }

    override fun onSuccess(
        results: PoseWithClassification,
        landMarksOverlay: LandMarksOverlay,
        onInference: (PoseWithClassification) -> Unit
    ) {
        onInference(results)
        landMarksOverlay.setPose(results.pose)
    }

    override fun onFailure(e: Exception) {
        Timber.d("Pose detection failed!", e)
    }
}
