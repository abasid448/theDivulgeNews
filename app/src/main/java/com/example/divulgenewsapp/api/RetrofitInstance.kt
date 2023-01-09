package com.example.divulgenewsapp.api

import com.example.divulgenewsapp.util.Constants.Companion.BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    companion object {
        private val retrofit by lazy {
            // Initialize logging interceptor for log every requests and response for easy debugging.
            val logging = HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY)

            // Build http client for api requests.
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            /* Make Convert the response to kotlin object
            using GSON(Google implementation of JSON) convertor factory. */
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }

        // Get API instance from retrofit builder.
        val api: NewsAPI by lazy {
            retrofit.create(NewsAPI::class.java)
        }
    }
}