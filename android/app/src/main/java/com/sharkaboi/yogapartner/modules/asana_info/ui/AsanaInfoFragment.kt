package com.sharkaboi.yogapartner.modules.asana_info.ui

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.sharkaboi.yogapartner.data.models.AsanaDifficulty
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
        binding.ivThumbnail.load(args.asana.asanaThumbnail)
        binding.tvDesc.text = args.asana.description
        binding.tvName.text = args.asana.name
        setDifficultySpan()
    }

    private fun setDifficultySpan() {
        val prefix = "Difficulty : "
        val difficultyText = "$prefix${args.asana.difficulty.name}"
        val spannableString = SpannableString(difficultyText)
        val whiteSpan = ForegroundColorSpan(Color.WHITE)
        val greenSpan = ForegroundColorSpan(Color.GREEN)
        val yellowSpan = ForegroundColorSpan(Color.YELLOW)
        val redSpan = ForegroundColorSpan(Color.RED)
        val blueSpan = ForegroundColorSpan(Color.BLUE)

        val difficultyColor = when (args.asana.difficulty) {
            AsanaDifficulty.UNKNOWN -> blueSpan
            AsanaDifficulty.EASY -> greenSpan
            AsanaDifficulty.MEDIUM -> yellowSpan
            AsanaDifficulty.DIFFICULT -> redSpan
        }
        spannableString.setSpan(whiteSpan, 0, prefix.lastIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(
            difficultyColor,
            prefix.lastIndex,
            spannableString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvDifficulty.text = spannableString
    }
}