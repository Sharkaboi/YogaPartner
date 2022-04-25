package com.sharkaboi.yogapartner.modules.asana_pose.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.pose.Pose
import com.sharkaboi.yogapartner.common.extensions.observe
import com.sharkaboi.yogapartner.common.extensions.showToast
import com.sharkaboi.yogapartner.databinding.FragmentAsanaPoseBinding
import com.sharkaboi.yogapartner.ml.Landmarks
import com.sharkaboi.yogapartner.ml.PoseClass
import com.sharkaboi.yogapartner.ml.config.DetectorOptions
import com.sharkaboi.yogapartner.ml.getFormattedString
import com.sharkaboi.yogapartner.modules.asana_pose.vm.AsanaPoseViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AsanaPoseFragment : Fragment() {
    private var _binding: FragmentAsanaPoseBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<AsanaPoseViewModel>()
    private val navController get() = findNavController()
    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var cameraSelector: CameraSelector? = null
    private var needUpdateGraphicOverlayImageSourceInfo = false
    private val mainExecutor get() = ContextCompat.getMainExecutor(requireContext())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAsanaPoseBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        setupBackButton()
        initCamera()
        initListeners()
        setupLandMarksObserver()
        setupPoseObserver()
        setupErrorObserver()
        setupLoadingObservers()
    }

    private fun setupBackButton() {
        binding.toolbar.setNavigationOnClickListener { navController.navigateUp() }
    }

    private fun initListeners() {
        binding.fabSwitchCamera.setOnClickListener {
            handleCameraSwitch()
        }
    }

    private fun handleCameraSwitch() {
        val cameraProvider = cameraProvider ?: return

        val newLensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }

        val newCameraSelector = CameraSelector.Builder().requireLensFacing(newLensFacing).build()
        val hasCamera = runCatching {
            cameraProvider.hasCamera(newCameraSelector)
        }.getOrDefault(false)
        if (hasCamera) {
            lensFacing = newLensFacing
            cameraSelector = newCameraSelector
            configureCamera()
            return
        }

        showToast("This device does not have lens with facing: $newLensFacing")
    }

    private fun initCamera() {
        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        observe(viewModel.cameraProviderLiveData) { provider ->
            cameraProvider = provider
            configureCamera()
        }
    }

    private fun configureCamera() {
        if (cameraProvider == null) {
            return
        }

        // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
        cameraProvider!!.unbindAll()
        bindPreviewUseCase()
        bindAnalysisUseCase()
    }

    private fun bindPreviewUseCase() {
        if (!DetectorOptions.isCameraLiveViewportEnabled()) {
            return
        }
        if (cameraProvider == null) {
            return
        }
        if (previewUseCase != null) {
            cameraProvider!!.unbind(previewUseCase)
        }

        val builder = Preview.Builder()
        previewUseCase = builder.build()
        previewUseCase!!.setSurfaceProvider(binding.cameraView.surfaceProvider)
        cameraProvider!!.bindToLifecycle(this, cameraSelector!!, previewUseCase)
    }

    private fun bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return
        }
        if (analysisUseCase != null) {
            cameraProvider!!.unbind(analysisUseCase)
        }
        viewModel.stopProcessor()
        viewModel.initProcessor()

        val builder = ImageAnalysis.Builder()
        analysisUseCase = builder.build()

        needUpdateGraphicOverlayImageSourceInfo = true
        val analyzer: (ImageProxy) -> Unit = { imageProxy: ImageProxy ->
            if (needUpdateGraphicOverlayImageSourceInfo) {
                val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                if (rotationDegrees == 0 || rotationDegrees == 180) {
                    binding.landmarksView.setImageSourceInfo(imageProxy.width, imageProxy.height, isImageFlipped)
                } else {
                    binding.landmarksView.setImageSourceInfo(imageProxy.height, imageProxy.width, isImageFlipped)
                }
                needUpdateGraphicOverlayImageSourceInfo = false
            }
            viewModel.processImageProxy(imageProxy)
        }
        // imageProcessor.processImageProxy will use another thread to run the detection underneath,
        // thus we can just runs the analyzer itself on main thread.
        analysisUseCase?.setAnalyzer(mainExecutor, analyzer)
        cameraProvider!!.bindToLifecycle(this, cameraSelector!!, analysisUseCase)
    }

    override fun onResume() {
        super.onResume()
        configureCamera()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onDestroy()
    }

//    private fun startCameraSource() {
//        if (cameraSource != null) {
//            try {
//                binding.cameraView.start(cameraSource!!)
//            } catch (e: IOException) {
//                Timber.d("Unable to start camera source.", e)
//                cameraSource!!.release()
//                cameraSource = null
//            }
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        initCamera()
//        startCameraSource()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        binding.cameraView.stop()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        if (cameraSource != null) {
//            cameraSource?.release()
//        }
//    }

    private fun setupPoseObserver() {
        observe(viewModel.currentPose) { pose ->
            handlePoseInferred(pose)
        }
    }

    private fun setupLandMarksObserver() {
        observe(viewModel.landmarks) { landmarks ->
            handleLandmarkUpdate(landmarks)
        }
    }

    private fun setupErrorObserver() {
        observe(viewModel.error) { error ->
            Timber.d(error)
            showToast(error)
        }
    }

    private fun handleLandmarkUpdate(landmarks: Landmarks) {
        binding.landmarksView.drawLandmarks(landmarks)
    }

    private fun setupLoadingObservers() {
        observe(viewModel.isLoading) { isLoading ->
            binding.progress.isVisible = isLoading
        }
//        observe(viewModel.isAsanaLoading) { isAsanaLoading ->
//            binding.tvInference.isGone = isAsanaLoading
//        }
    }

    private fun handlePoseInferred(classification: PoseClass) {
        binding.tvInference.text =
            classification.getFormattedString(requireContext())
    }
}