package com.example.divulgenewsapp.repository

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.example.divulgenewsapp.models.User
import com.example.divulgenewsapp.preference.AuthPreference
import com.example.divulgenewsapp.util.Constants
import com.example.divulgenewsapp.util.Constants.Companion.PROFILE_IMAGES
import com.example.divulgenewsapp.util.Constants.Companion.UID
import com.example.divulgenewsapp.util.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

class UserRepository(
    private val app: Application,
) {
    val firebaseUserMutableLiveData: MutableLiveData<FirebaseUser> = MutableLiveData()
    val userLoggedStatusMutableLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val preference: AuthPreference = AuthPreference(app)
    private val userCollectionRef = Firebase.firestore.collection(Constants.USERS)

    init {
        if (auth.currentUser != null) {
            firebaseUserMutableLiveData.postValue(auth.currentUser)
        }
    }

    fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                firebaseUserMutableLiveData.postValue(auth.currentUser)
            } else {
                Toast.makeText(app, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                firebaseUserMutableLiveData.postValue(auth.currentUser)
            } else {
                Toast.makeText(app, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun signOut() {
        auth.signOut()
        userLoggedStatusMutableLiveData.postValue(true)
    }

    fun saveLoginData(email: String, password: String, isLoggedIn: Boolean) = preference.saveLoginData(email, password, isLoggedIn)

    fun getLoginData() = preference.getLoginData()

    fun clearSavedUserDataSharedPreference(key: String) = preference.clearSharedPreference(key)

    fun updateUserProfilePicture(profileImageUri: Uri, result: (UiState<Boolean>) -> Unit) {
        val reference = storage.reference.child(PROFILE_IMAGES).child(auth.uid.toString())
        reference.putFile(profileImageUri).addOnCompleteListener { profileImageTask ->
            if (profileImageTask.isSuccessful) {
                reference.downloadUrl.addOnSuccessListener {
                    result.invoke(UiState.Success(true))
                }
            } else {
                Toast.makeText(app, profileImageTask.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            result.invoke(UiState.Failure("Something went wrong, couldn't update user."))
        }
    }

    suspend fun saveUserProfileData(user: User, result: (UiState<Boolean>) -> Unit) = suspendCancellableCoroutine { continuation ->
        userCollectionRef.add(user).addOnSuccessListener {
            continuation.resume(result.invoke(UiState.Success(true)))
        }.addOnFailureListener {
            continuation.resume(result.invoke(UiState.Failure(it.message)))
        }
    }

    suspend fun updateUserProfileData(user: User, result: (UiState<Boolean>) -> Unit) {
        suspendCancellableCoroutine { continuation ->
            val updates = mapOf(
                Constants.FULL_NAME to user.fullName,
                Constants.EMAIL to user.email,
                Constants.COUNTRY_CODE to user.countryCode,
            )
            val userQuery = userCollectionRef.whereEqualTo(Constants.UID, auth.uid.toString())
            userQuery.get().addOnSuccessListener { query ->
                for (document in query.documents) {
                    val userId = document.id
                    val userRef = userCollectionRef.document(userId)
                    userRef.update(updates).addOnSuccessListener {
                        if (continuation.isActive) continuation.resume(
                            result.invoke(
                                UiState.Success(
                                    true
                                )
                            )
                        )
                    }.addOnFailureListener {
                        continuation.resume(result.invoke(UiState.Failure(it.message)))
                    }
                }
            }.addOnFailureListener {
                continuation.resume(result.invoke(UiState.Failure(it.message)))
            }
        }
    }

    suspend fun checkIfUserExistOnFireStore(result: (UiState<Boolean>) -> Unit) {
        suspendCancellableCoroutine { continuation ->
            if (auth.uid != null) {
                val docRef = userCollectionRef.document(auth.uid.toString())
                docRef.get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        val document = it.result
                        if (document != null && document.exists()) {
                            if (continuation.isActive) continuation.resume(
                                result.invoke(
                                    UiState.Success(
                                        true
                                    )
                                )
                            )
                        } else {
                            if (continuation.isActive) continuation.resume(
                                result.invoke(
                                    UiState.Success(
                                        false
                                    )
                                )
                            )
                        }
                    } else {
                        if (continuation.isActive) continuation.resume(
                            result.invoke(
                                UiState.Success(
                                    false
                                )
                            )
                        )
                    }
                }.addOnFailureListener {
                    if (continuation.isActive) continuation.resume(result.invoke(UiState.Failure(it.message)))
                }
            }
        }
    }

    suspend fun getUserFromFireStore(uid: String, result: (UiState<User>) -> Unit) {
        suspendCancellableCoroutine { continuation ->
            val query = userCollectionRef.whereEqualTo(UID, uid).limit(1)
            query.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    try {
                        val document = it.result.documents.first().data
                        val countryCode = document?.get(Constants.COUNTRY_CODE).toString()
                        val email = document?.get(Constants.EMAIL).toString()
                        val fullName = document?.get(Constants.FULL_NAME).toString()
                        val isUser = document?.get(Constants.USER_ROLE) as Boolean
                        val user = User(uid, fullName, email, countryCode, isUser)
                        continuation.resume(result.invoke(UiState.Success(user)))
                    } catch (e: NoSuchElementException) {
                        continuation.resume(result.invoke(UiState.Failure(e.message)))
                    }
                }
            }.addOnFailureListener {
                continuation.resume(result.invoke(UiState.Failure(it.message)))
            }
        }
    }

    suspend fun getProfileImageFromFirebaseStorage(result: (UiState<Bitmap>) -> Unit) {
        suspendCancellableCoroutine { cont ->
            val storageRef = storage.getReference("$PROFILE_IMAGES/${auth.uid}")
            try {
                val localFile = File.createTempFile("tempFile", "jpg")
                storageRef.getFile(localFile).addOnSuccessListener {
                    val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                    if (cont.isActive) cont.resume(result.invoke(UiState.Success(bitmap)))
                }.addOnFailureListener {
                    if (cont.isActive) result.invoke(UiState.Failure(it.message))
                }
            } catch (e: Exception) {
                if (cont.isActive) result.invoke(UiState.Failure(e.message))
            }
        }
    }
}