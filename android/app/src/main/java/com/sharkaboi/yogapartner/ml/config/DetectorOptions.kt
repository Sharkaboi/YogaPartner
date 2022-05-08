package com.sharkaboi.yogapartner.ml.config

import android.content.Context
import android.content.SharedPreferences
import android.util.Size
import androidx.preference.PreferenceManager
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions

class DetectorOptions private constructor() {
    // Multiplier to apply to the torso to get minimal body size. Picked this by experimentation.
    /** [PoseEmbeddingUtils] */
    val torsoEmbeddingMultiplier = 2.5f

    // These thresholds can be tuned in conjunction with the Top K values in {@link PoseClassifier}.
    // The default Top K value is 10 so the range here is [0-10].
    val poseEnteredConfidenceThreshold = 6f
    val poseExitedConfidenceThreshold = 4f

    private val useAccurate = true
    private val preferGPU = true

    lateinit var sharedPrefs: SharedPreferences

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
        return true
    }

    fun shouldHideDetectionInfo(): Boolean {
        return true
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

    fun shouldShowLatencyInfo(): Boolean {
        return false
    }

    fun shouldShowInputImageSize(): Boolean {
        return false
    }

    fun shouldShowReps(): Boolean {
        return false
    }

    fun shouldShowOutLine(): Boolean {
        return sharedPrefs.getBoolean("showPose", false)
    }

    fun isMLImageEnabled(): Boolean {
        return true
    }

    companion object {
        private val instance = DetectorOptions()

        @JvmStatic
        fun getInstance() = instance

        fun init(context: Context) {
            instance.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        }
    }
}