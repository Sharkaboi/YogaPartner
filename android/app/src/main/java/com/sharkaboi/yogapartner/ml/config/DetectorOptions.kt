package com.sharkaboi.yogapartner.ml.config

import android.util.Size
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions

object DetectorOptions {
    private val useAccurate = false
    private val preferGPU = true

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

    fun isCameraLiveViewportEnabled(): Boolean {
        return false
    }

    fun shouldHideDetectionInfo(): Boolean {
        return false
    }

    fun getVisualizeZ(): Boolean {
        return true
    }

    fun rescaleZForVisualization(): Boolean {
        return true
    }

    fun inFrameLikelihood(): Boolean {
        return true
    }

    fun shouldPoseDetectionRunClassification(): Boolean {
        return true
    }

    fun getCameraXTargetResolution(): Size? {
        return null
    }
}