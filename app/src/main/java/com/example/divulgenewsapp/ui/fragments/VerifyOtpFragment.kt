package com.example.divulgenewsapp.ui.fragments

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.divulgenewsapp.R
import com.example.divulgenewsapp.databinding.FragmentOtpBinding
import com.example.divulgenewsapp.models.User
import com.example.divulgenewsapp.ui.UserActivity
import com.example.divulgenewsapp.ui.viewmodel.UserViewModel
import com.example.divulgenewsapp.util.Constants.Companion.IS_DEBUG
import com.example.divulgenewsapp.util.Constants.Companion.IS_USER_DEFAULT_VALUE
import com.example.divulgenewsapp.util.Constants.Companion.USER_DEFAULT_COUNTRY_VALUE
import com.example.divulgenewsapp.util.UiState
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class VerifyOtpFragment : Fragment(R.layout.fragment_otp) {

    private lateinit var binding: FragmentOtpBinding
    private lateinit var viewModel: UserViewModel
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private val TAG = "VerifyOtpFragment"
    private lateinit var timer: CountDownTimer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOtpBinding.inflate(layoutInflater)
        initializeComponents()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val inputMethodManager = (activity as UserActivity).getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(
            InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY
        )
        binding.otpView.requestFocus()
    }

    override fun onStart() {
        super.onStart()
        timer.start()
    }

    override fun onStop() {
        super.onStop()
        timer.start()
        timer.cancel()
    }

    private fun initializeComponents() {
        // Initialize view model from activity.
        viewModel = (activity as UserActivity).viewModel

        // Set Mobile Number on the text view.
        binding.tvPhoneNumber.text = viewModel.phoneNumber

        binding.tvChangePhoneNumber.setOnClickListener {
            Toast.makeText(activity, "OTP sent successfully", Toast.LENGTH_SHORT).show()
        }

        binding.tvChangePhoneNumber.setOnClickListener {
            // Go back to the mobile number fragment.
            findNavController().navigateUp()
        }

        binding.btnVerifyOtp.setOnClickListener(btnVerifyOtpOnClickListener)

        binding.tvResendOtp.setOnClickListener(btnResentOtpOnClickListener)

        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(remaining: Long) {
                binding.tvOtpTimer.text = "Seconds remaining: ${convertMillisecondsToMinutesAndSeconds(remaining)}"
            }

            override fun onFinish() {
                binding.tvOtpTimer.text = "OTP has expired, please resend"
            }
        }
    }

    private val btnResentOtpOnClickListener = View.OnClickListener {
        try {
            sendOtp()
            timer.cancel()
            timer.start()
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
            Toast.makeText(activity, "Couldn't resend OTP, please try again", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun sendOtp() {
        // Show progress bar.
        binding.progressBar.visibility = View.VISIBLE

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            }

            override fun onVerificationFailed(e: FirebaseException) {
                binding.progressBar.visibility = View.GONE
                Log.e(TAG, e.toString())
                if (IS_DEBUG) {
                    Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                }
                Toast.makeText(activity, getString(R.string.registration_failed_please_check_entered_phone_number), Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                binding.progressBar.visibility = View.GONE
                viewModel.verificationId = verificationId
            }
        }

        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(viewModel.phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity as UserActivity)
            .setCallbacks(callbacks)
            .setForceResendingToken(viewModel.resendToken)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val btnVerifyOtpOnClickListener = View.OnClickListener {
        val code = binding.otpView.text.toString().trim()

        if (code.length >= 6) {
            if (viewModel.verificationId != "") {
                // Set progress bar visible.
                hideKeyboard(activity as UserActivity, binding.otpView)
                binding.progressBar.visibility = View.VISIBLE
                binding.btnVerifyOtp.visibility = View.INVISIBLE

                val credential = PhoneAuthProvider.getCredential(viewModel.verificationId, code)
                signInWithPhoneAuthCredential(credential)
            }
        } else {
            Toast.makeText(activity, "Invalid OTP", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                if (!checkIfExistingUser()) {
                    saveUserProfileData()
                }
                // Hide the keyboard after verifying otp.
            } else {
                binding.progressBar.visibility = View.GONE
                binding.btnVerifyOtp.visibility = View.VISIBLE
                Toast.makeText(activity, "Invalid OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getUserDataToSession() {
        viewModel.getUserFromFireStore(FirebaseAuth.getInstance().uid.toString()) {
            when (it) {
                is UiState.Failure -> {
                    if (IS_DEBUG)
                        Toast.makeText(activity, it.error, Toast.LENGTH_LONG).show()
                }
                UiState.Loading -> {}
                is UiState.Success -> {
                    com.example.divulgenewsapp.Session.user = it.data
                    Navigation.findNavController(binding.root).navigate(R.id.action_verifyOtpFragment_to_selectCountryFragment)
                    if (IS_DEBUG) {
                        Toast.makeText(activity, "User saved on session", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun checkIfExistingUser(): Boolean {
        var isExist = false
        viewModel.checkIfUserExistOnFireStore {
            when (it) {
                is UiState.Failure -> {
                    Toast.makeText(activity, "User doesn't exist", Toast.LENGTH_SHORT).show()
                }
                is UiState.Loading -> {}
                is UiState.Success -> {
                    isExist = it.data
                }
            }
        }
        return isExist
    }

    private fun saveUserProfileData() {
        viewModel.saveUserProfileData(
            User(
                FirebaseAuth.getInstance().uid.toString(), "", "", USER_DEFAULT_COUNTRY_VALUE, IS_USER_DEFAULT_VALUE
            )
        ) {
            when (it) {
                is UiState.Failure -> {
                    Toast.makeText(activity, it.error, Toast.LENGTH_SHORT).show()
                    Log.e("VerifyOtp", it.error.toString())
                }
                UiState.Loading -> {}
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnVerifyOtp.visibility = View.VISIBLE
                    getUserDataToSession()
                }
            }
        }
    }

    private fun hideKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun convertMillisecondsToMinutesAndSeconds(milliseconds: Long): String {
        val minutes = (milliseconds / 1000) / 60
        val seconds = (milliseconds / 1000) % 60
        return "$minutes:$seconds"
    }
}