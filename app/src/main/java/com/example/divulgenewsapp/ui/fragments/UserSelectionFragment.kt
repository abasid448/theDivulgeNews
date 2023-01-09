package com.example.divulgenewsapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.divulgenewsapp.R
import com.example.divulgenewsapp.databinding.FragmentUserSelectionBinding

class UserSelectionFragment : Fragment(R.layout.fragment_user_selection) {

    private lateinit var binding: FragmentUserSelectionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserSelectionBinding.inflate(layoutInflater)
        binding.btnRegister.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_userSelectionFragment_to_registerMobileNumberFragment)
        }

        return binding.root
    }
}