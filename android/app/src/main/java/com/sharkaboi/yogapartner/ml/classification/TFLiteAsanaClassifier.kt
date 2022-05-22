package com.sharkaboi.yogapartner.ml.classification

import com.google.mlkit.vision.pose.Pose
import com.sharkaboi.yogapartner.ml.ConvertedModel
import com.sharkaboi.yogapartner.ml.utils.PoseEmbeddingUtils
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer

class TFLiteAsanaClassifier(private val model: ConvertedModel) : IAsanaClassifier {
    override fun classify(pose: Pose): ClassificationResult {
        val inputFeature = TensorBuffer.createFixedSize(intArrayOf(1, 69), DataType.FLOAT32)
        val inputPose =
            PoseEmbeddingUtils.getPoseEmbedding(pose.allPoseLandmarks.map { it.position3D })
        val unzippedList = ByteBuffer.allocate(inputPose.size * 3)
        inputPose.forEach {
            unzippedList.putFloat(it.x)
            unzippedList.putFloat(it.y)
            unzippedList.putFloat(it.z)
        }
        inputFeature.loadBuffer(unzippedList)
        val outputs = model.process(inputFeature)
        val output = outputs.outputFeature0AsTensorBuffer
        val classes = listOf(
            AsanaClass.bhujangasana,
            AsanaClass.marjaryasana,
            AsanaClass.virabhadrasana_ii,
            AsanaClass.virabhadrasana_i,
            AsanaClass.vriksasana,
            AsanaClass.phalakasana,
            AsanaClass.ustrasana,
            AsanaClass.utkatasana,
            AsanaClass.upavistha_konasana,
            AsanaClass.adho_mukha_svanasana
        )
        val classificationResult = ClassificationResult()
        output.floatArray.forEachIndexed { index: Int, fl: Float ->
            classificationResult.putClassConfidence(classes[index], fl)
        }
        return classificationResult
    }

    override fun confidenceRange(): Float {
        return 0.01f
    }
}