package com.speedroid.macroid

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import com.speedroid.macroid.Configs.Companion.BOTTOM_LEFT
import com.speedroid.macroid.Configs.Companion.BOTTOM_RIGHT
import com.speedroid.macroid.Configs.Companion.MOVEMENT
import com.speedroid.macroid.Configs.Companion.TOP_RIGHT
import com.speedroid.macroid.service.ProjectionService
import kotlin.math.abs
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

        // initialize target pixels
        val targetPixels = IntArray(tWidth * tHeight)
        targetBitmap.getPixels(targetPixels, 0, tWidth, 0, 0, tWidth, tHeight)

        // move x
        var x = startX
        do {
            // move y
            var y = startY
            do {
                // crop image
                val croppedBitmap = Bitmap.createBitmap(screenBitmap, x, y, tWidth, tHeight)

                // initialize crop pixels
                val cropPixels = IntArray(tWidth * tHeight)
                croppedBitmap.getPixels(cropPixels, 0, tWidth, 0, 0, tWidth, tHeight)

                // compute similarity
                val similarity = computeSimilarity(targetPixels, cropPixels)
                if (similarity >= 0.6) {
                    // TODO found

                    // recycle bitmap
                    screenBitmap.recycle()
                    targetBitmap.recycle()
                    croppedBitmap.recycle()
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

    private fun computeSimilarity(targetPixelArray: IntArray, croppedPixelArray: IntArray): Double {
        // normalize pixel Arrays
        val normalizedTargetPixelArray = normalize(targetPixelArray)
        val normalizedCroppedPixelArray = normalize(croppedPixelArray)

        // compute similarity
        var dotProduct = 0.0
        var targetSum = 0.0
        var croppedSum = 0.0

        for (i in normalizedTargetPixelArray.indices) {
            dotProduct += normalizedTargetPixelArray[i] * normalizedCroppedPixelArray[i]
            targetSum += normalizedTargetPixelArray[i] * normalizedTargetPixelArray[i]
            croppedSum += normalizedCroppedPixelArray[i] * normalizedCroppedPixelArray[i]
        }

        return dotProduct / (sqrt(targetSum) * sqrt(croppedSum))
    }

    private fun normalize(intArray: IntArray): IntArray {
        val normalizedIntArray = IntArray(intArray.size)

        var max = 0.0
        for (i in intArray.indices) {
            val absolute = abs(intArray[i])
            if (absolute > max) max = absolute.toDouble()
        }

        val scale = 32677.0 / max
        for (i in intArray.indices) normalizedIntArray[i] = (intArray[i].toDouble() * scale).toInt()

        return normalizedIntArray
    }
}