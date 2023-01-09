package com.example.divulgenewsapp.preference

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.divulgenewsapp.util.Constants.Companion.SHARED_KEY_EMAIL
import com.example.divulgenewsapp.util.Constants.Companion.SHARED_KEY_IS_LOGGED_IN
import com.example.divulgenewsapp.util.Constants.Companion.SHARED_KEY_PASSWORD
import com.example.divulgenewsapp.util.Constants.Companion.USER_AUTH

class AuthPreference(
    val app: Application,
) {
    private lateinit var preference: SharedPreferences

    fun saveLoginData(email: String, password: String, isLoggedIn: Boolean) {
        preference = app.getSharedPreferences(USER_AUTH, Context.MODE_PRIVATE)
        preference.edit().apply {
            putString(SHARED_KEY_EMAIL, email)
            putString(SHARED_KEY_PASSWORD, password)
            putBoolean(SHARED_KEY_IS_LOGGED_IN, isLoggedIn)
        }.apply()
    }

    fun getLoginData(): MutableList<HashMap<String, String>> {
        val loginHashMap: HashMap<String, String> = hashMapOf()
        val hashMaps: MutableList<HashMap<String, String>> = mutableListOf()

        preference = app.getSharedPreferences(USER_AUTH, Context.MODE_PRIVATE)
        // Get is user logged in or not.
        loginHashMap[SHARED_KEY_IS_LOGGED_IN] =
            preference.getBoolean(SHARED_KEY_IS_LOGGED_IN, false).toString()
        hashMaps.add(loginHashMap)

        // Get user saved email.
        loginHashMap[SHARED_KEY_EMAIL] =
            preference.getString(SHARED_KEY_EMAIL, null).toString()
        hashMaps.add(loginHashMap)

        // Get user saved password.
        loginHashMap[SHARED_KEY_PASSWORD] =
            preference.getString(SHARED_KEY_PASSWORD, null).toString()
        hashMaps.add(loginHashMap)

        return hashMaps
    }

    fun clearSharedPreference(key: String) {
        val preferences: SharedPreferences =
            app.getSharedPreferences(
                key,
                0
            )
        preferences.edit().clear().apply()
    }

}