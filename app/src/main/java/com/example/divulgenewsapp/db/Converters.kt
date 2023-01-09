package com.example.divulgenewsapp.db

import androidx.room.TypeConverter
import com.example.divulgenewsapp.models.Source

class Converters {
    // Convert article source to room readable type.
    @TypeConverter
    fun fromSource(source: Source): String {
        return source.name!!
    }

    @TypeConverter
    fun toSource(name: String): Source {
        return Source(name, name)
    }
}