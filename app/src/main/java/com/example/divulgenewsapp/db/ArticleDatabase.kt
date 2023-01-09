package com.example.divulgenewsapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.divulgenewsapp.models.Article

@Database(
    entities = [Article::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class ArticleDatabase : RoomDatabase() {

    // Room will implement this function behind the scene.
    abstract fun getArticleDao(): ArticleDao

    companion object {
        // Volatile - Other threads can immediately see when a thread changes this instance.
        @Volatile
        private var instance: ArticleDatabase? = null

        // For make sure that there is only a single instance of database at once.
        private val LOCK = Any()

        // This function will be called when the constructor is called.
        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            /* Everything that happens inside this block can't be accessed by other threads
            at the same time. So we will make sure that we don't set there is no another thread that sets
            this instance to something while we already set it. */
            instance ?: createDatabase(context).also {
                instance = it
            }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                ArticleDatabase::class.java,
                "article_db.db"
            ).build()
    }
}