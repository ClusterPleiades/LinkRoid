package com.speedroid.macroid

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import com.speedroid.macroid.Configs.Companion.DRAWABLE_POSITION_BUTTON_DUEL
import com.speedroid.macroid.Configs.Companion.DRAWABLE_POSITION_BUTTON_GATE
import com.speedroid.macroid.Configs.Companion.IMAGE_HEIGHT
import com.speedroid.macroid.Configs.Companion.IMAGE_STRIDE
import com.speedroid.macroid.Configs.Companion.IMAGE_WIDTH
import com.speedroid.macroid.macro.GateMacro
import com.speedroid.macroid.service.ProjectionService
import com.speedroid.macroid.ui.activity.SplashActivity.Companion.preservedContext
import kotlin.math.sqrt

class ImageController {
    private val deviceController: DeviceController = DeviceController(preservedContext)
    private val width = deviceController.getWidthMax()
    private val height = deviceController.getHeightMax()

    lateinit var villagePixelsArray: Array<DoubleArray?>

    // TODO update drawable resource ids
    private val villageDrawableResIdArray = arrayOf(
        R.drawable.image_button_gate,
        R.drawable.image_button_duel
    )

    init {
        initializeVillagePixelsArray()
    }

    private fun initializeVillagePixelsArray() {
        // initialize bitmap array
        val bitmapArray: Array<Bitmap?> = arrayOfNulls(villageDrawableResIdArray.size)
        for (i in bitmapArray.indices) {
            bitmapArray[i] = (ContextCompat.getDrawable(preservedContext, villageDrawableResIdArray[i]) as BitmapDrawable).bitmap
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
        villagePixelsArray = arrayOfNulls(pixelsArray.size)
        for (i in pixelsArray.indices)
            villagePixelsArray[i] = normalize(pixelsArray[i])

        // recycler bitmap array
        for (i in bitmapArray.indices) bitmapArray[i]!!.recycle()
    }

    private fun detectImage(): DetectedImage {
        // initialize screen bitmap
        var screenBitmap = ProjectionService.getScreenProjection()
        if (screenBitmap.width != width) screenBitmap = Bitmap.createScaledBitmap(screenBitmap, width, height, true)

        var maxSimilarity = 0.0
        var indexOfMax = 0
        var yOfMax = 0

        // move y
        var y = height - IMAGE_HEIGHT // start at bottom
        do {
            // crop screen bitmap
            val croppedBitmap = Bitmap.createBitmap(screenBitmap, 0, y, IMAGE_WIDTH, IMAGE_HEIGHT)
            val croppedPixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT)
            croppedBitmap.getPixels(croppedPixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)
            val normalizedCroppedPixels = normalize(croppedPixels)

            // compute similarity
            for (i in villagePixelsArray.indices) {
                val similarity = computeSimilarity(villagePixelsArray[i], normalizedCroppedPixels)
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity
                    indexOfMax = i
                    yOfMax = y
                }
            }

            // recycle cropped screen bitmap
            croppedBitmap.recycle()

            y -= IMAGE_STRIDE
        } while (y >= height / 2)

        // recycle bitmaps
        screenBitmap.recycle()

        // calculate duel button detect count
        if (indexOfMax == DRAWABLE_POSITION_BUTTON_DUEL) {
            GateMacro.duelButtonDetectCount++
            if (GateMacro.duelButtonDetectCount == 2) {
                GateMacro.isDuel = true
                GateMacro.duelButtonDetectCount = 0
            }
        }

        return DetectedImage(yOfMax, indexOfMax)
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

    fun findCoordinate(): Point {
        // initialize detected image
        val detectedImage = detectImage()

        // TODO update drawable resource coordinates
        val x = when (detectedImage.drawablePosition) {
            DRAWABLE_POSITION_BUTTON_GATE -> IMAGE_WIDTH / 5
            DRAWABLE_POSITION_BUTTON_DUEL -> IMAGE_WIDTH / 2
            else -> 0
        }

        return Point(x, detectedImage.y + IMAGE_HEIGHT / 2)
    }

//    private fun saveImage(bitmap: Bitmap) {
//        val fileOutputStream = preservedContext.openFileOutput("temp.png", Context.MODE_PRIVATE)
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
//        return
//    }

    private inner class DetectedImage(val y: Int, val drawablePosition: Int)
}