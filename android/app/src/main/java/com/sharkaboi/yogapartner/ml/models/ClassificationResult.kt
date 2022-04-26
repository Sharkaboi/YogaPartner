package com.sharkaboi.yogapartner.ml.models

import com.sharkaboi.yogapartner.ml.classification.PoseClass
import java.util.*

/**
 * Represents Pose classification result as outputted by [PoseClassifier]. Can be manipulated.
 */
class ClassificationResult {
    // For an entry in this map, the key is the class name, and the value is how many times this class
    // appears in the top K nearest neighbors. The value is in range [0, K] and could be a float after
    // EMA smoothing. We use this number to represent the confidence of a pose being in this class.
    private val classConfidences: MutableMap<PoseClass, Float> = EnumMap(PoseClass::class.java)

    val allClasses: Set<PoseClass>
        get() = classConfidences.keys

    fun getClassConfidence(className: PoseClass): Float {
        return classConfidences[className] ?: 0f
    }

    fun getClassNameConfidence(className: String): Float {
        val poseClass = runCatching { PoseClass.valueOf(className) }.getOrNull() ?: return 0f
        return classConfidences[poseClass] ?: 0f
    }

    // Gets first entry if same confidence
    fun getMaxConfidenceClass(): PoseClass {
        return classConfidences.maxWithOrNull { e1, e2 ->
            (e1.value - e2.value).toInt()
        }?.key ?: PoseClass.UNKNOWN
    }

    fun incrementClassConfidence(className: String) {
        val poseClass = runCatching { PoseClass.valueOf(className) }.getOrDefault(PoseClass.UNKNOWN)
        classConfidences[poseClass] = classConfidences[poseClass]?.plus(1) ?: 1f
    }

    fun putClassConfidence(className: PoseClass, confidence: Float) {
        classConfidences[className] = confidence
    }
}
