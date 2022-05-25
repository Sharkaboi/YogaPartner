package com.sharkaboi.yogapartner.ml.classification

import java.util.*

class ClassificationResult {
    private val classConfidences: MutableMap<AsanaClass, Float> = EnumMap(AsanaClass::class.java)

    fun getClassConfidence(asanaClass: AsanaClass): Float {
        return classConfidences[asanaClass] ?: 0f
    }

    // Gets first entry if same confidence
    fun getMaxConfidenceClass(): AsanaClass {
        return classConfidences.maxWithOrNull { e1, e2 ->
            e1.value.compareTo(e2.value)
        }?.key ?: AsanaClass.UNKNOWN
    }

    fun incrementClassConfidence(className: String) {
        val asanaClass =
            runCatching { AsanaClass.valueOf(className) }.getOrDefault(AsanaClass.UNKNOWN)
        classConfidences[asanaClass] = classConfidences[asanaClass]?.plus(1) ?: 1f
    }

    fun putClassConfidence(asanaClass: AsanaClass, confidence: Float) {
        classConfidences[asanaClass] = confidence
    }
}
