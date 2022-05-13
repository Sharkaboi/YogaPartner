package com.sharkaboi.yogapartner.modules.asana_pose.util

import com.sharkaboi.yogapartner.ml.classification.AsanaClass

class ResultSmoother {
    private val lastCachedResults = mutableListOf<AsanaClass>()

    fun getMajorityPose(): AsanaClass {
        return lastCachedResults.groupingBy { it }.eachCount().maxWithOrNull { e1, e2 ->
            (e1.value - e2.value)
        }?.key ?: AsanaClass.UNKNOWN
    }

    fun setInferredPose(asanaClass: AsanaClass) {
        checkFrameCountAndFlush()
        lastCachedResults.add(asanaClass)
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
        private const val FLUSH_FRAME_COUNT = 30
    }
}