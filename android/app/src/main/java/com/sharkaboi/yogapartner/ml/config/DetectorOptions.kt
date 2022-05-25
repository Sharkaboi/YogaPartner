package com.sharkaboi.yogapartner.ml.config

import android.content.Context
import android.content.SharedPreferences
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.sharkaboi.yogapartner.common.extensions.getOrSetDefault
import com.sharkaboi.yogapartner.ml.ConvertedModel
import com.sharkaboi.yogapartner.ml.classification.IAsanaClassifier
import com.sharkaboi.yogapartner.ml.classification.TFLiteAsanaClassifier
import com.sharkaboi.yogapartner.ml.utils.PoseEmbeddingUtils

class DetectorOptions(
    private val context: Context,
    private val sharedPrefs: SharedPreferences
) {
    fun setDefaults() {
        sharedPrefs.getOrSetDefault("showPose", false)
        sharedPrefs.getOrSetDefault("showZ", true)
        sharedPrefs.getOrSetDefault("shouldShowConfidence", false)
    }

    private val tfLiteModel get() = ConvertedModel.newInstance(context)

    fun getOption(): PoseDetectorOptionsBase {
        val builder: AccuratePoseDetectorOptions.Builder =
            AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        builder.setPreferredHardwareConfigs(PoseDetectorOptions.CPU_GPU)
        return builder.build()
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

    fun getClassifier(): IAsanaClassifier {
        return TFLiteAsanaClassifier(tfLiteModel)
    }

    fun shouldShowConfidence(): Boolean {
        return sharedPrefs.getBoolean("shouldShowConfidence", false)
    }

    fun isImportantLandMark(landmarkType: Int): Boolean {
        return PoseEmbeddingUtils.isImportantLandMark(landmarkType)
    }

    companion object {
        const val TORSO_MULTIPLIER = 2.5f
        const val LANDMARK_CONF_THRESHOLD = 0.6f
    }
}