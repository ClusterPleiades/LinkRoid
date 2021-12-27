package com.speedroid.macroid

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.core.content.ContextCompat
import com.speedroid.macroid.Configs.Companion.BOTTOM_LEFT
import com.speedroid.macroid.Configs.Companion.BOTTOM_RIGHT
import com.speedroid.macroid.Configs.Companion.SIMILARITY_THRESHOLD
import com.speedroid.macroid.Configs.Companion.STRIDE
import com.speedroid.macroid.Configs.Companion.TOP_LEFT
import com.speedroid.macroid.Configs.Companion.TOP_RIGHT
import com.speedroid.macroid.ui.activity.SplashActivity.Companion.preservedContext
import kotlin.math.abs

class ImageController {
    // TODO update
    fun detect(screenBitmap: Bitmap, drawableResId: Int): Point? {
        // initialize arguments
        val start =
            when (drawableResId) {
                R.drawable.image_retry -> BOTTOM_LEFT
                else -> -1
            }
        val movable =
            when (drawableResId) {
                R.drawable.image_retry -> true
                else -> false
            }

        // detect image
        return detectImage(screenBitmap, drawableResId, start, movable)
    }

    private fun detectImage(screenBitmap: Bitmap, drawableResId: Int, start: Int, movable: Boolean): Point? {
        // initialize drawable pixels
        val drawableBitmap = (ContextCompat.getDrawable(preservedContext, drawableResId) as BitmapDrawable).bitmap
        val drawableWidth = drawableBitmap.width // must be even number
        val drawableHeight = drawableBitmap.height
        val drawablePixels = IntArray(drawableWidth * drawableHeight)
        drawableBitmap.getPixels(drawablePixels, 0, drawableWidth, 0, 0, drawableWidth, drawableHeight)
        drawableBitmap.recycle()

        val screenWidth = screenBitmap.width
        val screenHeight = screenBitmap.height

        // initialize start coordinate
        val x = when (start) {
            TOP_LEFT, BOTTOM_LEFT -> 0
            TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - drawableWidth
            else -> -1
        }
        var y: Int
        val move: Int
        when (start) {
            TOP_LEFT, TOP_RIGHT -> {
                y = 0
                move = STRIDE
            }
            BOTTOM_LEFT, BOTTOM_RIGHT -> {
                y = screenHeight - drawableHeight
                move = -STRIDE
            }
            else -> {
                y = -1
                move = -1
            }
        }

        var croppedBitmap: Bitmap
        var croppedPixels: IntArray

        // detect
        if (movable) {
            do {
                croppedBitmap = Bitmap.createBitmap(screenBitmap, x, y, drawableWidth, drawableHeight)
                croppedPixels = IntArray(drawableWidth * drawableHeight)
                croppedBitmap.getPixels(croppedPixels, 0, drawableWidth, 0, 0, drawableWidth, drawableHeight)
                val similarity = computeSimilarity(croppedPixels, drawablePixels)

                Log.d("test", "similarity $similarity")
                if (similarity >= SIMILARITY_THRESHOLD) {
//                    return calculateCoordinate(drawableResId, drawableWidth, drawableHeight, x, y)
                }
                croppedBitmap.recycle()

                y += move
            } while (y >= 0 && y <= (screenHeight - drawableHeight))
        } else {
            croppedBitmap = Bitmap.createBitmap(screenBitmap, x, y, drawableWidth, drawableHeight)
            croppedPixels = IntArray(drawableWidth * drawableHeight)
            croppedBitmap.getPixels(croppedPixels, 0, drawableWidth, 0, 0, drawableWidth, drawableHeight)
            val similarity = computeSimilarity(croppedPixels, drawablePixels)
            if (similarity >= SIMILARITY_THRESHOLD) {
                return calculateCoordinate(drawableResId, drawableWidth, drawableHeight, x, y)
            }
            croppedBitmap.recycle()
        }

        // case not found
        return null
    }

    private fun computeSimilarity(intArray1: IntArray, intArray2: IntArray): Double {
        var distance = 0L

        for (i in intArray1.indices) {
            val difference = abs(intArray1[i] - intArray2[i]).toLong()
            if (distance > Long.MAX_VALUE - difference)
                return 0.0
            else
                distance += difference
        }

        return 10000000.0 / Long.MAX_VALUE * (Long.MAX_VALUE - distance.toDouble()) - 9999999
    }

    // TODO update
    private fun calculateCoordinate(drawableResId: Int, drawableWidth: Int, drawableHeight: Int, x: Int, y: Int): Point? {
        val point = when (drawableResId) {
            R.drawable.image_retry -> Point(drawableWidth * 3 / 4, y + drawableHeight / 2)
            else -> null
        }
        return point
    }
}