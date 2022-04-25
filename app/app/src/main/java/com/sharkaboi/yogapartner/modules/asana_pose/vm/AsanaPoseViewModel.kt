package com.sharkaboi.yogapartner.modules.asana_pose.vm

import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharkaboi.yogapartner.common.TaskState
import com.sharkaboi.yogapartner.ml.Landmarks
import com.sharkaboi.yogapartner.ml.PoseClass
import com.sharkaboi.yogapartner.ml.PoseClassification
import com.sharkaboi.yogapartner.modules.asana_pose.repo.AsanaPoseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AsanaPoseViewModel
@Inject
constructor(
    private val asanaPoseRepository: AsanaPoseRepository
) : ViewModel() {
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isAsanaLoading = MutableLiveData(false)
    val isAsanaLoading: LiveData<Boolean> = _isAsanaLoading

    private val _errors = MutableLiveData<String>()
    val error: LiveData<String> = _errors

    private val _landmarks = MutableLiveData<Landmarks>()
    val landmarks: LiveData<Landmarks> = _landmarks

    private val _currentPose = MutableLiveData(PoseClass.UNKNOWN)
    val currentPose: LiveData<PoseClass> = _currentPose

    private val _cameraProviderLiveData = MutableLiveData<ProcessCameraProvider>()
    val cameraProviderLiveData: LiveData<ProcessCameraProvider> = _cameraProviderLiveData

    init {
        initCameraProvider()
    }

    private fun initCameraProvider() {
        _isLoading.value = true
        viewModelScope.launch {
            val taskState = asanaPoseRepository.initAndGetCameraProvider()
            _isLoading.value = false
            when (taskState) {
                is TaskState.Failure -> _errors.value = taskState.message
                is TaskState.Success -> _cameraProviderLiveData.value = taskState.value
            }
        }
    }

    private fun handlePoseUpdate(pose: PoseClassification) {
        _currentPose.value = pose.getMostConfidentClass()
    }

    private fun handleLandmarksUpdate(landmarks: Landmarks) {
        _landmarks.value = landmarks
    }

    fun processImageProxy(imageProxy: ImageProxy) {
        viewModelScope.launch {
            val landmarkResult = asanaPoseRepository.getLandMarks(imageProxy)
            processLandMarkResult(landmarkResult)
        }
    }

    private fun processLandMarkResult(landmarkResult: TaskState<Landmarks>) {
        when (landmarkResult) {
            is TaskState.Failure -> {
                _errors.value = landmarkResult.message
                return
            }
            is TaskState.Success -> {
                handleLandmarksUpdate(landmarkResult.value)
                getClassifiedPose(landmarkResult.value)
            }
            is TaskState.NoErrorFailure -> return
        }
    }

    private fun getClassifiedPose(landmarks: Landmarks) {
        _isAsanaLoading.value = true
        viewModelScope.launch {
            val poseResult = asanaPoseRepository.getPose(landmarks.pose)
            _isAsanaLoading.value = false
            when (poseResult) {
                is TaskState.Failure -> _errors.value = poseResult.message
                is TaskState.Success -> handlePoseUpdate(poseResult.value)
            }
        }
    }

    fun onPause() {
        stopProcessor()
    }

    fun onDestroy() {
        stopProcessor()
    }

    fun stopProcessor() {
        asanaPoseRepository.stopProcessor()
    }

    fun initProcessor() {
        viewModelScope.launch {
            asanaPoseRepository.initProcessor()
        }
    }
}