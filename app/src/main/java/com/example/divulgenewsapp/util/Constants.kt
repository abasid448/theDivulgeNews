package com.example.divulgenewsapp.util

import com.example.divulgenewsapp.R
import com.example.divulgenewsapp.models.Country
import com.example.divulgenewsapp.models.NewsCategory

class Constants {
    companion object {
        const val IS_DEBUG = false

//        const val PAYPAL_CLIENT_KEY = "ATp-MZLvKozaNY90FKoyByOVIlVmg6TPBMI36o5x6uxJl7JmfI4XTIDDUJ0I5nB1nXfVG6RGS3pebR6x"
        const val API_KEY = "922028d2dec64af6bc1fbdddfad1e7fa"
        const val BASE_URL = "https://newsapi.org"
        const val SEARCH_NEWS_TIME_DELAY = 500L
        const val QUERY_PAGE_SIZE = 20

        // Remember login credentials.
        const val USER_AUTH = "user_auth"
        const val SHARED_KEY_EMAIL = "email"
        const val SHARED_KEY_PASSWORD = "password"
        const val SHARED_KEY_IS_LOGGED_IN = "is_logged_in"

        val NEWS_CATEGORIES = mutableListOf(
            NewsCategory(1, "Business", "business"),
            NewsCategory(2, "Entertainment", "entertainment"),
            NewsCategory(3, "General", "general"),
            NewsCategory(4, "Health", "health"),
            NewsCategory(5, "Science", "science"),
            NewsCategory(6, "Sports", "sports"),
            NewsCategory(7, "Technology", "technology"),
        )

        val COUNTRIES = arrayListOf<Country>(
            Country(1, "United Arab Emirates", "ae", R.drawable.uae),
            Country(2, "Argentina", "ar", R.drawable.argentina),
            Country(3, "Austria", "at", R.drawable.austria),
            Country(4, "United States of America", "us", R.drawable.usa),
        )

        //val COUNTRIES = arrayListOf<String>("us", "ae", "ar", "at")

        // Firebase values.
        const val PROFILE_IMAGES = "profileImages"
        const val USERS = "users"
        const val FULL_NAME = "fullName"
        const val EMAIL = "email"
        const val COUNTRY_CODE = "countryCode"
        const val ACTIVE_STATUS = "activeStatus"
        const val USER_ROLE = "userRole"
        const val UID = "uid"
        const val IS_USER_DEFAULT_VALUE = true
        const val USER_DEFAULT_COUNTRY_VALUE = "us"

        // Activity bundles
        const val IS_NEW_SUBSCRIPTION_TO_EXCLUSIVE_NEWS = "isNewSubscriptionToExclusiveNews"

        // Fragment tags
        const val EXCLUSIVE_NEWS_FRAGMENT_TAG = "exclusiveNewsFragment"
    }
}