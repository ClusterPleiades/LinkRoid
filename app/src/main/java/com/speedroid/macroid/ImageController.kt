package com.speedroid.macroid

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.core.content.ContextCompat
import com.speedroid.macroid.Configs.Companion.BOTTOM_LEFT
import com.speedroid.macroid.Configs.Companion.BOTTOM_RIGHT
import com.speedroid.macroid.Configs.Companion.MOVEMENT
import com.speedroid.macroid.Configs.Companion.SIMILARITY_THRESHOLD
import com.speedroid.macroid.Configs.Companion.TOP_RIGHT
import com.speedroid.macroid.service.ProjectionService
import kotlin.math.sqrt

class ImageController(private val context: Context, private val width: Int, private val height: Int) {

    fun findImage(drawableResId: Int, from: Int) {
        // initialize screen bitmap
        var screenBitmap = ProjectionService.getScreenProjection()
        if (screenBitmap.width != width) screenBitmap = Bitmap.createScaledBitmap(screenBitmap, width, height, true)

        // initialize target bitmap
        val targetBitmap = (ContextCompat.getDrawable(context, drawableResId) as BitmapDrawable).bitmap

        // initialize width, height
        val sWidth = screenBitmap.width
        val sHeight = screenBitmap.height
        val tWidth = targetBitmap.width
        val tHeight = targetBitmap.height

        // initialize coordinate, movement
        var startX = 0
        var startY = 0
        var moveX = MOVEMENT
        var moveY = MOVEMENT

        when (from) {
            TOP_RIGHT -> {
                startX = sWidth - tWidth
                moveX *= -1
            }
            BOTTOM_LEFT -> {
                startY = sHeight - tHeight
                moveY *= -1
            }
            BOTTOM_RIGHT -> {
                startX = sWidth - tWidth
                startY = sHeight - tHeight
                moveX *= -1
                moveY *= -1
            }
        }

        // initialize normalized target pixels
        val targetPixels = IntArray(tWidth * tHeight)
        targetBitmap.getPixels(targetPixels, 0, tWidth, 0, 0, tWidth, tHeight)
        val normalizedTargetPixels = normalize(targetPixels)

        // move x
        var x = startX
        var maxSimilarity = 0.0
        do {
            // move y
            var y = startY
            do {
                // crop image
                val croppedBitmap = Bitmap.createBitmap(screenBitmap, x, y, tWidth, tHeight)

                // initialize normalized cropped pixels
                val croppedPixels = IntArray(tWidth * tHeight)
                croppedBitmap.getPixels(croppedPixels, 0, tWidth, 0, 0, tWidth, tHeight)
                val normalizedCroppedPixels = normalize(croppedPixels)

                // compute similarity
                val similarity = computeSimilarity(normalizedTargetPixels, normalizedCroppedPixels)
                if (similarity > maxSimilarity) {
                    // TODO
                    Log.d("similarity", similarity.toString())
                    

                    // recycle bitmap
//                    screenBitmap.recycle()
//                    targetBitmap.recycle()
//                    croppedBitmap.recycle()
                }

                // recycle cropped bitmap
                croppedBitmap.recycle()

                y += moveY

            } while ((y >= 0) && (y + tHeight <= sHeight))

            x += moveX
        } while ((x >= 0) && (x + tWidth <= sWidth))
    }

    private fun saveImage(bitmap: Bitmap) {
        val fileOutputStream = context.openFileOutput("temp.png", Context.MODE_PRIVATE)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        return
    }

    private fun normalize(pixelArray: IntArray): DoubleArray {
        val min = pixelArray.minOrNull()!!.toDouble()
        val max = pixelArray.maxOrNull()!!.toDouble()
        val normalizedArray = DoubleArray(pixelArray.size)
        for (i in normalizedArray.indices) normalizedArray[i] = 1 - ((pixelArray[i] - min) / (max - min))
        return normalizedArray
    }

    private fun computeSimilarity(normalizedIntArrayA: DoubleArray, normalizedIntArrayB: DoubleArray): Double {
        var dotProduct = 0.0
        var sumA = 0.0
        var sumB = 0.0

        for (i in normalizedIntArrayA.indices) {
            dotProduct += normalizedIntArrayA[i] * normalizedIntArrayB[i]
            sumA += normalizedIntArrayA[i] * normalizedIntArrayA[i]
            sumB += normalizedIntArrayB[i] * normalizedIntArrayB[i]
        }

        return dotProduct / (sqrt(sumA) * sqrt(sumB))
    }
}