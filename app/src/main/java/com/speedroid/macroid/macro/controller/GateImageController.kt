package com.speedroid.macroid.macro.controller

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import com.speedroid.macroid.Configs.Companion.THRESHOLD_DISTANCE
import com.speedroid.macroid.Configs.Companion.IMAGE_HEIGHT
import com.speedroid.macroid.Configs.Companion.IMAGE_WIDTH
import com.speedroid.macroid.R
import com.speedroid.macroid.macro.result.DetectResult
import com.speedroid.macroid.service.ProjectionService
import com.speedroid.macroid.ui.activity.ModeActivity.Companion.preservedContext

class GateImageController : BaseImageController() {
    // TODO update
    private val centerDrawableResIdArray = arrayOf(
        R.drawable.image_button_retry,
    )
    private val bottomDrawableResIdArray = arrayOf(
        R.drawable.image_button_gate,
        R.drawable.image_button_back,
        R.drawable.image_button_duel_1,
        R.drawable.image_button_duel_2,
        R.drawable.image_background_dialog,
        R.drawable.image_button_confirm,
        R.drawable.image_button_next
    )

    private val bottomDrawablePixelsArray: Array<IntArray?> = arrayOfNulls(bottomDrawableResIdArray.size)
    private val centerDrawablePixelsArray: Array<IntArray?> = arrayOfNulls(centerDrawableResIdArray.size)
    private val winDrawablePixels: IntArray = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT)

    init {
        // initialize bitmap
        val centerBitmapArray: Array<Bitmap> = Array(centerDrawableResIdArray.size) { i ->
            (ContextCompat.getDrawable(preservedContext, centerDrawableResIdArray[i]) as BitmapDrawable).bitmap
        }
        val bottomBitmapArray: Array<Bitmap> = Array(bottomDrawableResIdArray.size) { i ->
            (ContextCompat.getDrawable(preservedContext, bottomDrawableResIdArray[i]) as BitmapDrawable).bitmap
        }
        val winBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_button_confirm) as BitmapDrawable).bitmap

        // initialize pixels
        var pixels: IntArray
        for (i in centerDrawablePixelsArray.indices) {
            pixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT)
            centerBitmapArray[i].getPixels(pixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)
            centerDrawablePixelsArray[i] = pixels
        }
        for (i in bottomDrawablePixelsArray.indices) {
            pixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT)
            bottomBitmapArray[i].getPixels(pixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)
            bottomDrawablePixelsArray[i] = pixels
        }
        winBitmap.getPixels(winDrawablePixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)

        // recycle bitmap array
        for (i in centerBitmapArray.indices) centerBitmapArray[i].recycle()
        for (i in bottomBitmapArray.indices) bottomBitmapArray[i].recycle()
        winBitmap.recycle()
    }

    fun detectImage(): DetectResult? {
        // detect image
        val centerDetectResult = detectCenterImage()
        val bottomDetectResult = detectBottomImage()

        val detectResult = if (centerDetectResult.distance < bottomDetectResult.distance) centerDetectResult else bottomDetectResult
        return if (detectResult.distance > THRESHOLD_DISTANCE) null else detectResult
    }

    fun detectWinImage(): DetectResult? {
        // initialize screen bitmap
        var screenBitmap = ProjectionService.getScreenProjection()
        if (screenBitmap.width != screenWidth) screenBitmap = Bitmap.createScaledBitmap(screenBitmap, screenWidth, screenHeight, true)

        // initialize y
        val y = screenHeight - IMAGE_HEIGHT

        // initialize cropped pixel
        val croppedBitmap = Bitmap.createBitmap(screenBitmap, 0, y, IMAGE_WIDTH, IMAGE_HEIGHT)
        val croppedPixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT)
        croppedBitmap.getPixels(croppedPixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)

        // detect
        val distance = computeDistanceAverage(winDrawablePixels, croppedPixels)

        // recycle bitmaps
        croppedBitmap.recycle()
        screenBitmap.recycle()

        // check threshold
        if (distance > THRESHOLD_DISTANCE)
            return null

        // initialize click point
        val clickPoint = Point(1080 / 2, y + IMAGE_HEIGHT / 2 + IMAGE_HEIGHT * 2 / 5)

        return DetectResult(R.drawable.image_button_confirm, clickPoint, distance)
    }

    fun detectCenterImage(): DetectResult {
        // initialize screen bitmap
        var screenBitmap = ProjectionService.getScreenProjection()
        if (screenBitmap.width != screenWidth) screenBitmap = Bitmap.createScaledBitmap(screenBitmap, screenWidth, screenHeight, true)

        // initialize y
        val y: Int = (screenHeight - IMAGE_HEIGHT) / 2

        // initialize cropped pixel
        val croppedBitmap = Bitmap.createBitmap(screenBitmap, 0, y, IMAGE_WIDTH, IMAGE_HEIGHT)
        val croppedPixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT)
        croppedBitmap.getPixels(croppedPixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)

        // detect
        var minDistance = Long.MAX_VALUE
        var indexOfMin = 0
        for (i in centerDrawablePixelsArray.indices) {
            val distance = computeDistanceAverage(centerDrawablePixelsArray[i]!!, croppedPixels)
            if (distance < minDistance) {
                minDistance = distance
                indexOfMin = i
            }
        }

        // recycle bitmaps
        croppedBitmap.recycle()
        screenBitmap.recycle()

        // TODO update
        // initialize click point
        val clickPoint = when (centerDrawableResIdArray[indexOfMin]) {
            R.drawable.image_button_retry -> Point(1080 * 3 / 4, IMAGE_HEIGHT * 9 / 10 + y)
            else -> null
        }

        return DetectResult(centerDrawableResIdArray[indexOfMin], clickPoint, minDistance)
    }

    private fun detectBottomImage(): DetectResult {
        // initialize screen bitmap
        var screenBitmap = ProjectionService.getScreenProjection()
        if (screenBitmap.width != screenWidth) screenBitmap = Bitmap.createScaledBitmap(screenBitmap, screenWidth, screenHeight, true)

        // initialize y
        val y = screenHeight - IMAGE_HEIGHT

        // initialize cropped pixel
        val croppedBitmap = Bitmap.createBitmap(screenBitmap, 0, y, IMAGE_WIDTH, IMAGE_HEIGHT)
        val croppedPixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT)
        croppedBitmap.getPixels(croppedPixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)

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

        // recycle bitmaps
        croppedBitmap.recycle()
        screenBitmap.recycle()

        // TODO update
        // initialize click point
        val clickPoint = when (bottomDrawableResIdArray[indexOfMin]) {
            R.drawable.image_button_gate -> Point(1080 / 16 * 3, IMAGE_HEIGHT * 3 / 4 + y)
            R.drawable.image_button_back,
            R.drawable.image_button_duel_1,
            R.drawable.image_button_duel_2,
            R.drawable.image_background_dialog -> Point(1080 / 2, IMAGE_HEIGHT / 8 + y)
            else -> null
        }

        return DetectResult(bottomDrawableResIdArray[indexOfMin], clickPoint, minDistance)
    }
}