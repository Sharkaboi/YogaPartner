

package com.sharkaboi.yogapartner.ml.classification;

import static com.sharkaboi.yogapartner.ml.utils.PointF3DUtils.multiplyAll;

import com.google.mlkit.vision.common.PointF3D;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.sharkaboi.yogapartner.ml.utils.PointF3DUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates embedding for given list of Pose landmarks.
 */
public class PoseEmbedding {
  // Multiplier to apply to the torso to get minimal body size. Picked this by experimentation.
  private static final float TORSO_MULTIPLIER = 2.5f;

  public static List<PointF3D> getPoseEmbedding(List<PointF3D> landmarks) {
    List<PointF3D> normalizedLandmarks = normalize(landmarks);
    return getEmbedding(normalizedLandmarks);
  }

  private static List<PointF3D> normalize(List<PointF3D> landmarks) {
    List<PointF3D> normalizedLandmarks = new ArrayList<>(landmarks);
    // Normalize translation.
    PointF3D center = PointF3DUtils.average(
        landmarks.get(PoseLandmark.LEFT_HIP), landmarks.get(PoseLandmark.RIGHT_HIP));
    PointF3DUtils.subtractAll(center, normalizedLandmarks);

    // Normalize scale.
    PointF3DUtils.multiplyAll(normalizedLandmarks, 1 / getPoseSize(normalizedLandmarks));
    // Multiplication by 100 is not required, but makes it easier to debug.
    PointF3DUtils.multiplyAll(normalizedLandmarks, 100);
    return normalizedLandmarks;
  }

  // Translation normalization should've been done prior to calling this method.
  private static float getPoseSize(List<PointF3D> landmarks) {
    // Note: This approach uses only 2D landmarks to compute pose size as using Z wasn't helpful
    // in our experimentation but you're welcome to tweak.
    PointF3D hipsCenter = PointF3DUtils.average(
        landmarks.get(PoseLandmark.LEFT_HIP), landmarks.get(PoseLandmark.RIGHT_HIP));

    PointF3D shouldersCenter = PointF3DUtils.average(
        landmarks.get(PoseLandmark.LEFT_SHOULDER),
        landmarks.get(PoseLandmark.RIGHT_SHOULDER));

    float torsoSize = PointF3DUtils.l2Norm2D(PointF3DUtils.subtract(hipsCenter, shouldersCenter));

    float maxDistance = torsoSize * TORSO_MULTIPLIER;
    // torsoSize * TORSO_MULTIPLIER is the floor we want based on experimentation but actual size
    // can be bigger for a given pose depending on extension of limbs etc so we calculate that.
    for (PointF3D landmark : landmarks) {
      float distance = PointF3DUtils.l2Norm2D(PointF3DUtils.subtract(hipsCenter, landmark));
      if (distance > maxDistance) {
        maxDistance = distance;
      }
    }
    return maxDistance;
  }

  private static List<PointF3D> getEmbedding(List<PointF3D> lm) {
    List<PointF3D> embedding = new ArrayList<>();

    // We use several pairwise 3D distances to form pose embedding. These were selected
    // based on experimentation for best results with our default pose classes as captued in the
    // pose samples csv. Feel free to play with this and add or remove for your use-cases.

    // We group our distances by number of joints between the pairs.
    // One joint.
    embedding.add(PointF3DUtils.subtract(
        PointF3DUtils.average(lm.get(PoseLandmark.LEFT_HIP), lm.get(PoseLandmark.RIGHT_HIP)),
        PointF3DUtils.average(lm.get(PoseLandmark.LEFT_SHOULDER), lm.get(PoseLandmark.RIGHT_SHOULDER))
    ));

    embedding.add(PointF3DUtils.subtract(
        lm.get(PoseLandmark.LEFT_SHOULDER), lm.get(PoseLandmark.LEFT_ELBOW)));
    embedding.add(PointF3DUtils.subtract(
        lm.get(PoseLandmark.RIGHT_SHOULDER), lm.get(PoseLandmark.RIGHT_ELBOW)));

    embedding.add(PointF3DUtils.subtract(lm.get(PoseLandmark.LEFT_ELBOW), lm.get(PoseLandmark.LEFT_WRIST)));
    embedding.add(PointF3DUtils.subtract(lm.get(PoseLandmark.RIGHT_ELBOW), lm.get(PoseLandmark.RIGHT_WRIST)));

    embedding.add(PointF3DUtils.subtract(lm.get(PoseLandmark.LEFT_HIP), lm.get(PoseLandmark.LEFT_KNEE)));
    embedding.add(PointF3DUtils.subtract(lm.get(PoseLandmark.RIGHT_HIP), lm.get(PoseLandmark.RIGHT_KNEE)));

    embedding.add(PointF3DUtils.subtract(lm.get(PoseLandmark.LEFT_KNEE), lm.get(PoseLandmark.LEFT_ANKLE)));
    embedding.add(PointF3DUtils.subtract(lm.get(PoseLandmark.RIGHT_KNEE), lm.get(PoseLandmark.RIGHT_ANKLE)));

    // Two joints.
    embedding.add(PointF3DUtils.subtract(
        lm.get(PoseLandmark.LEFT_SHOULDER), lm.get(PoseLandmark.LEFT_WRIST)));
    embedding.add(PointF3DUtils.subtract(
        lm.get(PoseLandmark.RIGHT_SHOULDER), lm.get(PoseLandmark.RIGHT_WRIST)));

    embedding.add(PointF3DUtils.subtract(lm.get(PoseLandmark.LEFT_HIP), lm.get(PoseLandmark.LEFT_ANKLE)));
    embedding.add(PointF3DUtils.subtract(lm.get(PoseLandmark.RIGHT_HIP), lm.get(PoseLandmark.RIGHT_ANKLE)));

    // Four joints.
    embedding.add(PointF3DUtils.subtract(lm.get(PoseLandmark.LEFT_HIP), lm.get(PoseLandmark.LEFT_WRIST)));
    embedding.add(PointF3DUtils.subtract(lm.get(PoseLandmark.RIGHT_HIP), lm.get(PoseLandmark.RIGHT_WRIST)));

    // Five joints.
    embedding.add(PointF3DUtils.subtract(
        lm.get(PoseLandmark.LEFT_SHOULDER), lm.get(PoseLandmark.LEFT_ANKLE)));
    embedding.add(PointF3DUtils.subtract(
        lm.get(PoseLandmark.RIGHT_SHOULDER), lm.get(PoseLandmark.RIGHT_ANKLE)));

    embedding.add(PointF3DUtils.subtract(lm.get(PoseLandmark.LEFT_HIP), lm.get(PoseLandmark.LEFT_WRIST)));
    embedding.add(PointF3DUtils.subtract(lm.get(PoseLandmark.RIGHT_HIP), lm.get(PoseLandmark.RIGHT_WRIST)));

    // Cross body.
    embedding.add(PointF3DUtils.subtract(lm.get(PoseLandmark.LEFT_ELBOW), lm.get(PoseLandmark.RIGHT_ELBOW)));
    embedding.add(PointF3DUtils.subtract(lm.get(PoseLandmark.LEFT_KNEE), lm.get(PoseLandmark.RIGHT_KNEE)));

    embedding.add(PointF3DUtils.subtract(lm.get(PoseLandmark.LEFT_WRIST), lm.get(PoseLandmark.RIGHT_WRIST)));
    embedding.add(PointF3DUtils.subtract(lm.get(PoseLandmark.LEFT_ANKLE), lm.get(PoseLandmark.RIGHT_ANKLE)));

    return embedding;
  }

  private PoseEmbedding() {}
}
