package com.example.divulgenewsapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.divulgenewsapp.R
import com.example.divulgenewsapp.databinding.FragmentMobileNumberBinding
import com.example.divulgenewsapp.ui.UserActivity
import com.example.divulgenewsapp.ui.viewmodel.UserViewModel
import com.example.divulgenewsapp.util.Constants.Companion.IS_DEBUG
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.concurrent.TimeUnit

class RegisterMobileNumberFragment : Fragment(R.layout.fragment_mobile_number) {

    private lateinit var binding: FragmentMobileNumberBinding
    private lateinit var viewModel: UserViewModel
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private val TAG = "RegisterMobileNumberFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMobileNumberBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        // Initialize view model from activity.
        viewModel = (activity as UserActivity).viewModel

        // Set OnClick listener for get otp.
        binding.btnGetOtp.setOnClickListener {
            var countryCode = binding.etCountryCode.text.toString().trim()
            val phoneNumber = binding.etPhoneNumber.text.toString().trim()

            // Add plus sign if it is not there.
            if (!countryCode.startsWith("+")) {
                countryCode = "+ $countryCode"
            }

            if (isValidCountryCode(countryCode)) {
                sendOtp(countryCode + phoneNumber)
            } else {
                Toast.makeText(activity, "Phone number is invalid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isValidCountryCode(countryCode: String): Boolean {
        val phoneUtil = PhoneNumberUtil.getInstance()
        return try {
            // Get the region code for the given country code.
            val regionCode = phoneUtil.getRegionCodeForCountryCode(countryCode.toInt())
            // If the region code is not null, then the country code is valid.
            regionCode != null
        } catch (e: NumberFormatException) {
            // If the country code is not a valid integer, then it is not valid.
            false
        }
    }

    private fun sendOtp(phoneNumber: String) {
        // Show progress bar.
        binding.progressBar.visibility = View.VISIBLE
        binding.btnGetOtp.visibility = View.INVISIBLE

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {}

            override fun onVerificationFailed(e: FirebaseException) {
                binding.progressBar.visibility = View.GONE
                binding.btnGetOtp.visibility = View.VISIBLE
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
                binding.btnGetOtp.visibility = View.VISIBLE
                viewModel.phoneNumber = phoneNumber
                viewModel.verificationId = verificationId
                viewModel.resendToken = token
                Navigation.findNavController(binding.root)
                    .navigate(R.id.action_registerMobileNumberFragment_to_verifyOtpFragment)
            }
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity as UserActivity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}