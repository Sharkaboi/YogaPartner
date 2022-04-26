package com.sharkaboi.yogapartner.ml.classification.reps

import com.sharkaboi.yogapartner.ml.config.DetectorOptions
import com.sharkaboi.yogapartner.ml.models.ClassificationResult

/**
 * Counts reps for the give class.
 */
class RepetitionCounter @JvmOverloads constructor(
    val className: String,
    private val enterThreshold: Float = DEFAULT_ENTER_THRESHOLD,
    private val exitThreshold: Float = DEFAULT_EXIT_THRESHOLD
) {
    var numRepeats = 0
        private set
    private var poseEntered = false

    /**
     * Adds a new Pose classification result and updates reps for given class.
     *
     * @param classificationResult {link ClassificationResult} of class to confidence values.
     * @return number of reps.
     */
    fun addClassificationResult(classificationResult: ClassificationResult): Int {
        val poseConfidence = classificationResult.getClassNameConfidence(className)

        if (!poseEntered) {
            poseEntered = poseConfidence > enterThreshold
            return numRepeats
        }
        if (poseConfidence < exitThreshold) {
            numRepeats++
            poseEntered = false
        }
        return numRepeats
    }

    companion object {
        private const val DEFAULT_ENTER_THRESHOLD = DetectorOptions.poseEnteredConfidenceThreshold
        private const val DEFAULT_EXIT_THRESHOLD = DetectorOptions.poseExitedConfidenceThreshold
    }
}