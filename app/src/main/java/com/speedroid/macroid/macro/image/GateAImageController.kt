package com.speedroid.macroid.macro.image

import android.graphics.Bitmap
import android.graphics.Point
import com.speedroid.macroid.Configs.Companion.IMAGE_HEIGHT_HUGE
import com.speedroid.macroid.Configs.Companion.IMAGE_HEIGHT_SMALL
import com.speedroid.macroid.Configs.Companion.IMAGE_WIDTH
import com.speedroid.macroid.Configs.Companion.THRESHOLD_DISTANCE
import com.speedroid.macroid.R

class GateAImageController : BaseImageController() {
    private val bottomDrawableResIdArray = arrayOf(
        R.drawable.image_button_gate,
        R.drawable.image_button_back,
        R.drawable.image_background_conv
    )
    private val exceptionDrawableResIdArray = arrayOf(
        R.drawable.image_button_appear
    )

    private var bottomDrawablePixelsArray: Array<IntArray?> = arrayOfNulls(bottomDrawableResIdArray.size)
    private var exceptionDrawablePixelsArray: Array<IntArray?> = arrayOfNulls(exceptionDrawableResIdArray.size)

    init {
        for (i in bottomDrawableResIdArray.indices) bottomDrawablePixelsArray[i] = pixelsHashMap[bottomDrawableResIdArray[i]]
        for (i in exceptionDrawableResIdArray.indices) exceptionDrawablePixelsArray[i] = pixelsHashMap[exceptionDrawableResIdArray[i]]
    }

    fun detectImage(screenBitmap: Bitmap): DetectResult? {
        // detect
        val exceptionDetectResult = detectExceptionImage(screenBitmap)
        val bottomDetectResult = detectBottomImage(screenBitmap)

        return exceptionDetectResult ?: bottomDetectResult
    }

    fun detectBottomImage(screenBitmap: Bitmap): DetectResult? {
        // initialize y
        val y = screenHeight - IMAGE_HEIGHT_SMALL

        // initialize cropped pixel
        val croppedBitmap = Bitmap.createBitmap(screenBitmap, 0, y, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        val croppedPixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_SMALL)
        croppedBitmap.getPixels(croppedPixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        croppedBitmap.recycle()

        // detect
        var minDistance = Long.MAX_VALUE
        var indexOfMin = 0
        for (i in bottomDrawablePixelsArray.indices) {
            val distance = getDistanceAverage(bottomDrawablePixelsArray[i]!!, croppedPixels)
            if (distance != null)
                if (distance < minDistance) {
                    minDistance = distance
                    indexOfMin = i
                }
        }

        // check distance
        if (minDistance > THRESHOLD_DISTANCE)
            return null

        // initialize click point
        val clickPoint = when (bottomDrawableResIdArray[indexOfMin]) {
            R.drawable.image_button_gate -> Point(1080 / 16 * 3, IMAGE_HEIGHT_SMALL * 3 / 4 + y)
            R.drawable.image_button_back,
            R.drawable.image_background_conv -> Point(1080 / 2, IMAGE_HEIGHT_SMALL / 8 + y)
            else -> null
        }

        return DetectResult(bottomDrawableResIdArray[indexOfMin], clickPoint)
    }

    private fun detectExceptionImage(screenBitmap: Bitmap): DetectResult? {
        // initialize y
        val y: Int = (screenHeight - IMAGE_HEIGHT_HUGE) / 2

        // initialize cropped pixel
        val croppedBitmap = Bitmap.createBitmap(screenBitmap, 0, y, IMAGE_WIDTH, IMAGE_HEIGHT_HUGE)
        val croppedPixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_HUGE)
        croppedBitmap.getPixels(croppedPixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_HUGE)
        croppedBitmap.recycle()

        // detect
        var minDistance = Long.MAX_VALUE
        var indexOfMin = 0
        for (i in exceptionDrawablePixelsArray.indices) {
            val distance = getDistanceAverage(exceptionDrawablePixelsArray[i]!!, croppedPixels)
            if (distance != null)
                if (distance < minDistance) {
                    minDistance = distance
                    indexOfMin = i
                }
        }

        // check distance
        if (minDistance > THRESHOLD_DISTANCE)
            return null

        // initialize click point
        val clickPoint = when (exceptionDrawableResIdArray[indexOfMin]) {
            R.drawable.image_button_appear -> Point(1080 / 4, IMAGE_HEIGHT_HUGE * 9 / 10 + y)
            else -> null
        }

        return DetectResult(exceptionDrawableResIdArray[indexOfMin], clickPoint)
    }
}