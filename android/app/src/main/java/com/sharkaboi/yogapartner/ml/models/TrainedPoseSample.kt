package com.sharkaboi.yogapartner.ml.models

import com.google.common.base.Splitter
import com.google.mlkit.vision.common.PointF3D
import com.sharkaboi.yogapartner.ml.utils.PoseEmbeddingUtils
import timber.log.Timber

/**
 * Reads Pose samples from a csv file.
 */
data class TrainedPoseSample(
    val name: String,
    val className: String,
    val landmarks: List<PointF3D>
) {

    val embedding: List<PointF3D> = PoseEmbeddingUtils.getPoseEmbedding(landmarks)

    companion object {
        // 33 points on body detected.
        private const val NUM_LANDMARKS = 33

        // X, Y, Z coords
        private const val NUM_DIMS = 3

        @JvmStatic
        fun getPoseSample(csvLine: String, separator: String): TrainedPoseSample? {
            val tokens = Splitter.onPattern(separator).splitToList(csvLine)
            // Format is expected to be Name,Class,X1,Y1,Z1,X2,Y2,Z2...
            // + 2 is for Name & Class.
            if (tokens.size != NUM_LANDMARKS * NUM_DIMS + 2) {
                Timber.d("Invalid number of tokens for PoseSample")
                return null
            }
            val name = tokens[0]
            val className = tokens[1]
            val landmarks: ArrayList<PointF3D> = ArrayList<PointF3D>()
            // Read from the third token, first 2 tokens are name and class.
            var i = 2
            while (i < tokens.size) {
                try {
                    landmarks.add(
                        PointF3D.from(
                            tokens[i].toFloat(),
                            tokens[i + 1].toFloat(),
                            tokens[i + 2].toFloat()
                        )
                    )
                } catch (e: NullPointerException) {
                    Timber.d("Invalid value " + tokens[i] + " for landmark position.")
                    return null
                } catch (e: NumberFormatException) {
                    Timber.d("Invalid value " + tokens[i] + " for landmark position.")
                    return null
                }
                i += NUM_DIMS
            }
            return TrainedPoseSample(name, className, landmarks)
        }
    }
}
