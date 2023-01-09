package com.example.divulgenewsapp.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.divulgenewsapp.models.User
import com.example.divulgenewsapp.repository.UserRepository
import com.example.divulgenewsapp.util.Constants.Companion.SHARED_KEY_IS_LOGGED_IN
import com.example.divulgenewsapp.util.UiState
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch

class UserViewModel(
    app: Application,
) : AndroidViewModel(app) {

    private var repository: UserRepository
    var userMutableData: MutableLiveData<FirebaseUser>
    var loggedMutableStatus: MutableLiveData<Boolean>
    var verificationStatus = MutableLiveData<Boolean>()
    var phoneNumber = ""
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    var verificationId: String = ""

    init {
        repository = UserRepository(app)
        userMutableData = repository.firebaseUserMutableLiveData
        loggedMutableStatus = repository.userLoggedStatusMutableLiveData
    }

    fun register(email: String, password: String) {
        repository.register(email, password)
        repository.saveLoginData(email, password, true)
    }

    fun login(email: String, password: String) {
        repository.login(email, password)
        repository.saveLoginData(email, password, true)
    }

    fun signOut() = repository.signOut()

    var userData: MutableList<HashMap<String, String>> = mutableListOf()

    fun saveLoginData(email: String, password: String, isLoggedIn: Boolean) = repository.saveLoginData(email, password, isLoggedIn)

    fun getLoginData(): MutableList<HashMap<String, String>>? {
        userData = repository.getLoginData()
        val isLoggedIn = userData[0][SHARED_KEY_IS_LOGGED_IN].toBoolean()
        return if (isLoggedIn) userData else null
    }

    fun clearSavedUserDataSharedPreference(key: String) = repository.clearSavedUserDataSharedPreference(key)

    fun updateProfileImage(profileImageUri: Uri, result: (UiState<Boolean>) -> Unit) = repository.updateUserProfilePicture(profileImageUri, result)

    fun updateUserProfileData(user: User, result: (UiState<Boolean>) -> Unit) = viewModelScope.launch {
        repository.updateUserProfileData(user, result)
    }

    fun checkIfUserExistOnFireStore(result: (UiState<Boolean>) -> Unit) = viewModelScope.launch {
        repository.checkIfUserExistOnFireStore(result)
    }

    fun getUserFromFireStore(uid: String, result: (UiState<User>) -> Unit) = viewModelScope.launch {
        repository.getUserFromFireStore(uid, result)
    }

    fun getProfileImageFromFirebaseStorage(result: (UiState<Bitmap>) -> Unit) = viewModelScope.launch {
        repository.getProfileImageFromFirebaseStorage(result)
    }

    fun saveUserProfileData(user: User, result: (UiState<Boolean>) -> Unit) = viewModelScope.launch {
        repository.saveUserProfileData(user, result)
    }

    fun logout() {
        repository.signOut()
    }
}