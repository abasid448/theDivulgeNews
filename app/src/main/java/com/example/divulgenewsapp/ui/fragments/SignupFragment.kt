package com.example.divulgenewsapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.divulgenewsapp.R
import com.example.divulgenewsapp.databinding.FragmentSignupBinding
import com.example.divulgenewsapp.ui.UserActivity
import com.example.divulgenewsapp.ui.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class SignupFragment : Fragment(R.layout.fragment_signup) {

    private lateinit var binding: FragmentSignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var viewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignupBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as UserActivity).viewModel
        viewModel.userMutableData.observe(viewLifecycleOwner, Observer {
            if (it != null) {
//                Navigation.findNavController(binding.root)
//                    .navigate(R.id.action_userRegisterFragment_to_userLoginFragment)
            }
        })


        firebaseAuth = FirebaseAuth.getInstance()
        binding.btnRegister.setOnClickListener {
            val name = binding.etUsername.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (formValidation(name, email, password, confirmPassword)) {
                // Register the user.
                viewModel.register(email, password)


                //firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                //    if (it.isSuccessful) {
                //        Navigation.findNavController(binding.root)
                //            .navigate(R.id.action_userRegisterFragment_to_userLoginFragment)
                //    } else {
                //        Toast.makeText(activity, it.exception!!.message, Toast.LENGTH_SHORT).show()
                //    }
                //}
            } else {
                Toast.makeText(activity, "Form validation failed", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSignIn.setOnClickListener {
//            Navigation.findNavController(binding.root)
//                .navigate(R.id.action_userRegisterFragment_to_userLoginFragment)
        }
    }

    private fun formValidation(
        name: String,
        email: String,
        password: String,
        confirm: String
    ): Boolean {
        return if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirm.isNotEmpty()) {
            password == confirm
        } else false
    }
}