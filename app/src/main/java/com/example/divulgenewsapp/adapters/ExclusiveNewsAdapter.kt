package com.example.divulgenewsapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.divulgenewsapp.databinding.ItemArticlePreviewBinding
import com.example.divulgenewsapp.models.ExclusiveNews

class ExclusiveNewsAdapter : RecyclerView.Adapter<ExclusiveNewsAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(val binding: ItemArticlePreviewBinding) : RecyclerView.ViewHolder(binding.root)

    private val differCallBack = object : DiffUtil.ItemCallback<ExclusiveNews>() {
        override fun areItemsTheSame(oldItem: ExclusiveNews, newItem: ExclusiveNews): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ExclusiveNews, newItem: ExclusiveNews): Boolean {
            return oldItem == newItem
        }
    }

    // Tool which takes our two lists and compare them and calculates the differences.
    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExclusiveNewsAdapter.ArticleViewHolder {
        return ArticleViewHolder(
            ItemArticlePreviewBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ExclusiveNewsAdapter.ArticleViewHolder, position: Int) {
        val article = differ.currentList[position]
        holder.binding.apply {
            // Load image from our article to image view.
            Glide.with(this.root).load(article.urlToImage).into(ivArticleImage)

            tvSource.text = article.source
            tvTitle.text = article.title
            tvDescription.text = article.description
            tvPublishedAt.text = article.publishedAt
            root.setOnClickListener {
                onItemClickListener?.let { it(article) }
            }
        }
    }

    private var onItemClickListener: ((ExclusiveNews) -> Unit)? = null

    fun setOnItemClickListener(listener: (ExclusiveNews) -> Unit) {
        onItemClickListener = listener
    }

}