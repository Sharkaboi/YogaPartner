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
import com.sharkaboi.yogapartner.R
import com.sharkaboi.yogapartner.common.extensions.observe
import com.sharkaboi.yogapartner.common.extensions.showToast
import com.sharkaboi.yogapartner.databinding.FragmentAsanaPoseBinding
import com.sharkaboi.yogapartner.ml.config.DetectorOptions
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
    private lateinit var ttsSpeechManager: TTSSpeechManager

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
        initTTS()
        initCamera()
        initListeners()
        initObservers()
    }

    private fun initTTS() {
        ttsSpeechManager = TTSSpeechManager(requireContext())
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
                binding.progress.isVisible = false
                return
            }
        } catch (e: Exception) {
        }
        showToast(
            "This device does not have lens with facing: $newLensFacing"
        )
        binding.progress.isVisible = false
    }

    private fun bindAllCameraUseCases() {
        if (cameraProvider != null) {
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
            asanaProcessor = AsanaProcessor(detectorOptions)
        } catch (e: Exception) {
            Timber.d("Can not create image processor", e)
            showToast("Can not create image processor: " + e.localizedMessage)
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
        setInferenceUi(poseWithAsanaClassification)
    }

    private fun setInferenceUi(poseWithAsanaClassification: PoseWithAsanaClassification) {
        val typeToConfidences = poseWithAsanaClassification.pose.allPoseLandmarks.map {
            Pair(it.landmarkType, it.inFrameLikelihood)
        }

        val isNotConfident = typeToConfidences.isEmpty() || typeToConfidences.any {
            detectorOptions.isImportantLandMark(it.first)
                    && it.second < DetectorOptions.LANDMARK_CONF_THRESHOLD
        }

        if (isNotConfident) {
            binding.tvInference.text = getString(R.string.unknown)
            binding.tvNotConfidentMessage.alpha = 1f
            return
        }

        binding.tvNotConfidentMessage.alpha = 0f

        val result = resultSmoother.getMajorityPose()
        val string = buildString {
            if (detectorOptions.shouldShowConfidence()) {
                append("${result.confidence.toInt()}% - ")
            }
            append(result.asanaClass.getFormattedString())
        }
        binding.tvInference.text = string

        ttsSpeechManager.speakAsana(result.asanaClass)
    }
}
