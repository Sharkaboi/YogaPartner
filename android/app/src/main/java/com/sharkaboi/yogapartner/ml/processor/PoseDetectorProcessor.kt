

package com.sharkaboi.yogapartner.ml.processor

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.odml.image.MlImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import com.sharkaboi.yogapartner.ml.classification.PoseClassifierProcessor
import com.sharkaboi.yogapartner.modules.asana_pose.ui.PoseGraphic
import com.sharkaboi.yogapartner.modules.asana_pose.camera.GraphicOverlay
import timber.log.Timber
import java.util.ArrayList
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/** A processor to run pose detector. */
class PoseDetectorProcessor(
  private val context: Context,
  options: PoseDetectorOptionsBase,
  private val showInFrameLikelihood: Boolean,
  private val visualizeZ: Boolean,
  private val rescaleZForVisualization: Boolean,
  private val runClassification: Boolean,
  private val isStreamMode: Boolean
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

  override fun detectInImage(image: InputImage): Task<PoseWithClassification> {
    return detector
      .process(image)
      .continueWith(
        classificationExecutor,
        { task ->
          val pose = task.result
          var classificationResult: List<String> = ArrayList()
          if (runClassification) {
            if (poseClassifierProcessor == null) {
              poseClassifierProcessor =
                PoseClassifierProcessor(
                  context,
                  isStreamMode
                )
            }
            classificationResult = poseClassifierProcessor!!.getPoseResult(pose)
          }
          PoseWithClassification(pose, classificationResult)
        }
      )
  }

  // do not call super.detectInImage
  override fun detectInImage(image: MlImage): Task<PoseWithClassification> {
    return detector
      .process(image)
      .continueWith(
        classificationExecutor,
        { task ->
          val pose = task.getResult()
          var classificationResult: List<String> = ArrayList()
          if (runClassification) {
            if (poseClassifierProcessor == null) {
              poseClassifierProcessor =
                PoseClassifierProcessor(
                  context,
                  isStreamMode
                )
            }
            classificationResult = poseClassifierProcessor!!.getPoseResult(pose)
          }
          PoseWithClassification(pose, classificationResult)
        }
      )
  }

  override fun onSuccess(
    results: PoseWithClassification,
    graphicOverlay: GraphicOverlay
  ) {
    graphicOverlay.add(
      PoseGraphic(
        graphicOverlay,
        results.pose,
        showInFrameLikelihood,
        visualizeZ,
        rescaleZForVisualization,
        results.classificationResult
      )
    )
  }

  override fun onFailure(e: Exception) {
    Timber.d( "Pose detection failed!", e)
  }
}
