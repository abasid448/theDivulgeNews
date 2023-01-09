package com.example.divulgenewsapp.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.divulgenewsapp.models.Article
import com.example.divulgenewsapp.models.ExclusiveNews
import com.example.divulgenewsapp.models.NewsResponse
import com.example.divulgenewsapp.repository.NewsRepository
import com.example.divulgenewsapp.util.Resource
import com.example.divulgenewsapp.util.UiState
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(
    app: Application, private val newsRepository: NewsRepository
) : AndroidViewModel(app) {

    companion object {
        var newsCategory: String = "general"
    }

    /* Live data object is used for simultaneously subscribe to data as observers with fragments
       and whenever we post changes to the live data fragments will automatically get notified about that change. */
    val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var breakingNewsPage = 1
    var breakingNewsResponse: NewsResponse? = null

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null

    val isArticleExist: MutableLiveData<Boolean> = MutableLiveData()
    var articles: MutableList<Article> = mutableListOf()

    private val _savedArticlesFromFireStore = MutableLiveData<List<Article>>()
    val savedArticlesFromFireStore: LiveData<List<Article>>
        get() = _savedArticlesFromFireStore

    private val _deleteArticle = MutableLiveData<UiState<String>>()
    val deleteArticle: LiveData<UiState<String>>
        get() = _deleteArticle

    private val _exclusiveNewsFromFireStore = MutableLiveData<List<ExclusiveNews>>()
    val exclusiveNewsFromFireStore: LiveData<List<ExclusiveNews>>
        get() = _exclusiveNewsFromFireStore

    init {
        getBreakingNews(com.example.divulgenewsapp.Session.user.countryCode, newsCategory, false)
    }

    fun getBreakingNews(countryCode: String, category: String, isPagination: Boolean) = viewModelScope.launch {
        // View model scope will make sure that the co-routine is only alive as long as the view model is alive.
        if (!isPagination) breakingNewsPage = 1
        safeBreakingNewsCall(countryCode, category, isPagination)
    }

    fun searchNews(searchQuery: String, isPagination: Boolean) = viewModelScope.launch {
        if (!isPagination) searchNewsPage = 1
        safeSearchNewsCall(searchQuery, isPagination)
    }

    private fun handleBreakingNewsResponse(
        response: Response<NewsResponse>, isPagination: Boolean
    ): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                breakingNewsPage++
                if (breakingNewsResponse == null || !isPagination) {
                    breakingNewsResponse = resultResponse
                } else {
                    val oldArticles = breakingNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(breakingNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchNewsResponse(
        response: Response<NewsResponse>, isPagination: Boolean
    ): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                searchNewsPage++
                if (searchNewsResponse == null || !isPagination) {
                    searchNewsResponse = resultResponse
                } else {
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun saveArticle(article: Article) = viewModelScope.launch {
        article.uid = com.example.divulgenewsapp.Session.user.uid
        newsRepository.saveArticle(article)
    }

    fun isArticleExist(article: Article) = viewModelScope.launch {
        newsRepository.isArticleExistInFireStore(article.url!!) { state ->
            when (state) {
                is UiState.Success -> {
                    if (state.data) {
                        Log.e("ViewModelExist", "True")
                        isArticleExist.postValue(true)
                    } else {
                        isArticleExist.postValue(false)
                    }
                }
                is UiState.Failure -> {
                    Log.e("ViewModelExist", "False")
                }
                is UiState.Loading -> {}
            }
        }
    }

    private suspend fun safeBreakingNewsCall(
        countryCode: String, category: String, isPagination: Boolean
    ) {
        breakingNews.postValue(Resource.Loading())
        try {
            // Here we make the network response.
            if (hasInternetConnection()) {
                val response = newsRepository.getBreakingNews(countryCode, category, breakingNewsPage)
                breakingNews.postValue(handleBreakingNewsResponse(response, isPagination))
            } else {
                breakingNews.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            // If has internet connection and throws any other exception.
            when (t) {
                is IOException -> breakingNews.postValue(Resource.Error("Network Failure"))
                else -> breakingNews.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private suspend fun safeSearchNewsCall(searchQuery: String, isPagination: Boolean) {
        searchNews.postValue(Resource.Loading())
        try {
            // Here we make the network response.
            if (hasInternetConnection()) {
                val response = newsRepository.searchNews(searchQuery, searchNewsPage)
                searchNews.postValue(handleSearchNewsResponse(response, isPagination))
            } else {
                searchNews.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            // If has internet connection and throws any other exception.
            when (t) {
                is IOException -> searchNews.postValue(Resource.Error("Network Failure"))
                else -> searchNews.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    fun getSavedArticlesFromFireStore() = viewModelScope.launch {
        _savedArticlesFromFireStore.value = newsRepository.getSavedArticles().data!!
    }

    fun deleteArticle(article: Article) = viewModelScope.launch {
        _deleteArticle.value = UiState.Loading
        newsRepository.deleteArticleFromFireStore(article) {
            _deleteArticle.value = it
            Log.e("DeleteResult", it.toString())
        }
    }

    fun getExclusiveNews() = viewModelScope.launch {
        _exclusiveNewsFromFireStore.value = newsRepository.getExclusiveNews().data!!
    }

    private fun hasInternetConnection(): Boolean {
        // Get connectivity manager from application.
        val connectivityManager = getApplication<com.example.divulgenewsapp .NewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        // Check for connection.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }
}