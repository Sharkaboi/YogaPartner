package com.sharkaboi.yogapartner.ml

import android.graphics.Bitmap
import com.google.mlkit.vision.pose.Pose

data class Landmarks(
    val pose: Pose,
    val originalImage: Bitmap?
)
