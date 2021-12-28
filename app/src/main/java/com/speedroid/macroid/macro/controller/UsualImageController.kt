package com.speedroid.macroid.macro.controller

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import com.speedroid.macroid.Configs.Companion.DISTANCE_THRESHOLD
import com.speedroid.macroid.Configs.Companion.IMAGE_HEIGHT
import com.speedroid.macroid.Configs.Companion.IMAGE_WIDTH
import com.speedroid.macroid.DeviceController
import com.speedroid.macroid.R
import com.speedroid.macroid.macro.DetectResult
import com.speedroid.macroid.service.ProjectionService
import com.speedroid.macroid.ui.activity.SplashActivity.Companion.preservedContext
import kotlin.math.abs

class UsualImageController {
    private val deviceController: DeviceController = DeviceController(preservedContext)
    private val screenWidth = deviceController.getWidthMax()
    private val screenHeight = deviceController.getHeightMax()

    // TODO update
    private val centerDrawableResIdArray = arrayOf(
        R.drawable.image_retry
    )
    private val bottomDrawableResIdArray = arrayOf(
        R.drawable.image_gate,
        R.drawable.image_duel,
        R.drawable.image_duel_2,
        R.drawable.image_dialog
    )

    private val bottomDrawablePixelsArray: Array<IntArray?> = arrayOfNulls(bottomDrawableResIdArray.size)
    private val centerDrawablePixelsArray: Array<IntArray?> = arrayOfNulls(centerDrawableResIdArray.size)

    init {
        // initialize bitmap array
        val centerBitmapArray: Array<Bitmap> = Array(centerDrawableResIdArray.size) { i ->
            (ContextCompat.getDrawable(preservedContext, centerDrawableResIdArray[i]) as BitmapDrawable).bitmap
        }
        val bottomBitmapArray: Array<Bitmap> = Array(bottomDrawableResIdArray.size) { i ->
            (ContextCompat.getDrawable(preservedContext, bottomDrawableResIdArray[i]) as BitmapDrawable).bitmap
        }

        // initialize pixels array
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
    }

    fun detectImage(): DetectResult? {
        // initialize screen bitmap
        var screenBitmap = ProjectionService.getScreenProjection()
        if (screenBitmap.width != screenWidth) screenBitmap = Bitmap.createScaledBitmap(screenBitmap, screenWidth, screenHeight, true)

        // detect center image
        val centerDetectResult = detectCenterImage(screenBitmap)

        // detect bottom image
        val bottomDetectResult = detectBottomImage(screenBitmap)

        // recycle bitmaps
        screenBitmap.recycle()

        val returnDetectResult = if (centerDetectResult.distance < bottomDetectResult.distance) centerDetectResult else bottomDetectResult
        return if (returnDetectResult.distance > DISTANCE_THRESHOLD) null else returnDetectResult
    }

    fun detectRetryImage(): DetectResult {
        // initialize screen bitmap
        var screenBitmap = ProjectionService.getScreenProjection()
        if (screenBitmap.width != screenWidth) screenBitmap = Bitmap.createScaledBitmap(screenBitmap, screenWidth, screenHeight, true)

        return detectCenterImage(screenBitmap)
    }

    private fun detectCenterImage(screenBitmap: Bitmap): DetectResult {
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

        // recycler bitmap
        croppedBitmap.recycle()

        // TODO update
        // initialize click point
        val clickPoint = when (centerDrawableResIdArray[indexOfMin]) {
            R.drawable.image_retry -> Point(1080 * 3 / 4, IMAGE_HEIGHT * 9 / 10 + y)
            else -> Point(-1, -1)
        }

        return DetectResult(clickPoint, minDistance, false)
    }

    private fun detectBottomImage(screenBitmap: Bitmap): DetectResult {
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

        // recycler bitmap
        croppedBitmap.recycle()

        // TODO update
        // initialize click point
        val clickPoint = when (bottomDrawableResIdArray[indexOfMin]) {
            R.drawable.image_gate -> Point(1080 / 16 * 3, IMAGE_HEIGHT * 3 / 4 + y)
            R.drawable.image_duel,
            R.drawable.image_duel_2,
            R.drawable.image_dialog -> Point(1080 / 2, IMAGE_HEIGHT / 8 + y)
            else -> Point(-1, -1)
        }

        // initialize is duel
        val isDuel = when (bottomDrawableResIdArray[indexOfMin]) {
            R.drawable.image_duel, R.drawable.image_duel_2 -> true
            else -> false
        }

        return DetectResult(clickPoint, minDistance, isDuel)
    }

    private fun computeDistanceAverage(drawablePixels: IntArray, screenPixels: IntArray): Long {
        var distance = 0L
        var compareCount = 0

        for (i in drawablePixels.indices) {
            if (drawablePixels[i] == 0)
                continue
            distance += abs(drawablePixels[i] - screenPixels[i])
            compareCount++
        }

        return distance / compareCount
    }
}