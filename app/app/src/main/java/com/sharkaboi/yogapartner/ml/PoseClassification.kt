package com.sharkaboi.yogapartner.ml

/**
 * Represents Pose classification result as outputted by {@link PoseClassifier}. Can be manipulated.
 */
class PoseClassification(private val confidenceRange: Int) {
    // For an entry in this map, the key is the class name, and the value is how many times this class
    // appears in the top K nearest neighbors. The value is in range [0, K] and could be a float after
    // EMA smoothing. We use this number to represent the confidence of a pose being in this class.
    private var classConfidences = hashMapOf<PoseClass, Float>()


    fun getClassConfidence(poseClass: PoseClass): Float {
        return classConfidences[poseClass] ?: 0f / confidenceRange
    }

    fun getMostConfidentClass(): PoseClass {
        return classConfidences.maxByOrNull {
            it.value
        }?.key ?: PoseClass.UNKNOWN
    }

    fun incrementClassConfidence(className: String) {
        val poseClass = runCatching { PoseClass.valueOf(className) }.getOrNull() ?: return
        val prev = classConfidences[poseClass]
        classConfidences[poseClass] = prev?.plus(1f) ?: 1f
    }

    fun putClassConfidence(className: String, confidence: Float) {
        val poseClass = runCatching { PoseClass.valueOf(className) }.getOrNull() ?: return
        classConfidences[poseClass] = confidence
    }

    companion object {
        val EMPTY = PoseClassification(1)
    }
}
