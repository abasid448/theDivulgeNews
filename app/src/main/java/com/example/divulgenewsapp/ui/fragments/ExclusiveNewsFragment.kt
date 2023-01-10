package com.example.divulgenewsapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.divulgenewsapp.R
import com.example.divulgenewsapp.adapters.ExclusiveNewsAdapter
import com.example.divulgenewsapp.databinding.FragmentExclusiveNewsBinding
import com.example.divulgenewsapp.models.Article
import com.example.divulgenewsapp.models.ExclusiveNews
import com.example.divulgenewsapp.models.Source
import com.example.divulgenewsapp.ui.NewsActivity
import com.example.divulgenewsapp.ui.NewsSubscriptionActivity
import com.example.divulgenewsapp.ui.viewmodel.NewsViewModel
import com.example.divulgenewsapp.ui.viewmodel.SubscriptionViewModel
import com.example.divulgenewsapp.util.UiState


class ExclusiveNewsFragment : Fragment(R.layout.fragment_exclusive_news) {

    private lateinit var binding: FragmentExclusiveNewsBinding
    private lateinit var exclusiveNewsAdapter: ExclusiveNewsAdapter
    private lateinit var viewModel: NewsViewModel
    private lateinit var subscriptionViewModel: SubscriptionViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentExclusiveNewsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel
        subscriptionViewModel = (activity as NewsActivity).subscriptionViewModel

        setupRecyclerView()

        exclusiveNewsAdapter.setOnItemClickListener {
            // Get the clicked the article and put into a bundle and attach the bundle to a navigation component.

            val bundle = Bundle().apply {
                putSerializable("exclusive_news", it)
            }
            // Fragment transition.
            findNavController().navigate(
                R.id.action_exclusiveNewsFragment_to_exclusiveNewsViewFragment, bundle
            )
        }
        checkIsSubscriptionExist()
    }

    override fun onStart() {
        super.onStart()
        subscriptionViewModel.checkUserHaveAnyActiveSubscription {
            when (it) {
                is UiState.Failure -> {
                    Toast.makeText(activity, "Couldn't complete request, please try again", Toast.LENGTH_SHORT).show()
                }
                UiState.Loading -> {}
                is UiState.Success -> {
                    if (it.data) {
                        viewModel.getExclusiveNews()
                        viewModel.exclusiveNewsFromFireStore.observe(viewLifecycleOwner, Observer { exclusiveNews ->
                            exclusiveNewsAdapter.differ.submitList(exclusiveNews)
                        })
                    }
                }
            }
        }
    }

    private fun checkIsSubscriptionExist() {
        subscriptionViewModel.checkUserHaveAnyActiveSubscription {
            when (it) {
                is UiState.Failure -> {
                    Toast.makeText(activity, "Couldn't complete request, please try again", Toast.LENGTH_SHORT).show()
                }
                UiState.Loading -> {}
                is UiState.Success -> {
                    if (it.data) {
                        viewModel.getExclusiveNews()
                        viewModel.exclusiveNewsFromFireStore.observe(viewLifecycleOwner, Observer { exclusiveNews ->
                            exclusiveNewsAdapter.differ.submitList(exclusiveNews)
                            binding.llNoSearchResult.visibility = View.GONE
                        })
                    } else {
                        startActivity(Intent(activity, NewsSubscriptionActivity::class.java))
                        binding.llNoSearchResult.visibility = View.VISIBLE

                    }
                }
            }
        }
    }

    private fun getExclusiveNewsToArticle(exclusiveNews: ExclusiveNews): Article {
        return Article(
            1,
            exclusiveNews.author,
            exclusiveNews.content,
            exclusiveNews.description,
            exclusiveNews.publishedAt,
            Source("", exclusiveNews.source!!),
            exclusiveNews.title,
            exclusiveNews.url,
            exclusiveNews.urlToImage
        )
    }

    private fun setupRecyclerView() {
        exclusiveNewsAdapter = ExclusiveNewsAdapter()
        binding.rvExclusiveNews.apply {
            adapter = exclusiveNewsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }
}