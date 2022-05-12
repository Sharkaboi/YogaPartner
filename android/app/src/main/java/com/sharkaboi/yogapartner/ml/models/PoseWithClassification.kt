package com.sharkaboi.yogapartner.ml.models

import com.google.mlkit.vision.pose.Pose
import com.sharkaboi.yogapartner.ml.classification.PoseClass

data class PoseWithClassification(
    val pose: Pose,
    val classificationResult: PoseClass
)