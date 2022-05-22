package com.sharkaboi.yogapartner.ml.classification

import com.google.mlkit.vision.pose.Pose

interface IAsanaClassifier {
    fun classify(pose: Pose): ClassificationResult
    fun confidenceRange(): Float
}