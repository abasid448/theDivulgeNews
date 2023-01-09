package com.example.divulgenewsapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.divulgenewsapp.R
import com.example.divulgenewsapp.databinding.FragmentExclusiveNewsViewBinding
import com.example.divulgenewsapp.ui.NewsActivity

class ExclusiveNewsViewFragment : Fragment(R.layout.fragment_exclusive_news_view) {
    private lateinit var binding: FragmentExclusiveNewsViewBinding
    private val args: ExclusiveNewsViewFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentExclusiveNewsViewBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val exclusiveNews = args.exclusiveNews

        binding.apply {
            tvTitle.text = exclusiveNews.title
            tvAuthor.text = exclusiveNews.author
            tvPublishedOn.text = exclusiveNews.publishedAt
            tvDescription.text = exclusiveNews.description
            tvContent.text = exclusiveNews.content
            Glide.with(activity as NewsActivity)
                .load(exclusiveNews.urlToImage)
                .into(ivImage)
        }
    }
}

