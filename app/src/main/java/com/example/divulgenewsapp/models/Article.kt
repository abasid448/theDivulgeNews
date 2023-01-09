package com.example.divulgenewsapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

// This class is an entity (table) in room database.
@Entity(
    tableName = "articles"
)
data class Article(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val author: String? = null,
    val content: String? = null,
    val description: String? = null,
    val publishedAt: String? = null,
    val source: Source? = null,
    val title: String? = null,
    val url: String? = null,
    val urlToImage: String? = null,
    var uid: String? = null
) : Serializable