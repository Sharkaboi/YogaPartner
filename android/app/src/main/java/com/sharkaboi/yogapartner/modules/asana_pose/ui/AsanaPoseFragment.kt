package com.sharkaboi.yogapartner.modules.asana_pose.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.mlkit.vision.pose.Pose
import com.sharkaboi.yogapartner.R
import com.sharkaboi.yogapartner.common.extensions.capitalizeFirst
import com.sharkaboi.yogapartner.common.extensions.observe
import com.sharkaboi.yogapartner.common.extensions.showToast
import com.sharkaboi.yogapartner.databinding.FragmentAsanaPoseBinding
import com.sharkaboi.yogapartner.ml.config.DetectorOptions
import com.sharkaboi.yogapartner.ml.log.LatencyLogger
import com.sharkaboi.yogapartner.ml.models.PoseWithAsanaClassification
import com.sharkaboi.yogapartner.ml.processor.AsanaProcessor
import com.sharkaboi.yogapartner.modules.asana_pose.ui.custom.LandMarksOverlay
import com.sharkaboi.yogapartner.modules.asana_pose.util.ResultSmoother
import com.sharkaboi.yogapartner.modules.asana_pose.util.TTSSpeechManager
import com.sharkaboi.yogapartner.modules.asana_pose.vm.AsanaPoseViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@ExperimentalGetImage
@AndroidEntryPoint
class AsanaPoseFragment : Fragment() {

    @Inject
    lateinit var detectorOptions: DetectorOptions

    private var _binding: FragmentAsanaPoseBinding? = null
    private val binding get() = _binding!!
    private val asanaPoseViewModel by viewModels<AsanaPoseViewModel>()
    private val navController get() = findNavController()
    private val mainExecutor get() = ContextCompat.getMainExecutor(requireContext())
    private val resultSmoother = ResultSmoother()
    private val ttsSpeechManager = TTSSpeechManager(requireContext())

    private val previewView: PreviewView get() = binding.previewView
    private val landMarksOverlay: LandMarksOverlay get() = binding.landmarksOverlay
    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var asanaProcessor: AsanaProcessor? = null
    private var needUpdateGraphicOverlayImageSourceInfo = false
    private var lensFacing = CameraSelector.LENS_FACING_FRONT
    private var cameraSelector: CameraSelector? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAsanaPoseBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        bindAllCameraUseCases()
    }

    override fun onPause() {
        ttsSpeechManager.stop()
        super.onPause()
        asanaProcessor?.run { this.stop() }
    }

    override fun onDestroyView() {
        asanaProcessor?.run { this.stop() }
        ttsSpeechManager.stop()
        ttsSpeechManager.shutdown()
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCamera()
        initListeners()
        initObservers()
    }

    private fun initCamera() {
        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
    }

    private fun initListeners() {
        binding.btnSwitchCamera.setOnClickListener { switchCamera() }
        binding.toolbar.setNavigationOnClickListener { navController.navigateUp() }
    }

    private fun initObservers() {
        observe(asanaPoseViewModel.processCameraProvider) { provider: ProcessCameraProvider? ->
            cameraProvider = provider
            bindAllCameraUseCases()
        }
    }

    private fun switchCamera() {
        if (cameraProvider == null) {
            return
        }

        binding.progress.isVisible = true
        val newLensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        val newCameraSelector = CameraSelector.Builder().requireLensFacing(newLensFacing).build()
        try {
            if (cameraProvider!!.hasCamera(newCameraSelector)) {
                Timber.d("Set facing to $newLensFacing")
                lensFacing = newLensFacing
                cameraSelector = newCameraSelector
                bindAllCameraUseCases()
                // TODO: 12-05-2022 restart detector and not use tracker
                binding.progress.isVisible = false
                return
            }
        } catch (e: Exception) {
            // Falls through
        }
        showToast(
            "This device does not have lens with facing: $newLensFacing"
        )
        binding.progress.isVisible = false
    }

    private fun bindAllCameraUseCases() {
        if (cameraProvider != null) {
            // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
            cameraProvider!!.unbindAll()
            setCameraPreviewToSurfaceView()
            bindAnalysisUseCase()
        }
    }

    private fun setCameraPreviewToSurfaceView() {
        if (cameraProvider == null) {
            return
        }

        if (previewUseCase != null) {
            cameraProvider!!.unbind(previewUseCase)
        }

        val builder = Preview.Builder()
        previewUseCase = builder.build()
        previewUseCase!!.setSurfaceProvider(previewView.surfaceProvider)
        cameraProvider!!.bindToLifecycle(viewLifecycleOwner, cameraSelector!!, previewUseCase)
    }

    private fun bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return
        }

        if (analysisUseCase != null) {
            cameraProvider!!.unbind(analysisUseCase)
        }

        if (asanaProcessor != null) {
            asanaProcessor!!.stop()
        }

        resultSmoother.clearCache()

        try {
            asanaProcessor = AsanaProcessor(
                requireContext(),
                LatencyLogger(),
                detectorOptions
            )
        } catch (e: Exception) {
            Timber.d("Can not create image processor", e)
            showToast(
                "Can not create image processor: " + e.localizedMessage,
            )
            FirebaseCrashlytics.getInstance().recordException(e)
            return
        }

        val builder = ImageAnalysis.Builder()
        analysisUseCase = builder.build()

        needUpdateGraphicOverlayImageSourceInfo = true

        analysisUseCase?.setAnalyzer(mainExecutor) { imageProxy: ImageProxy ->
            handleImageProxy(imageProxy)
        }
        binding.landmarksOverlay.clear()
        cameraProvider!!.bindToLifecycle(viewLifecycleOwner, cameraSelector!!, analysisUseCase)
    }

    private fun handleImageProxy(imageProxy: ImageProxy) {
        if (needUpdateGraphicOverlayImageSourceInfo) {
            val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            if (rotationDegrees == 0 || rotationDegrees == 180) {
                landMarksOverlay.setImageSourceInfo(
                    imageProxy.width,
                    imageProxy.height,
                    isImageFlipped
                )
            } else {
                landMarksOverlay.setImageSourceInfo(
                    imageProxy.height,
                    imageProxy.width,
                    isImageFlipped
                )
            }
            needUpdateGraphicOverlayImageSourceInfo = false
        }
        try {
            asanaProcessor!!.processImageProxy(
                imageProxy,
                landMarksOverlay,
                onInference = ::onInference,
                isLoading = ::isLoadingCallback
            )
        } catch (e: Exception) {
            Timber.d("Failed to process image. Error: " + e.localizedMessage)
            showToast(e.localizedMessage)
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun isLoadingCallback(isLoading: Boolean) {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.progress.isVisible = isLoading
        }
    }

    private fun onInference(poseWithAsanaClassification: PoseWithAsanaClassification) {
        resultSmoother.setInferredPose(poseWithAsanaClassification)
        setInferenceUi()
        checkDistanceFromCamera(poseWithAsanaClassification.pose)
        speakAsana(poseWithAsanaClassification)
    }

    private fun speakAsana(poseWithAsanaClassification: PoseWithAsanaClassification) {
        ttsSpeechManager.speakAsana(poseWithAsanaClassification.classification.asanaClass)
    }

    private fun setInferenceUi() {
        val result = resultSmoother.getMajorityPose()
        val string = buildString {
            if (detectorOptions.shouldShowConfidence()) {
                append("${result.confidence.toInt()}% - ")
            }
            append(result.asanaClass.getFormattedString().capitalizeFirst())
        }
        binding.tvInference.text = string
    }

    private fun checkDistanceFromCamera(pose: Pose) {
        val typeToConfidences =
            pose.allPoseLandmarks.map { Pair(it.landmarkType, it.inFrameLikelihood) }
        val isNotConfident = typeToConfidences.any {
            isImportantTypeLandmark(it.first) && it.second < DetectorOptions.LANDMARK_CONF_THRESHOLD
        } || typeToConfidences.isEmpty()
        if (isNotConfident) {
            binding.tvInference.text = getString(R.string.unknown)
        }
        binding.tvNotConfidentMessage.alpha = if (isNotConfident) 1f else 0f
    }

    private fun isImportantTypeLandmark(landmarkType: Int): Boolean {
        return detectorOptions.isImportantLandMark(landmarkType)
    }
}
