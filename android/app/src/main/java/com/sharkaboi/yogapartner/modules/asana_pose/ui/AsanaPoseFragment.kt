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
import com.google.mlkit.common.MlKitException
import com.sharkaboi.yogapartner.common.extensions.capitalizeFirst
import com.sharkaboi.yogapartner.common.extensions.observe
import com.sharkaboi.yogapartner.common.extensions.showToast
import com.sharkaboi.yogapartner.databinding.FragmentAsanaCameraBinding
import com.sharkaboi.yogapartner.ml.config.DetectorOptions
import com.sharkaboi.yogapartner.ml.detector.PoseDetectorProcessor
import com.sharkaboi.yogapartner.modules.asana_pose.ui.custom.GraphicOverlay
import com.sharkaboi.yogapartner.modules.asana_pose.vm.AsanaPoseViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@ExperimentalGetImage
@AndroidEntryPoint
class AsanaPoseFragment : Fragment() {
    private var _binding: FragmentAsanaCameraBinding? = null
    private val binding get() = _binding!!
    private val asanaPoseViewModel by viewModels<AsanaPoseViewModel>()
    private val navController get() = findNavController()

    private val previewView: PreviewView get() = binding.previewView
    private val landMarksOverlay: GraphicOverlay get() = binding.landmarksOverlay
    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var imageProcessor: PoseDetectorProcessor? = null
    private var needUpdateGraphicOverlayImageSourceInfo = false
    private var lensFacing = CameraSelector.LENS_FACING_FRONT
    private var cameraSelector: CameraSelector? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAsanaCameraBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        bindAllCameraUseCases()
    }

    override fun onPause() {
        super.onPause()
        imageProcessor?.run { this.stop() }
    }

    override fun onDestroyView() {
        imageProcessor?.run { this.stop() }
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
        val newLensFacing =
            if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
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
            bindPreviewUseCase()
            bindAnalysisUseCase()
        }
    }

    private fun bindPreviewUseCase() {
        if (!DetectorOptions.getInstance().isCameraLiveViewportEnabled()) {
            return
        }

        if (cameraProvider == null) {
            return
        }
        if (previewUseCase != null) {
            cameraProvider!!.unbind(previewUseCase)
        }

        val builder = Preview.Builder()
        val targetResolution =
            DetectorOptions.getInstance().getCameraXTargetResolution()
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution)
        }
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
        if (imageProcessor != null) {
            imageProcessor!!.stop()
        }

        try {
            val poseDetectorOptions = DetectorOptions.getInstance().getOption()
            val shouldShowInFrameLikelihood = DetectorOptions.getInstance().inFrameLikelihood()
            val visualizeZ = DetectorOptions.getInstance().getVisualizeZ()
            val rescaleZ = DetectorOptions.getInstance().rescaleZForVisualization()
            val runClassification =
                DetectorOptions.getInstance().shouldPoseDetectionRunClassification()
            imageProcessor = PoseDetectorProcessor(
                requireContext(),
                poseDetectorOptions,
                shouldShowInFrameLikelihood,
                visualizeZ,
                rescaleZ,
                runClassification,
                /* isStreamMode = */ DetectorOptions.getInstance().shouldShowReps()
            )
        } catch (e: Exception) {
            Timber.d("Can not create image processor", e)
            showToast(
                "Can not create image processor: " + e.localizedMessage,
            )
            return
        }

        val builder = ImageAnalysis.Builder()
        val targetResolution =
            DetectorOptions.getInstance().getCameraXTargetResolution()
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution)
        }
        analysisUseCase = builder.build()

        needUpdateGraphicOverlayImageSourceInfo = true

        analysisUseCase?.setAnalyzer(
            // imageProcessor.processImageProxy will use another thread to run the detection underneath,
            // thus we can just runs the analyzer itself on main thread.
            ContextCompat.getMainExecutor(requireContext()),
            { imageProxy: ImageProxy ->
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
                    imageProcessor!!.processImageProxy(
                        imageProxy, landMarksOverlay,
                        onInference = {
                            binding.tvInference.text =
                                it.classificationResult.firstOrNull()?.capitalizeFirst()
                                    ?: "Unknown"
                        }, isLoading = {
                            lifecycleScope.launch(Dispatchers.Main) {
                                binding.progress.isVisible = it
                            }
                        })
                } catch (e: MlKitException) {
                    Timber.d("Failed to process image. Error: " + e.localizedMessage)
                    showToast(e.localizedMessage)
                }
            }
        )
        cameraProvider!!.bindToLifecycle(viewLifecycleOwner, cameraSelector!!, analysisUseCase)
    }
}
