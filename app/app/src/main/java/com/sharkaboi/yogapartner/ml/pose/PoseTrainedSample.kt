package com.sharkaboi.yogapartner.ml.pose

import com.google.common.base.Splitter
import com.google.mlkit.vision.common.PointF3D
import timber.log.Timber

data class PoseTrainedSample(
    val name: String,
    val className: String,
    val landmarks: List<PointF3D>
) {
    val embedding: List<PointF3D> = PoseEmbedding.getPoseEmbedding(landmarks)

    companion object {
        private const val NUM_LANDMARKS = 33
        private const val NUM_DIMS = 3

        fun getPoseSample(csvLine: String, separator: String): PoseTrainedSample? {
            val tokens = Splitter.onPattern(separator).splitToList(csvLine)
            // Format is expected to be Name,Class,X1,Y1,Z1,X2,Y2,Z2...
            // + 2 is for Name & Class.
            if (tokens.size != ((NUM_LANDMARKS * NUM_DIMS) + 2)) {
                Timber.d("Invalid number of tokens for PoseSample $tokens")
                return null
            }
            val name = tokens[0]
            val className = tokens[1]
            val landmarks: MutableList<PointF3D> = mutableListOf()
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
            return PoseTrainedSample(name, className, landmarks)
        }
    }
}
