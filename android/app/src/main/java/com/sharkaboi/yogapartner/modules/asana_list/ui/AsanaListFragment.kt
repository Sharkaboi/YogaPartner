package com.sharkaboi.yogapartner.modules.asana_list.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.sharkaboi.yogapartner.NavGraphDirections
import com.sharkaboi.yogapartner.common.extensions.showToast
import com.sharkaboi.yogapartner.databinding.FragmentAsanaListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AsanaListFragment : Fragment() {
    private var _binding: FragmentAsanaListBinding? = null
    private val binding get() = _binding!!
    private val navController get() = findNavController()
    private val permLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            handlePermissionCheck(it)
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAsanaListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        binding.fabOpenPose.setOnClickListener {
            checkPermission()
        }
    }

    private fun checkPermission() {
        permLauncher.launch(android.Manifest.permission.CAMERA)
    }

    private fun handlePermissionCheck(wasGranted: Boolean) {
        if (!wasGranted) {
            showToast("Need Camera permission")
            return
        }

        val direction = NavGraphDirections.openAsanaPose()
        navController.navigate(direction)
    }
}