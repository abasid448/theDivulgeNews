package com.example.divulgenewsapp.models

import com.google.firebase.firestore.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
data class ExclusiveNews(
    var id: String? = null,
    var author: String? = null,
    var title: String? = null,
    var content: String? = null,
    var description: String? = null,
    var publishedAt: String? = null,
    var createdBy: String? = null,
    var activeStatus: Boolean? = null,
    var country: String? = null,
    var source: String? = null,
    var url: String? = null,
    var urlToImage: String? = null
) : Serializable
