package com.speedroid.macroid.macro.image

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import com.speedroid.macroid.Configs.Companion.THRESHOLD_DISTANCE
import com.speedroid.macroid.Configs.Companion.IMAGE_HEIGHT
import com.speedroid.macroid.Configs.Companion.IMAGE_HEIGHT_LARGE
import com.speedroid.macroid.Configs.Companion.IMAGE_WIDTH
import com.speedroid.macroid.R
import com.speedroid.macroid.macro.DetectResult
import com.speedroid.macroid.ui.activity.ModeActivity.Companion.preservedContext

class GateImageController : BaseImageController() {
    // TODO update
    private val bottomDrawableResIdArray = arrayOf(
        R.drawable.image_button_gate,
        R.drawable.image_button_back,
        R.drawable.image_background_dialog
    )
    private val exceptionDrawableResIdArray = arrayOf(
        R.drawable.image_button_appear
    )

    private val bottomDrawablePixelsArray: Array<IntArray?> = arrayOfNulls(bottomDrawableResIdArray.size)
    private val exceptionDrawablePixelsArray: Array<IntArray?> = arrayOfNulls(exceptionDrawableResIdArray.size)

    init {
        // initialize bitmap
        val bottomBitmapArray: Array<Bitmap> = Array(bottomDrawableResIdArray.size) { i ->
            (ContextCompat.getDrawable(preservedContext, bottomDrawableResIdArray[i]) as BitmapDrawable).bitmap
        }
        val exceptionBitmapArray: Array<Bitmap> = Array(exceptionDrawableResIdArray.size) { i ->
            (ContextCompat.getDrawable(preservedContext, exceptionDrawableResIdArray[i]) as BitmapDrawable).bitmap
        }

        // initialize pixels
        var pixels: IntArray
        for (i in bottomDrawablePixelsArray.indices) {
            pixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT)
            bottomBitmapArray[i].getPixels(pixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)
            bottomDrawablePixelsArray[i] = pixels
        }
        for (i in exceptionDrawablePixelsArray.indices) {
            pixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_LARGE)
            exceptionBitmapArray[i].getPixels(pixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_LARGE)
            exceptionDrawablePixelsArray[i] = pixels
        }

        // recycle bitmap array
        for (i in bottomBitmapArray.indices) bottomBitmapArray[i].recycle()
        for (i in exceptionBitmapArray.indices) exceptionBitmapArray[i].recycle()
    }

    fun detectImage(screenBitmap: Bitmap): DetectResult? {
        // detect
        val exceptionDetectResult = detectExceptionImage(screenBitmap)
        val bottomDetectResult = detectBottomImage(screenBitmap)

        return exceptionDetectResult ?: bottomDetectResult
    }

    fun detectBottomImage(screenBitmap: Bitmap): DetectResult? {
        // initialize y
        val y = screenHeight - IMAGE_HEIGHT

        // initialize cropped pixel
        val croppedBitmap = Bitmap.createBitmap(screenBitmap, 0, y, IMAGE_WIDTH, IMAGE_HEIGHT)
        val croppedPixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT)
        croppedBitmap.getPixels(croppedPixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)
        croppedBitmap.recycle()

        // detect
        var minDistance = Long.MAX_VALUE
        var indexOfMin = 0
        for (i in bottomDrawablePixelsArray.indices) {
            val distance = computeDistanceAverage(bottomDrawablePixelsArray[i]!!, croppedPixels)
            if (distance < minDistance) {
                minDistance = distance
                indexOfMin = i
            }
        }

        // check distance
        if (minDistance > THRESHOLD_DISTANCE)
            return null

        // TODO update
        // initialize click point
        val clickPoint = when (bottomDrawableResIdArray[indexOfMin]) {
            R.drawable.image_button_gate -> Point(1080 / 16 * 3, IMAGE_HEIGHT * 3 / 4 + y)
            R.drawable.image_button_back,
            R.drawable.image_background_dialog -> Point(1080 / 2, IMAGE_HEIGHT / 8 + y)
            else -> null
        }

        return DetectResult(bottomDrawableResIdArray[indexOfMin], clickPoint, minDistance)
    }

    private fun detectExceptionImage(screenBitmap: Bitmap): DetectResult? {
        // initialize y
        val y: Int = (screenHeight - IMAGE_HEIGHT_LARGE) / 2

        // initialize cropped pixel
        val croppedBitmap = Bitmap.createBitmap(screenBitmap, 0, y, IMAGE_WIDTH, IMAGE_HEIGHT_LARGE)
        val croppedPixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_LARGE)
        croppedBitmap.getPixels(croppedPixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_LARGE)
        croppedBitmap.recycle()

        // detect
        var minDistance = Long.MAX_VALUE
        var indexOfMin = 0
        for (i in exceptionDrawablePixelsArray.indices) {
            val distance = computeDistanceAverage(exceptionDrawablePixelsArray[i]!!, croppedPixels)
            if (distance < minDistance) {
                minDistance = distance
                indexOfMin = i
            }
        }

        // check distance
        if (minDistance > THRESHOLD_DISTANCE)
            return null

        // TODO update
        // initialize click point
        val clickPoint = when (exceptionDrawableResIdArray[indexOfMin]) {
            R.drawable.image_button_appear -> Point(1080 / 4, IMAGE_HEIGHT_LARGE * 9 / 10 + y)
            else -> null
        }

        return DetectResult(exceptionDrawableResIdArray[indexOfMin], clickPoint, minDistance)
    }
}