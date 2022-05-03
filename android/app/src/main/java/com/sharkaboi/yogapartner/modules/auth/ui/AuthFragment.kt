package com.sharkaboi.yogapartner.modules.auth.ui

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sharkaboi.yogapartner.common.extensions.showToast
import com.sharkaboi.yogapartner.databinding.FragmentAuthBinding
import timber.log.Timber

class AuthFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private val navController get() = findNavController()
    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!
    private val REQ_ONE_TAP = 69420

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
        oneTapClient = Identity.getSignInClient(requireContext())
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
        val tokenBuilder =
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId("755981629368-73av377kh3h0rvr6m5qsrm3od8ofdv89.apps.googleusercontent.com")
                .setFilterByAuthorizedAccounts(false)
                .build()
        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(tokenBuilder)
            .build()
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(requireActivity()) { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP,
                        null, 0, 0, 0, null
                    )
                } catch (e: IntentSender.SendIntentException) {
                    binding.btnSignIn.isEnabled = true
                    showToast("Couldnt launch google sign in")
                    Timber.e("Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }.addOnFailureListener(requireActivity()) { e ->
                binding.btnSignIn.isEnabled = true
                showToast("No google accounts logged in")
                Timber.d(e.localizedMessage)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val googleCredential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = googleCredential.googleIdToken
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with Firebase.
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(requireActivity()) { task ->
                                    if (task.isSuccessful) {
                                        Timber.d("signInWithCredential:success")
                                        loginSuccess()
                                    } else {
                                        binding.btnSignIn.isEnabled = true
                                        showToast("Firebase sign in failed")
                                        Timber.w("signInWithCredential:failure " + task.exception)
                                    }
                                }
                        }
                        else -> {
                            binding.btnSignIn.isEnabled = true
                            showToast("No ID token!")
                            Timber.d("No ID token!")
                        }
                    }

                } catch (e: Exception) {
                    binding.btnSignIn.isEnabled = true
                    showToast("An error occurred")
                    Timber.d(e.message)
                }
            }
        }
    }

    private fun loginSuccess() {
        binding.btnSignIn.isEnabled = true
        showToast(("Welcome " + auth.currentUser?.displayName.orEmpty()).trim())
        navigateToMain()
    }

    private fun navigateToMain() {
        navController.navigate(AuthFragmentDirections.openMain())
    }

}