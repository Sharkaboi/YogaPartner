package com.sharkaboi.yogapartner.modules.asana_info.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.sharkaboi.yogapartner.databinding.FragmentAsanaInfoBinding

class AsanaInfoFragment : Fragment() {
    private var _binding: FragmentAsanaInfoBinding? = null
    private val binding get() = _binding!!
    private val navController get() = findNavController()
    private val args by navArgs<AsanaInfoFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAsanaInfoBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
    }

    private fun initData() {
        binding.btnBack.setOnClickListener {
            navController.navigateUp()
        }
        binding.ivThumbnail.load(args.asana.asanaThumbnail) {
            scale(Scale.FILL)
            transformations(RoundedCornersTransformation(8f))
        }
        binding.tvDesc.text = args.asana.description
        binding.tvName.text = args.asana.name
        binding.tvDifficulty.text = ("Difficulty : ${args.asana.difficulty.name}")
    }
}