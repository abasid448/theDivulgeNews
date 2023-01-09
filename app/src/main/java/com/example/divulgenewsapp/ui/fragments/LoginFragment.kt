package com.example.divulgenewsapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.divulgenewsapp.R
import com.example.divulgenewsapp.databinding.FragmentLoginBinding
import com.example.divulgenewsapp.ui.NewsActivity
import com.example.divulgenewsapp.ui.UserActivity
import com.example.divulgenewsapp.ui.viewmodel.UserViewModel

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize view model.
        viewModel = (activity as UserActivity).viewModel
        viewModel.userMutableData.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                val intent = Intent(activity, NewsActivity::class.java)
                startActivity(intent)
                // Clear all previous activities
                activity?.finishAffinity()
            }
        })

        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(email, password)
            } else {
                Toast.makeText(activity, "Enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        //binding.btnRegister.setOnClickListener {
        //    Navigation.findNavController(binding.root)
        //        .navigate(R.id.action_userLoginFragment_to_userRegisterFragment)
        //}
    }
}