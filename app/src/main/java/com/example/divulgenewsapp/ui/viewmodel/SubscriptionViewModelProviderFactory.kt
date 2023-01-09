package com.example.divulgenewsapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.divulgenewsapp.repository.SubscriptionRepository

class SubscriptionViewModelProviderFactory(
    val app: Application,
    private val repository: SubscriptionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SubscriptionViewModel(app, repository) as T
    }
}