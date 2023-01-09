package com.example.divulgenewsapp.repository

import com.example.divulgenewsapp.models.ExclusiveNewsPlan
import com.example.divulgenewsapp.models.Subscription
import com.example.divulgenewsapp.util.Constants.Companion.UID
import com.example.divulgenewsapp.util.Resource
import com.example.divulgenewsapp.util.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SubscriptionRepository {

    private val exclusiveNewsPlanCollectionRef = Firebase.firestore.collection("exclusive_news_plans")
    private val subscriptionsCollectionRef = Firebase.firestore.collection("subscriptions")

    suspend fun getExclusiveNewsPlans(): Resource<List<ExclusiveNewsPlan>> = suspendCoroutine { continuation ->
        exclusiveNewsPlanCollectionRef.get().addOnSuccessListener {
            try {
                continuation.resume(Resource.Success(it.toObjects()))
            } catch (e: Exception) {
                continuation.resume(Resource.Error(e.message!!, null))
            }
        }.addOnFailureListener {
            continuation.resume(Resource.Error(it.message!!, null))
        }
    }

    suspend fun saveSubscriptionData(subscription: Subscription, result: (UiState<Boolean>) -> Unit) = suspendCancellableCoroutine { continuation ->
        subscriptionsCollectionRef.add(subscription).addOnSuccessListener {
            continuation.resume(result.invoke(UiState.Success(true)))
        }.addOnFailureListener {
            continuation.resume(result.invoke(UiState.Failure(it.message)))
        }
    }

    suspend fun checkIfAnySubscriptionExists(result: (UiState<QuerySnapshot>) -> Unit) {
        suspendCancellableCoroutine { continuation ->
            if (FirebaseAuth.getInstance().uid != null) {
                val query = subscriptionsCollectionRef.whereEqualTo(UID, FirebaseAuth.getInstance().uid.toString())
                query.get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (continuation.isActive) continuation.resume(result.invoke(UiState.Success(it.result)))
                    } else {
                        if (continuation.isActive) continuation.resume(result.invoke(UiState.Success(it.result)))
                    }
                }.addOnFailureListener {
                    continuation.resume(result.invoke(UiState.Failure(it.message)))
                }
            }
        }
    }
}