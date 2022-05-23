package com.sharkaboi.yogapartner.ml.models

import com.google.mlkit.vision.pose.Pose
import com.sharkaboi.yogapartner.ml.classification.AsanaClass

data class PoseWithAsanaClassification(
    val pose: Pose,
    val classification: Classification
)

data class Classification(
    val asanaClass: AsanaClass,
    val confidence: Float
)