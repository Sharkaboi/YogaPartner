package com.sharkaboi.yogapartner.ml.config

import android.content.Context
import android.content.SharedPreferences
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.sharkaboi.yogapartner.common.extensions.getOrSetDefault
import com.sharkaboi.yogapartner.ml.ConvertedModel
import com.sharkaboi.yogapartner.ml.classification.IAsanaClassifier
import com.sharkaboi.yogapartner.ml.classification.KNNAsanaClassifier
import com.sharkaboi.yogapartner.ml.classification.TFLiteAsanaClassifier
import com.sharkaboi.yogapartner.ml.models.TrainedPoseSample

class DetectorOptions(
    private val context: Context,
    private val sharedPrefs: SharedPreferences
) {
    init {
        sharedPrefs.getOrSetDefault("isMlImage", true)
        sharedPrefs.getOrSetDefault("showPose", false)
        sharedPrefs.getOrSetDefault("showZ", true)
        sharedPrefs.getOrSetDefault("preferGpu", true)
        sharedPrefs.getOrSetDefault("useAccurate", true)
        sharedPrefs.getOrSetDefault("shouldShowConfidence", false)
        sharedPrefs.getOrSetDefault("useKnn", false)
    }

    private val tfLiteModel get() = ConvertedModel.newInstance(context)

    fun getOption(): PoseDetectorOptionsBase {
        val useAccurate = sharedPrefs.getBoolean("useAccurate", true)
        val preferGpu = sharedPrefs.getBoolean("preferGpu", true)
        if (useAccurate) {
            val builder: AccuratePoseDetectorOptions.Builder =
                AccuratePoseDetectorOptions.Builder()
                    .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            if (preferGpu) {
                builder.setPreferredHardwareConfigs(PoseDetectorOptions.CPU_GPU)
            }
            return builder.build()
        } else {
            val builder =
                PoseDetectorOptions.Builder().setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            if (preferGpu) {
                builder.setPreferredHardwareConfigs(PoseDetectorOptions.CPU_GPU)
            }
            return builder.build()
        }
    }

    fun getVisualizeZ(): Boolean {
        return sharedPrefs.getBoolean("showZ", true)
    }

    fun rescaleZForVisualization(): Boolean {
        return sharedPrefs.getBoolean("showZ", true)
    }

    fun shouldShowOutLine(): Boolean {
        return sharedPrefs.getBoolean("showPose", false)
    }

    fun isMLImageEnabled(): Boolean {
        return sharedPrefs.getBoolean("isMlImage", true)
    }

    fun getClassifier(poseSamples: List<TrainedPoseSample>?): IAsanaClassifier {
        val useKnn = sharedPrefs.getBoolean("useKnn", true)
        return if (useKnn) KNNAsanaClassifier(poseSamples) else TFLiteAsanaClassifier(tfLiteModel)
    }

    fun shouldShowConfidence(): Boolean {
        return sharedPrefs.getBoolean("shouldShowConfidence", true)
    }

    fun isImportantLandMark(landmarkType: Int): Boolean {
        return landmarkType == PoseLandmark.LEFT_HIP
                || landmarkType == PoseLandmark.RIGHT_HIP
                || landmarkType == PoseLandmark.LEFT_SHOULDER
                || landmarkType == PoseLandmark.RIGHT_SHOULDER
                || landmarkType == PoseLandmark.LEFT_ELBOW
                || landmarkType == PoseLandmark.RIGHT_ELBOW
                || landmarkType == PoseLandmark.LEFT_WRIST
                || landmarkType == PoseLandmark.RIGHT_WRIST
                || landmarkType == PoseLandmark.LEFT_KNEE
                || landmarkType == PoseLandmark.RIGHT_KNEE
                || landmarkType == PoseLandmark.LEFT_ANKLE
                || landmarkType == PoseLandmark.RIGHT_ANKLE
                || landmarkType == PoseLandmark.NOSE
                || landmarkType == PoseLandmark.RIGHT_FOOT_INDEX
                || landmarkType == PoseLandmark.LEFT_FOOT_INDEX
    }

    companion object {
        const val TORSO_MULTIPLIER = 2.5f
        const val LANDMARK_CONF_THRESHOLD = 0.6f
    }
}