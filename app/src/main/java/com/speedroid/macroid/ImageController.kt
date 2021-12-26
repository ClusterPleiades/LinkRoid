package com.speedroid.macroid

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import com.speedroid.macroid.Configs.Companion.DRAWABLE_POSITION_GATE
import com.speedroid.macroid.Configs.Companion.IMAGE_HEIGHT
import com.speedroid.macroid.Configs.Companion.IMAGE_STRIDE
import com.speedroid.macroid.Configs.Companion.IMAGE_WIDTH
import com.speedroid.macroid.Configs.Companion.SIMILARITY_THRESHOLD
import com.speedroid.macroid.service.ProjectionService
import com.speedroid.macroid.ui.activity.SplashActivity.Companion.preservedContext
import kotlin.math.sqrt

class ImageController {
    private val deviceController: DeviceController = DeviceController(preservedContext)
    private val width = deviceController.getWidthMax()
    private val height = deviceController.getHeightMax()

    private var pixelsArray: Array<DoubleArray?>

    init {
        // TODO update drawable resource ids
        val drawableResIdArrayList = ArrayList<Int>()
        drawableResIdArrayList.add(R.drawable.image_gate)

        // initialize bitmap array
        val bitmapArray: Array<Bitmap?> = arrayOfNulls(drawableResIdArrayList.size)
        for (i in bitmapArray.indices) {
            bitmapArray[i] = (ContextCompat.getDrawable(preservedContext, drawableResIdArrayList[i]) as BitmapDrawable).bitmap
        }

        // initialize pixels array
        val pixelsArray: Array<IntArray?> = arrayOfNulls(bitmapArray.size)
        var pixels: IntArray
        for (i in pixelsArray.indices) {
            pixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT)
            bitmapArray[i]!!.getPixels(pixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)
            pixelsArray[i] = pixels
        }

        // normalize pixels array
        this.pixelsArray = arrayOfNulls(pixelsArray.size)
        for (i in pixelsArray.indices) {
            this.pixelsArray[i] = normalize(pixelsArray[i])
        }

        // recycler bitmap array
        for (i in bitmapArray.indices) bitmapArray[i]!!.recycle()
    }

    fun findCoordinate(): Point? {
        // initialize image info
        val detectedImage = detectImage()

        // return coordinate
        return if (detectedImage == null)
            null
        // TODO update drawable resource coordinates
        else {
            // initialize x
            val x = when (detectedImage.drawablePosition) {
                DRAWABLE_POSITION_GATE -> IMAGE_WIDTH / 5
                else -> 0
            }

            // initialize y
            val y = detectedImage.y + IMAGE_HEIGHT / 2

            return Point(x, y)
        }
    }

    private fun detectImage(): DetectedImage? {
        // initialize screen bitmap
        var screenBitmap = ProjectionService.getScreenProjection()
        if (screenBitmap.width != width) screenBitmap = Bitmap.createScaledBitmap(screenBitmap, width, height, true)

        // move y
        var y = height - IMAGE_HEIGHT // start at bottom
        do {
            // crop screen bitmap
            val croppedBitmap = Bitmap.createBitmap(screenBitmap, 0, y, IMAGE_WIDTH, IMAGE_HEIGHT)
            val croppedPixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT)
            croppedBitmap.getPixels(croppedPixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)
            val normalizedCroppedPixels = normalize(croppedPixels)

            // compute similarity
            for (i in pixelsArray.indices) {
                val similarity = computeSimilarity(pixelsArray[i], normalizedCroppedPixels)
                if (similarity > SIMILARITY_THRESHOLD) {
//                    // recycle bitmaps
//                    screenBitmap.recycle()
//                    croppedBitmap.recycle()
//
//                    return DetectedImage(y, i)
                }
            }

            // recycle cropped screen bitmap
            croppedBitmap.recycle()

            y -= IMAGE_STRIDE
        } while (y >= 0)

        // case not found
        return null
    }

    private fun normalize(pixelArray: IntArray?): DoubleArray {
        val min = pixelArray!!.minOrNull()!!.toDouble()
        val max = pixelArray.maxOrNull()!!.toDouble()
        val normalizedArray = DoubleArray(pixelArray.size)
        for (i in normalizedArray.indices) normalizedArray[i] = 1 - ((pixelArray[i] - min) / (max - min))
        return normalizedArray
    }

    private fun computeSimilarity(normalizedIntArrayA: DoubleArray?, normalizedIntArrayB: DoubleArray): Double {
        var dotProduct = 0.0
        var sumA = 0.0
        var sumB = 0.0

        for (i in normalizedIntArrayA!!.indices) {
            dotProduct += normalizedIntArrayA[i] * normalizedIntArrayB[i]
            sumA += normalizedIntArrayA[i] * normalizedIntArrayA[i]
            sumB += normalizedIntArrayB[i] * normalizedIntArrayB[i]
        }

        return dotProduct / (sqrt(sumA) * sqrt(sumB))
    }

//    private fun saveImage(bitmap: Bitmap) {
//        val fileOutputStream = preservedContext.openFileOutput("temp.png", Context.MODE_PRIVATE)
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
//        return
//    }

    private inner class DetectedImage(val y: Int, val drawablePosition: Int)
}