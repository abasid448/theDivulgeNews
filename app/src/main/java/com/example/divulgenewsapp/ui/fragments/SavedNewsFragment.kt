package com.example.divulgenewsapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.divulgenewsapp.R
import com.example.divulgenewsapp.adapters.NewsAdapter
import com.example.divulgenewsapp.databinding.FragmentSavedNewsBinding
import com.example.divulgenewsapp.ui.NewsActivity
import com.example.divulgenewsapp.ui.viewmodel.NewsViewModel
import com.example.divulgenewsapp.util.UiState
import com.google.android.material.snackbar.Snackbar

class SavedNewsFragment : Fragment(R.layout.fragment_saved_news) {

    private lateinit var viewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var binding: FragmentSavedNewsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSavedNewsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel
        // Setup saved news recycler view.
        setupRecyclerView()

        // Set on item click listener for recycler view.
        newsAdapter.setOnItemClickListener {
            // Get the clicked the article and put into a bundle and attach the bundle to a navigation component.
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            // Fragment transition.
            findNavController().navigate(
                R.id.action_savedNewsFragment_to_articleFragment,
                bundle
            )
        }

        // Initialize call back for item touch helper to swipe and delete article.
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Get the position of clicked article.
                val position = viewHolder.adapterPosition
                // Get the clicked article from list differ in adapter.
                val article = newsAdapter.differ.currentList[position]
                // Delete article from database.
                viewModel.deleteArticle(article)
                // Setup undo function for delete article.
                Snackbar.make(view, "Successfully deleted article", Snackbar.LENGTH_LONG)
                    .apply {
                        setAction("Undo") {
                            viewModel.saveArticle(article)
                            viewModel.getSavedArticlesFromFireStore()
                        }
                        show()
                    }
            }
        }

        // Attach item call back to the recycler view.
        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.rvSavedNews)
        }

        // Make an observer for getting and observe on the saved news data.
        viewModel.getSavedArticlesFromFireStore()
        viewModel.savedArticlesFromFireStore.observe(viewLifecycleOwner, Observer { articles ->
            newsAdapter.differ.submitList(articles)
            if (articles.isNotEmpty()) {
                binding.rvSavedNews.visibility = View.VISIBLE
                binding.llNoSearchResult.visibility = View.GONE
            } else {
                binding.rvSavedNews.visibility = View.GONE
                binding.llNoSearchResult.visibility = View.VISIBLE
            }
        })

        viewModel.deleteArticle.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Loading
                }
                is UiState.Failure -> {
                    Toast.makeText(
                        activity,
                        "Something went wrong, article not deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is UiState.Success -> {
                    viewModel.getSavedArticlesFromFireStore()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.rvSavedNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }
}