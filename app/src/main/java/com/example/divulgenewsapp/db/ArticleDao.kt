package com.example.divulgenewsapp.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.divulgenewsapp.models.Article

@Dao
interface ArticleDao {
    /* Insert article to database with on conflict strategy.
    On conflict strategy is for defining what happens the
    article we try to save in our database already exists.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(article: Article): Long

    /* Select every saved articles from db.
    Not using suspend function because we return live-data object
    which will not work with suspend function.
     */
    @Query("SELECT * FROM articles")
    fun getAllArticles(): LiveData<List<Article>>

    @Query("SELECT * FROM articles WHERE url =:url")
    fun getArticle(url: String): LiveData<Article>

    // Delete an article in database.
    @Delete
    suspend fun deleteArticle(article: Article)

    // Check the article already exists.
    @Query(
        "SELECT COUNT(id) from articles WHERE url = :url"
    )
    suspend fun isArticleExist(
        url: String
    ): Int
}