package com.sharkaboi.yogapartner.modules.auth.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.sharkaboi.yogapartner.R
import com.sharkaboi.yogapartner.common.extensions.showToast
import com.sharkaboi.yogapartner.databinding.FragmentAuthBinding
import timber.log.Timber

class AuthFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private val navController get() = findNavController()
    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private val signInLauncher = registerForActivityResult(FirebaseAuthUIActivityResultContract()) {
        this.onSignInResult(it)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        setListeners()
    }

    private fun setListeners() {
        binding.btnSignIn.setOnClickListener {
            it.isEnabled = false
            signIn()
        }
    }

    private fun signIn() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.ic_launcher_foreground)
            .setTheme(R.style.Theme_MaterialComponents)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun loginSuccess() {
        showToast(("Welcome " + auth.currentUser?.displayName.orEmpty()).trim())
        navigateToMain()
    }

    private fun navigateToMain() {
        navController.navigate(AuthFragmentDirections.openMain())
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        binding.btnSignIn.isEnabled = true
        val response = result.idpResponse
        if (response == null) {
            showToast("Please login first")
            Timber.d("Cancelled sign in")
            return
        }

        val error = response.error
        if (result.resultCode == Activity.RESULT_OK) {
            loginSuccess()
            return
        }

        showToast("An error occurred with code ${error?.errorCode}")
        Timber.d(error?.message)
        if (error != null) {
            FirebaseCrashlytics.getInstance().recordException(error)
        }
    }
}