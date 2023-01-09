package com.example.divulgenewsapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.divulgenewsapp.models.ExclusiveNewsPlan
import com.example.divulgenewsapp.models.Subscription
import com.example.divulgenewsapp.repository.SubscriptionRepository
import com.example.divulgenewsapp.util.UiState
import kotlinx.coroutines.launch
import java.time.LocalDate

class SubscriptionViewModel(
    app: Application, private val repository: SubscriptionRepository
) : AndroidViewModel(app) {

    private val _exclusiveNewsPlansFromFireStore = MutableLiveData<List<ExclusiveNewsPlan>>()
    val exclusiveNewsPlansFromFireStore: LiveData<List<ExclusiveNewsPlan>>
        get() = _exclusiveNewsPlansFromFireStore

    fun getExclusiveNewsPlansFromFireStore() = viewModelScope.launch {
        _exclusiveNewsPlansFromFireStore.value = repository.getExclusiveNewsPlans().data!!
    }

    fun saveSubscriptionData(subscription: Subscription, result: (UiState<Boolean>) -> Unit) = viewModelScope.launch {
        repository.saveSubscriptionData(subscription, result)
    }

    fun checkUserHaveAnyActiveSubscription(result: (UiState<Boolean>) -> Unit) {
        viewModelScope.launch {
            repository.checkIfAnySubscriptionExists {
                when (it) {
                    is UiState.Failure -> {
                        result.invoke(UiState.Failure(it.error))
                    }
                    UiState.Loading -> TODO()
                    is UiState.Success -> {
                        if (it.data.documents.isEmpty()) {
                            result.invoke(UiState.Success(false))
                        } else {
                            var isExist = false
                            for (document in it.data.documents) {
                                val subscription = document.toObject(Subscription::class.java)
                                if (subscription != null) {
                                    val currentDate: LocalDate = LocalDate.now()
                                    val endsDate: LocalDate = LocalDate.parse(subscription.endsDate)

                                    isExist = currentDate <= endsDate
                                    if (isExist)
                                        break
                                }
                            }
                            result.invoke(UiState.Success(isExist))
                        }
                    }
                }
            }
        }
    }
}
