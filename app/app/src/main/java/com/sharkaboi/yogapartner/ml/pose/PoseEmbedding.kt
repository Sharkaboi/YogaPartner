package com.sharkaboi.yogapartner.ml.pose

import com.google.mlkit.vision.common.PointF3D
import com.google.mlkit.vision.pose.PoseLandmark
import com.sharkaboi.yogapartner.ml.util.PointF3DUtils

/**
 * Generates embedding for given list of Pose landmarks.
 */
object PoseEmbedding {
    // Multiplier to apply to the torso to get minimal body size. Picked this by experimentation.
    private const val TORSO_MULTIPLIER = 2.5f
    fun getPoseEmbedding(landmarks: List<PointF3D>): List<PointF3D> {
        val normalizedLandmarks = normalize(landmarks)
        return getEmbedding(normalizedLandmarks)
    }

    private fun normalize(landmarks: List<PointF3D>): List<PointF3D> {
        val normalizedLandmarks: MutableList<PointF3D> = ArrayList(landmarks)
        // Normalize translation.
        val center: PointF3D = PointF3DUtils.average(
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
        // in our experimentation but you're welcome to tweak.
        val hipsCenter: PointF3D = PointF3DUtils.average(
            landmarks[PoseLandmark.LEFT_HIP], landmarks[PoseLandmark.RIGHT_HIP]
        )
        val shouldersCenter: PointF3D = PointF3DUtils.average(
            landmarks[PoseLandmark.LEFT_SHOULDER],
            landmarks[PoseLandmark.RIGHT_SHOULDER]
        )
        val torsoSize: Float =
            PointF3DUtils.l2Norm2D(PointF3DUtils.subtract(hipsCenter, shouldersCenter))
        var maxDistance = torsoSize * TORSO_MULTIPLIER
        // torsoSize * TORSO_MULTIPLIER is the floor we want based on experimentation but actual size
        // can be bigger for a given pose depending on extension of limbs etc so we calculate that.
        for (landmark in landmarks) {
            val distance: Float =
                PointF3DUtils.l2Norm2D(PointF3DUtils.subtract(hipsCenter, landmark))
            if (distance > maxDistance) {
                maxDistance = distance
            }
        }
        return maxDistance
    }

    private fun getEmbedding(lm: List<PointF3D>): List<PointF3D> {
        val embedding: MutableList<PointF3D> = ArrayList()

        // We use several pairwise 3D distances to form pose embedding. These were selected
        // based on experimentation for best results with our default pose classes as captued in the
        // pose samples csv. Feel free to play with this and add or remove for your use-cases.

        // We group our distances by number of joints between the pairs.
        // One joint.
        embedding.add(
            PointF3DUtils.subtract(
                PointF3DUtils.average(lm[PoseLandmark.LEFT_HIP], lm[PoseLandmark.RIGHT_HIP]),
                PointF3DUtils.average(
                    lm[PoseLandmark.LEFT_SHOULDER],
                    lm[PoseLandmark.RIGHT_SHOULDER]
                )
            )
        )
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.LEFT_SHOULDER], lm[PoseLandmark.LEFT_ELBOW]
            )
        )
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.RIGHT_SHOULDER], lm[PoseLandmark.RIGHT_ELBOW]
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
        embedding.add(PointF3DUtils.subtract(lm[PoseLandmark.LEFT_HIP], lm[PoseLandmark.LEFT_KNEE]))
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

        // Two joints.
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.LEFT_SHOULDER], lm[PoseLandmark.LEFT_WRIST]
            )
        )
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.RIGHT_SHOULDER], lm[PoseLandmark.RIGHT_WRIST]
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

        // Four joints.
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

        // Five joints.
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.LEFT_SHOULDER], lm[PoseLandmark.LEFT_ANKLE]
            )
        )
        embedding.add(
            PointF3DUtils.subtract(
                lm[PoseLandmark.RIGHT_SHOULDER], lm[PoseLandmark.RIGHT_ANKLE]
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

        // Cross body.
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
}