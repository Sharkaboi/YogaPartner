package com.sharkaboi.yogapartner.ml.utils

import com.google.mlkit.vision.common.PointF3D
import com.google.mlkit.vision.pose.PoseLandmark
import com.sharkaboi.yogapartner.ml.config.DetectorOptions

object PoseEmbeddingUtils {
    private const val TORSO_MULTIPLIER = DetectorOptions.TORSO_MULTIPLIER

    @JvmStatic
    fun getPoseEmbedding(landmarks: List<PointF3D>): List<PointF3D> {
        val normalizedLandmarks = normalize(landmarks)
        return getEmbedding(normalizedLandmarks)
    }

    private fun normalize(landmarks: List<PointF3D>): List<PointF3D> {
        val normalizedLandmarks = ArrayList(landmarks)
        // Normalize translation.
        val center = PointF3DUtils.average(
            landmarks[PoseLandmark.LEFT_HIP], landmarks[PoseLandmark.RIGHT_HIP]
        )
        PointF3DUtils.subtractAll(center, normalizedLandmarks)

        // Normalize scale.
        PointF3DUtils.multiplyAll(normalizedLandmarks, 1 / getPoseSize(normalizedLandmarks))
        // Multiplication by 100 is not required, but makes it easier to debug.
        PointF3DUtils.multiplyAll(normalizedLandmarks, 100f)
        return normalizedLandmarks
    }

    // Translation normalization should've been done prior to calling this method.
    private fun getPoseSize(landmarks: List<PointF3D>): Float {
        // Note: This approach uses only 2D landmarks to compute pose size as using Z wasn't helpful
        val hipsCenter = PointF3DUtils.average(
            landmarks[PoseLandmark.LEFT_HIP], landmarks[PoseLandmark.RIGHT_HIP]
        )
        val shouldersCenter = PointF3DUtils.average(
            landmarks[PoseLandmark.LEFT_SHOULDER],
            landmarks[PoseLandmark.RIGHT_SHOULDER]
        )
        val torsoSize = PointF3DUtils.l2Norm2D(PointF3DUtils.subtract(hipsCenter, shouldersCenter))
        var maxDistance = torsoSize * TORSO_MULTIPLIER
        // torsoSize * TORSO_MULTIPLIER is the floor we want but actual size
        // can be bigger for a given pose depending on extension of limbs etc so we calculate that.
        for (landmark in landmarks) {
            val distance = PointF3DUtils.l2Norm2D(PointF3DUtils.subtract(hipsCenter, landmark))
            if (distance > maxDistance) {
                maxDistance = distance
            }
        }
        return maxDistance
    }

    // 23 embeddings
    private fun getEmbedding(lm: List<PointF3D>): List<PointF3D> {
        val embedding: ArrayList<PointF3D> = ArrayList()

        // We use several pairwise 3D distances to form pose embedding. These were selected
        // based on experimentation for best results

        // Group our distances by number of joints between the pairs.
        // One joint. - (9)
        embedding.add(
            PointF3DUtils.subtract(
                PointF3DUtils.average(
                    lm[PoseLandmark.LEFT_HIP],
                    lm[PoseLandmark.RIGHT_HIP]
                ),
                PointF3DUtils.average(
                    lm[PoseLandmark.LEFT_SHOULDER],
                    lm[PoseLandmark.RIGHT_SHOULDER]
                )
            )
        )
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.LEFT_SHOULDER],
                lm[PoseLandmark.LEFT_ELBOW]
            )
        )
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.RIGHT_SHOULDER],
                lm[PoseLandmark.RIGHT_ELBOW]
            )
        )
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.LEFT_ELBOW],
                lm[PoseLandmark.LEFT_WRIST]
            )
        )
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.RIGHT_ELBOW],
                lm[PoseLandmark.RIGHT_WRIST]
            )
        )
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.LEFT_HIP],
                lm[PoseLandmark.LEFT_KNEE]
            )
        )
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.RIGHT_HIP],
                lm[PoseLandmark.RIGHT_KNEE]
            )
        )
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.LEFT_KNEE],
                lm[PoseLandmark.LEFT_ANKLE]
            )
        )
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.RIGHT_KNEE],
                lm[PoseLandmark.RIGHT_ANKLE]
            )
        )

        // Two joints. (4)
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.LEFT_SHOULDER],
                lm[PoseLandmark.LEFT_WRIST]
            )
        )
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.RIGHT_SHOULDER],
                lm[PoseLandmark.RIGHT_WRIST]
            )
        )
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.LEFT_HIP],
                lm[PoseLandmark.LEFT_ANKLE]
            )
        )
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.RIGHT_HIP],
                lm[PoseLandmark.RIGHT_ANKLE]
            )
        )

        // Four joints. (2)
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.LEFT_HIP],
                lm[PoseLandmark.LEFT_WRIST]
            )
        )
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.RIGHT_HIP],
                lm[PoseLandmark.RIGHT_WRIST]
            )
        )

        // Five joints. (4)
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.LEFT_SHOULDER],
                lm[PoseLandmark.LEFT_ANKLE]
            )
        )
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.RIGHT_SHOULDER],
                lm[PoseLandmark.RIGHT_ANKLE]
            )
        )
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.LEFT_HIP],
                lm[PoseLandmark.LEFT_WRIST]
            )
        )
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.RIGHT_HIP],
                lm[PoseLandmark.RIGHT_WRIST]
            )
        )

        // Cross body. (4)
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.LEFT_ELBOW],
                lm[PoseLandmark.RIGHT_ELBOW]
            )
        )
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.LEFT_KNEE],
                lm[PoseLandmark.RIGHT_KNEE]
            )
        )
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.LEFT_WRIST],
                lm[PoseLandmark.RIGHT_WRIST]
            )
        )
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.LEFT_ANKLE],
                lm[PoseLandmark.RIGHT_ANKLE]
            )
        )
        return embedding
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
    }
}
