package com.sharkaboi.yogapartner.ml.log

import android.os.SystemClock
import timber.log.Timber

@Suppress("ReplaceJavaStaticMethodWithKotlinAnalog")
class LatencyLogger {
    // Used to calculate latency, running in the same thread, no sync needed.
    private var numRuns = 0
    private var totalFrameMs = 0L
    private var maxFrameMs = 0L
    private var minFrameMs = Long.MAX_VALUE
    private var totalDetectorMs = 0L
    private var maxDetectorMs = 0L
    private var minDetectorMs = Long.MAX_VALUE

    private var logTimer = 0

    private val shouldShowForEachDetection = false
    private val shouldShowForEachClassification = true

    fun notifyDetectorFinished(
        frameStartMs: Long,
        detectorStartMs: Long
    ) {
        val endMs = SystemClock.elapsedRealtime()
        val currentFrameLatencyMs = endMs - frameStartMs
        val currentDetectorLatencyMs = endMs - detectorStartMs
        if (numRuns >= 500) {
            resetLatencyStats()
        }
        numRuns++
        logTimer++
        totalFrameMs += currentFrameLatencyMs
        maxFrameMs = Math.max(currentFrameLatencyMs, maxFrameMs)
        minFrameMs = Math.min(currentFrameLatencyMs, minFrameMs)
        totalDetectorMs += currentDetectorLatencyMs
        maxDetectorMs = Math.max(currentDetectorLatencyMs, maxDetectorMs)
        minDetectorMs = Math.min(currentDetectorLatencyMs, minDetectorMs)

        if (logTimer < 60) {
            return
        }

        logTimer = 0

        Timber.d("Num of Runs: $numRuns")
        Timber.d(
            "Frame latency: max=" +
                    maxFrameMs +
                    ", min=" +
                    minFrameMs +
                    ", avg=" +
                    totalFrameMs / numRuns
        )
        Timber.d(
            "Detector latency: max=" +
                    maxDetectorMs +
                    ", min=" +
                    minDetectorMs +
                    ", avg=" +
                    totalDetectorMs / numRuns
        )
    }

    private fun resetLatencyStats() {
        numRuns = 0
        totalFrameMs = 0
        maxFrameMs = 0
        minFrameMs = Long.MAX_VALUE
        totalDetectorMs = 0
        maxDetectorMs = 0
        minDetectorMs = Long.MAX_VALUE
    }

    fun reset() {
        resetLatencyStats()
    }

    fun logDetectionTime(detectorStart: Long) {
        if (!shouldShowForEachDetection) {
            return
        }
        Timber.d("Detector took " + SystemClock.elapsedRealtime().minus(detectorStart) + "ms")
    }

    fun logClassifierTime(classifierStart: Long) {
        if (!shouldShowForEachClassification) {
            return
        }

        Timber.d(
            "Classifier took " + SystemClock.elapsedRealtime()
                .minus(classifierStart) + "ms"
        )
    }
}