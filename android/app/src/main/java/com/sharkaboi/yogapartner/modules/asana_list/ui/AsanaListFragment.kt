package com.sharkaboi.yogapartner.modules.asana_list.ui

import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.sharkaboi.yogapartner.NavGraphDirections
import com.sharkaboi.yogapartner.common.extensions.observe
import com.sharkaboi.yogapartner.common.extensions.showToast
import com.sharkaboi.yogapartner.data.models.Asana
import com.sharkaboi.yogapartner.databinding.FragmentAsanaListBinding
import com.sharkaboi.yogapartner.modules.asana_list.adapter.AsanaListAdapter
import com.sharkaboi.yogapartner.modules.asana_list.vm.AsanaListViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AsanaListFragment : Fragment() {
    private lateinit var adapter: AsanaListAdapter
    private var _binding: FragmentAsanaListBinding? = null
    private val binding get() = _binding!!
    private val navController get() = findNavController()
    private val viewModel by viewModels<AsanaListViewModel>()
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
        binding.rvAsanas.adapter = null
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        setupRecyclerView()
        setupObservers()
    }

    private fun openSettings() {
        val destinations = NavGraphDirections.openSettings()
        navController.navigate(destinations)
    }

    private fun setupObservers() {
        observe(viewModel.currentList) {
            adapter.submitList(it)
            binding.tvEmptyHint.isVisible = it.isEmpty()
        }
        observe(viewModel.isLoading) {
            binding.progress.isVisible = it
        }
        observe(viewModel.errors) {
            showToast(it)
        }
    }


    private fun setupRecyclerView() {
        val rvAsanas = binding.rvAsanas
        this.adapter = AsanaListAdapter(
            onClick = {
                openYogaAsana(it)
            }
        )
        rvAsanas.adapter = this.adapter
        rvAsanas.layoutManager = LinearLayoutManager(context)
        rvAsanas.itemAnimator = DefaultItemAnimator()
        rvAsanas.setHasFixedSize(true)
    }

    private fun openYogaAsana(asana: Asana) {
        val action = NavGraphDirections.openAsanaDetails(asana)
        navController.navigate(action)
    }

    private fun setListeners() {
        binding.fabOpenPose.setOnClickListener {
            checkPermission()
        }
        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrBlank()) {
                viewModel.resetSearch()
                return@doOnTextChanged
            }

            viewModel.search(text.toString())
        }
        binding.btnSettings.setOnClickListener { openSettings() }
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