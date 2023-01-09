package com.example.divulgenewsapp.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.divulgenewsapp.R
import com.example.divulgenewsapp.Session
import com.example.divulgenewsapp.models.User
import com.example.divulgenewsapp.repository.UserRepository
import com.example.divulgenewsapp.util.Constants.Companion.IS_USER_DEFAULT_VALUE
import com.example.divulgenewsapp.util.Constants.Companion.USER_DEFAULT_COUNTRY_VALUE
import com.example.divulgenewsapp.util.UiState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        com.example.divulgenewsapp.Session.user = User("", "", "", "us", IS_USER_DEFAULT_VALUE)
        val auth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            //Open the next activity.
            val intent: Intent
            if (auth.currentUser == null) {
                startActivity(Intent(this@SplashScreenActivity, UserActivity::class.java))
                finish()
            } else {
                getUserDataToSessionAndGotoNews(auth.uid.toString())
            }
        }, 2000)
    }

    private fun getUserDataToSessionAndGotoNews(uid: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val repository = UserRepository(application)
            repository.getUserFromFireStore(uid) {
                when (it) {
                    is UiState.Failure -> {
                        com.example.divulgenewsapp.Session.user = User("", "", "", USER_DEFAULT_COUNTRY_VALUE, IS_USER_DEFAULT_VALUE)
                        startActivity(
                            Intent(this@SplashScreenActivity, UserActivity::class.java)
                        )
                        finish()
                    }
                    is UiState.Loading -> {}
                    is UiState.Success -> {
                        Session.user = it.data
                        startActivity(
                            Intent(this@SplashScreenActivity, NewsActivity::class.java)
                        )
                        finish()
                    }
                }
            }
        }
    }
}