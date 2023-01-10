package com.example.divulgenewsapp.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.divulgenewsapp.R
import com.example.divulgenewsapp.adapters.NewsAdapter
import com.example.divulgenewsapp.adapters.NewsCategoryAdapter
import com.example.divulgenewsapp.databinding.FragmentBreakingNewsBinding
import com.example.divulgenewsapp.ui.NewsActivity
import com.example.divulgenewsapp.ui.ProfileAndSettingsActivity
import com.example.divulgenewsapp.ui.viewmodel.NewsViewModel
import com.example.divulgenewsapp.util.Constants
import com.example.divulgenewsapp.util.Constants.Companion.QUERY_PAGE_SIZE
import com.example.divulgenewsapp.util.Constants.Companion.aboutusURL
import com.example.divulgenewsapp.util.Resource

class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news) {
    private lateinit var binding: FragmentBreakingNewsBinding
    private lateinit var viewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var newsCategoryAdapter: NewsCategoryAdapter

    private val TAG = "BreakingNewsFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBreakingNewsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel

        //contact us

        binding.tvaboutus.setOnClickListener {
            val intent =  Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse(aboutusURL))
            startActivity(intent)
        }

        // Logout button click.
        binding.btnLogout.setOnClickListener {
            // Show an alert box.
            val intent = Intent(activity, ProfileAndSettingsActivity::class.java)
            startActivity(intent)
        }

        // Setup breaking news recycler view.
        setupRecyclerView()
        setupNewsCategoryRecyclerView()

        // Set on item click listener for recycler view.
        newsAdapter.setOnItemClickListener {
            // Get the clicked the article and put into a bundle and attach the bundle to a navigation component.
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }

            // Check if article exists.
            viewModel.isArticleExist(it)
            // Fragment transition.
            findNavController().navigate(
                R.id.action_breakingNewsFragment_to_articleFragment, bundle
            )
        }

        // Set OnClick listener for news category recycler view.
        newsCategoryAdapter.setOnItemClickListener {
            NewsViewModel.newsCategory = it.value
            viewModel.getBreakingNews(com.example.divulgenewsapp.Session.user.countryCode, NewsViewModel.newsCategory, false)
        }

//        Call breaking news live data and to observe on that.
        viewModel.breakingNews.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        // Notify scroll listener about the last page, if it should paginate further or not.
                        val totalPages = newsResponse.totalResults / QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.breakingNewsPage == totalPages
                        if (isLastPage) {
                            binding.rvBreakingNews.setPadding(0, 0, 0, 0)
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(activity, "An  error occurred: $message", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })
    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            // Check if recycler view is scrolling.
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            // Whether we scroll until the last or not.
            // Calculate with recycler view layout manager.
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotAtLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate =
                isNotLoadingAndNotAtLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                viewModel.getBreakingNews(com.example.divulgenewsapp.Session.user.countryCode, NewsViewModel.newsCategory, true)
                isScrolling = false
            }
        }
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.rvBreakingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@BreakingNewsFragment.scrollListener)
        }
    }

    private fun setupNewsCategoryRecyclerView() {
        newsCategoryAdapter = NewsCategoryAdapter()
        binding.rvCategories.apply {
            adapter = newsCategoryAdapter
            layoutManager = StaggeredGridLayoutManager(1, RecyclerView.HORIZONTAL)
        }
        newsCategoryAdapter.differ.submitList(Constants.Companion.NEWS_CATEGORIES)
    }

}