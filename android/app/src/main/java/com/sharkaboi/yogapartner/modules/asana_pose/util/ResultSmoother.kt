package com.sharkaboi.yogapartner.modules.asana_pose.util

import com.sharkaboi.yogapartner.ml.classification.AsanaClass
import com.sharkaboi.yogapartner.ml.config.DetectorOptions
import com.sharkaboi.yogapartner.ml.models.Classification
import com.sharkaboi.yogapartner.ml.models.PoseWithAsanaClassification

class ResultSmoother {
    private val lastCachedResults = mutableListOf<Classification>()

    fun getMajorityPose(): Classification {
        val asanaGrouped = lastCachedResults.groupingBy { it.asanaClass }.eachCount()
        val maxFrequencyAsana =
            asanaGrouped.maxWithOrNull { e1, e2 -> e1.value.compareTo(e2.value) }?.key
                ?: return Classification(AsanaClass.UNKNOWN, 100f)

        return lastCachedResults
            .filter {
                it.asanaClass == maxFrequencyAsana
                        && it.confidence > DetectorOptions.LANDMARK_CONF_THRESHOLD / 0.01f
            }
            .maxByOrNull { it.confidence }
            ?: return Classification(AsanaClass.UNKNOWN, 100f)
    }

    fun setInferredPose(poseWithAsanaClassification: PoseWithAsanaClassification) {
        checkFrameCountAndFlush()
        lastCachedResults.add(poseWithAsanaClassification.classification)
    }

    fun clearCache() {
        lastCachedResults.clear()
    }

    private fun checkFrameCountAndFlush() {
        if (lastCachedResults.size > FLUSH_FRAME_COUNT) {
            clearCache()
        }
    }

    companion object {
        private const val FLUSH_FRAME_COUNT = 60
    }
}