package com.sharkaboi.yogapartner.ml.models

import com.google.mlkit.vision.pose.Pose

data class PoseWithClassification(
    val pose: Pose,
    val classificationResult: List<String>
)