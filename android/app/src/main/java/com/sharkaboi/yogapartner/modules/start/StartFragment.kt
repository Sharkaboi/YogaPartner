package com.sharkaboi.yogapartner.modules.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sharkaboi.yogapartner.R

class StartFragment : Fragment() {
    private val navController get() = findNavController()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start, container, false)
    }

    override fun onStart() {
        super.onStart()
        checkLogin()
    }

    private fun checkLogin() {
        val user = Firebase.auth.currentUser
        if (user == null) {
            navigateToAuth()
        } else {
            navigateToMain()
        }
    }

    private fun navigateToMain() {
        navController.navigate(StartFragmentDirections.openMain())
    }

    private fun navigateToAuth() {
        navController.navigate(StartFragmentDirections.openAuth())
    }
}