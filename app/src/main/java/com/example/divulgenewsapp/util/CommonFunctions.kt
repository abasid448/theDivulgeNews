package com.example.divulgenewsapp.util

import android.graphics.Bitmap

class CommonFunctions {
    companion object{
        fun resizeBitmap(source: Bitmap, maxLength: Int = 400): Bitmap {
            return try {
                if (source.height >= source.width) {
                    // if image already smaller than the required height.
                    if (source.height <= maxLength)
                        return source

                    val aspectRatio = source.width.toDouble() / source.height.toDouble()
                    val targetWidth = (maxLength * aspectRatio).toInt()
                    Bitmap.createScaledBitmap(source, targetWidth, maxLength, false)
                } else {
                    // if image already smaller than the required height.
                    if (source.width <= maxLength)
                        return source

                    val aspectRatio = source.height.toDouble() / source.width.toDouble()
                    val targetHeight = (maxLength * aspectRatio).toInt()
                    Bitmap.createScaledBitmap(source, maxLength, targetHeight, false)
                }
            } catch (e: Exception) {
                source
            }
        }
    }
}