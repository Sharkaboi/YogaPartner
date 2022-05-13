package com.sharkaboi.yogapartner.ml.classification

import java.util.*

class ClassificationResult {
    // For an entry in this map, the key is the class name, and the value is how many times this class
    // appears in the top K nearest neighbors. The value is in range [0, K] and could be a float after
    // EMA smoothing. We use this number to represent the confidence of a pose being in this class.
    private val classConfidences: MutableMap<AsanaClass, Float> = EnumMap(AsanaClass::class.java)

    val allClasses: Set<AsanaClass>
        get() = classConfidences.keys

    fun getClassConfidence(asanaClass: AsanaClass): Float {
        return classConfidences[asanaClass] ?: 0f
    }

    fun getClassNameConfidence(className: String): Float {
        val asanaClass = runCatching { AsanaClass.valueOf(className) }.getOrNull() ?: return 0f
        return classConfidences[asanaClass] ?: 0f
    }

    // Gets first entry if same confidence
    fun getMaxConfidenceClass(): AsanaClass {
        return classConfidences.maxWithOrNull { e1, e2 ->
            (e1.value - e2.value).toInt()
        }?.key ?: AsanaClass.UNKNOWN
    }

    fun incrementClassConfidence(className: String) {
        val asanaClass = runCatching { AsanaClass.valueOf(className) }.getOrDefault(AsanaClass.UNKNOWN)
        classConfidences[asanaClass] = classConfidences[asanaClass]?.plus(1) ?: 1f
    }

    fun putClassConfidence(asanaClass: AsanaClass, confidence: Float) {
        classConfidences[asanaClass] = confidence
    }
}
