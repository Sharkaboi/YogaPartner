package com.sharkaboi.yogapartner.ml.classification

import com.google.mlkit.vision.pose.Pose
import com.sharkaboi.yogapartner.ml.ModelV2
import com.sharkaboi.yogapartner.ml.utils.PoseEmbeddingUtils
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class TFLiteAsanaClassifier(private val model: ModelV2) : IAsanaClassifier {
    override fun classify(pose: Pose): ClassificationResult {
        val classificationResult = ClassificationResult()
        if (pose.allPoseLandmarks.isEmpty()) {
            return classificationResult
        }

        val inputFeature = TensorBuffer.createFixedSize(intArrayOf(1, 69), DataType.FLOAT32)
        val inputPose =
            PoseEmbeddingUtils.getPoseEmbedding(pose.allPoseLandmarks.map { it.position3D })
        val unzippedList = mutableListOf<Float>()
        inputPose.forEach {
            unzippedList.add(it.x)
            unzippedList.add(it.y)
            unzippedList.add(it.z)
        }
        inputFeature.loadArray(unzippedList.toFloatArray())
        val outputs = model.process(inputFeature)
        val output = outputs.outputFeature0AsTensorBuffer
        // Maintain same order as [labels_list] used while training
        val classes = listOf(
            AsanaClass.adho_mukha_svanasana,
            AsanaClass.bhujangasana,
            AsanaClass.bidalasana,
            AsanaClass.phalakasana,
            AsanaClass.ustrasana,
            AsanaClass.utkatasana,
            AsanaClass.utkata_konasana,
            AsanaClass.virabhadrasana_i,
            AsanaClass.virabhadrasana_ii,
            AsanaClass.vrikshasana
        )
        output.floatArray.forEachIndexed { index: Int, fl: Float ->
            classificationResult.putClassConfidence(classes[index], fl)
        }
        return classificationResult
    }

    override fun confidenceRange(): Float {
        return 0.01f
    }

    override fun close() {
        model.close()
    }
}