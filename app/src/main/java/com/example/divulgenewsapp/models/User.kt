package com.example.divulgenewsapp.models

data class User(
    val uid: String,
    val fullName: String,
    val email: String,
    var countryCode: String,
    val userRole: Boolean
)