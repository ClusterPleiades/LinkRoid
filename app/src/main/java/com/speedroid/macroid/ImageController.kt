package com.speedroid.macroid

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.speedroid.macroid.Configs.Companion.BOTTOM_LEFT
import com.speedroid.macroid.Configs.Companion.BOTTOM_RIGHT
import com.speedroid.macroid.Configs.Companion.MOVEMENT
import com.speedroid.macroid.Configs.Companion.TOP_RIGHT
import com.speedroid.macroid.service.ProjectionService
import kotlin.math.sqrt

class ImageController(private val context: Context, private val width:Int, private val height:Int) {

    fun findImage(drawableResId: Int, from: Int) {
        // initialize bitmap
        val screenBitmap = ProjectionService.getScreenProjection().extractAlpha()
        // TODO resize?
        val targetBitmap = ContextCompat.getDrawable(context, drawableResId)!!.toBitmap().extractAlpha()

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
        targetBitmap.getPixels(targetPixels, 0, 1, 0, 0, tWidth, tHeight)

        // recycle target bitmap
        targetBitmap.recycle()

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
                croppedBitmap.getPixels(cropPixels, 0, 1, 0, 0, tWidth, tHeight)

                // recycle cropped bitmap
                croppedBitmap.recycle()

                // compute similarity
                val similarity = computeSimilarity(targetPixels, cropPixels)
                if (similarity >= 0.6) {
                    // TODO found
                }

                y += moveY
            } while ((y >= 0) && (y + tHeight <= sHeight))

            x += moveX
        } while ((x >= 0) && (x + tWidth <= sWidth))

    }

    fun computeSimilarity(targetPixelArray: IntArray, croppedPixelArray: IntArray): Double {
        var dotProduct = 0.0
        var normASum = 0.0
        var normBSum = 0.0

        for (i in targetPixelArray.indices) {
            dotProduct += targetPixelArray[i] * croppedPixelArray[i]
            normASum += targetPixelArray[i] * targetPixelArray[i]
            normBSum += croppedPixelArray[i] * croppedPixelArray[i]
        }

        val eucledianDistance = sqrt(normASum) * sqrt(normBSum)
        return dotProduct / eucledianDistance
    }
}