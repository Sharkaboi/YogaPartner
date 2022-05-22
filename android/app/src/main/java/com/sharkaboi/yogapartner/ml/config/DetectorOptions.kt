package com.sharkaboi.yogapartner.ml.config

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.sharkaboi.yogapartner.common.extensions.getOrSetDefault
import com.sharkaboi.yogapartner.ml.ConvertedModel
import com.sharkaboi.yogapartner.ml.classification.IAsanaClassifier
import com.sharkaboi.yogapartner.ml.classification.TFLiteAsanaClassifier
import com.sharkaboi.yogapartner.ml.models.TrainedPoseSample

class DetectorOptions private constructor() {

    // These thresholds can be tuned in conjunction with the Top K values in {@link PoseClassifier}.
    // The default Top K value is 10 so the range here is [0-10].
    val poseEnteredConfidenceThreshold = 6f
    val poseExitedConfidenceThreshold = 4f

    lateinit var sharedPrefs: SharedPreferences
    lateinit var tfliteModel: ConvertedModel

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
//        return KNNAsanaClassifier(poseSamples)
        return TFLiteAsanaClassifier(tfliteModel)
    }

    companion object {
        // Multiplier to apply to the torso to get minimal body size. Picked this by experimentation.
        /** [PoseEmbeddingUtils] */
        const val TORSO_MULTIPLIER = 2.5f

        const val LANDMARK_CONF_THRESHOLD = 0.6f

        private val instance = DetectorOptions()

        @JvmStatic
        fun getInstance() = instance

        fun init(context: Context) {
            instance.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            instance.tfliteModel = ConvertedModel.newInstance(context)
            initDefaultValuesIfNotPresent()
        }

        private fun initDefaultValuesIfNotPresent() {
            instance.sharedPrefs.getOrSetDefault("isMlImage", true)
            instance.sharedPrefs.getOrSetDefault("showPose", false)
            instance.sharedPrefs.getOrSetDefault("showZ", true)
            instance.sharedPrefs.getOrSetDefault("preferGpu", true)
            instance.sharedPrefs.getOrSetDefault("useAccurate", true)
        }
    }
}