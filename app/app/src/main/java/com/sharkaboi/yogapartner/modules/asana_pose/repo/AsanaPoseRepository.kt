package com.sharkaboi.yogapartner.modules.asana_pose.repo

import android.content.Context
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.work.await
import com.google.mlkit.vision.pose.Pose
import com.sharkaboi.yogapartner.common.TaskState
import com.sharkaboi.yogapartner.ml.Landmarks
import com.sharkaboi.yogapartner.ml.PoseClassification
import com.sharkaboi.yogapartner.ml.PoseClassifier
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AsanaPoseRepository(
    private val poseClassifier: PoseClassifier,
    @ApplicationContext private val applicationContext: Context
) {
    private val executor = ContextCompat.getMainExecutor(applicationContext)

    suspend fun getPose(pose: Pose): TaskState<PoseClassification> =
        withContext(Dispatchers.IO) {
            poseClassifier.getPoseClassification(pose)
        }

    suspend fun getLandMarks(imageProxy: ImageProxy): TaskState<Landmarks> =
        withContext(Dispatchers.IO) {
            poseClassifier.getLandmarks(imageProxy)
        }

    suspend fun initAndGetCameraProvider(): TaskState<ProcessCameraProvider> =
        withContext(Dispatchers.Main) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(applicationContext)
            return@withContext try {
                val cameraProvider = cameraProviderFuture.await()
                TaskState.Success(cameraProvider)
            } catch (e: Exception) {
                TaskState.Failure(e.message ?: "Error occurred")
            }
        }

    fun stopProcessor() {
        poseClassifier.stop()
    }

    suspend fun initProcessor() {
        poseClassifier.init()
    }
}