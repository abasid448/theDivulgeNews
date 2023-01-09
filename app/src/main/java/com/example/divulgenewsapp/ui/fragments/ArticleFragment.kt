package com.example.divulgenewsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.example.divulgenewsapp.R
import com.example.divulgenewsapp.databinding.FragmentArticleBinding
import com.example.divulgenewsapp.ui.NewsActivity
import com.example.divulgenewsapp.ui.viewmodel.NewsViewModel

class ArticleFragment : Fragment(R.layout.fragment_article) {

    private lateinit var viewModel: NewsViewModel
    private lateinit var binding: FragmentArticleBinding
    private val args: ArticleFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentArticleBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel

        // Get the clicked or current article passed from the other activity.
        val article = args.article

        // Display the news on the web view using the url.
        binding.webView.apply {
            /* Web view client for loading the url in the web view other
             than loading in the standard browser of the phone. */
            webViewClient = WebViewClient()
            loadUrl(article.url!!)
        }

        // Set on click listener for fab for saving article.
        binding.fab.setOnClickListener {
            if (viewModel.isArticleExist.value!!)
                viewModel.deleteArticle(article)
            else
                viewModel.saveArticle(article)
            viewModel.isArticleExist(article)
        }

        // Check if article exists.
        viewModel.isArticleExist.observe(viewLifecycleOwner, Observer
        {
            if (it) {
                Log.e("Observer", "True")
                changeFabToArticleExist()
            } else {
                Log.e("Observer", "False")
                changeFabToArticleNotExist()
            }
        })

//        viewModel.deleteArticle.observe(viewLifecycleOwner) { state ->
//            when (state) {
//                is UiState.Loading -> {}
//                is UiState.Failure -> {}
//                is UiState.Success<*> -> {
//                    viewModel.isArticleExist(article)
//                }
//            }
//        }

        viewModel.isArticleExist(article)
    }

    private fun changeFabToArticleExist() {
        try {
            binding.fab.setImageDrawable(
                ContextCompat.getDrawable(
                    activity as NewsActivity, R.drawable.ic_afterdelete
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeFabToArticleNotExist() {
        try {
            binding.fab.setImageDrawable(
                ContextCompat.getDrawable(
                    activity as NewsActivity, R.drawable.ic_heart
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}