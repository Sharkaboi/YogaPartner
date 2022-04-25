package com.sharkaboi.yogapartner.ml.config

import android.util.Size
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions

object DetectorOptions {
    private val useAccurate = true
    private val preferGPU = true

    @JvmStatic
    fun getOption(): PoseDetectorOptionsBase {
        if (useAccurate) {
            val builder: AccuratePoseDetectorOptions.Builder =
                AccuratePoseDetectorOptions.Builder()
                    .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            if (preferGPU) {
                builder.setPreferredHardwareConfigs(PoseDetectorOptions.CPU_GPU)
            }
            return builder.build()
        } else {
            val builder =
                PoseDetectorOptions.Builder().setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            if (preferGPU) {
                builder.setPreferredHardwareConfigs(PoseDetectorOptions.CPU_GPU)
            }
            return builder.build()
        }
    }

    @JvmStatic
    fun isCameraLiveViewportEnabled(): Boolean {
        return false
    }

    @JvmStatic
    fun shouldHideDetectionInfo(): Boolean {
        return false
    }

    @JvmStatic
    fun getVisualizeZ(): Boolean {
        return true
    }

    @JvmStatic
    fun rescaleZForVisualization(): Boolean {
        return true
    }

    @JvmStatic
    fun inFrameLikelihood(): Boolean {
        return true
    }

    @JvmStatic
    fun shouldPoseDetectionRunClassification(): Boolean {
        return true
    }

    @JvmStatic
    fun getCameraXTargetResolution(): Size? {
        return null
    }

    @JvmStatic
    fun shouldShowLatencyInfo(): Boolean {
        return false
    }

    @JvmStatic
    fun shouldShowInputImageSize(): Boolean {
        return false
    }

    @JvmStatic
    fun shouldShowReps(): Boolean {
        return false
    }
}