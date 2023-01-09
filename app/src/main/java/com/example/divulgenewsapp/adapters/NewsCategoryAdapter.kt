package com.example.divulgenewsapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.divulgenewsapp.databinding.ItemNewsCategoryPreviewBinding
import com.example.divulgenewsapp.models.NewsCategory

class NewsCategoryAdapter : RecyclerView.Adapter<NewsCategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(val binding: ItemNewsCategoryPreviewBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val differCallBack = object : DiffUtil.ItemCallback<NewsCategory>() {
        override fun areItemsTheSame(oldItem: NewsCategory, newItem: NewsCategory): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: NewsCategory, newItem: NewsCategory): Boolean {
            return oldItem == newItem
        }
    }

    // Tool which takes our two lists and compare them and calculates the differences.
    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            ItemNewsCategoryPreviewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = differ.currentList[position]

        holder.binding.apply {
            tvCategoryTitle.text = category.title
            root.setOnClickListener {
                onItemClickListener?.let { it(category) }
            }
        }
    }

    private var onItemClickListener: ((NewsCategory) -> Unit)? = null

    fun setOnItemClickListener(listener: (NewsCategory) -> Unit) {
        onItemClickListener = listener
    }
}