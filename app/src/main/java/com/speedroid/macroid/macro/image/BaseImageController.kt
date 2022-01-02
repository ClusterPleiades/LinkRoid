package com.speedroid.macroid.macro.image

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import com.speedroid.macroid.Configs.Companion.IMAGE_HEIGHT_LARGE
import com.speedroid.macroid.Configs.Companion.IMAGE_HEIGHT_SMALL
import com.speedroid.macroid.Configs.Companion.IMAGE_WIDTH
import com.speedroid.macroid.Configs.Companion.THRESHOLD_DISTANCE
import com.speedroid.macroid.DeviceController
import com.speedroid.macroid.R
import com.speedroid.macroid.macro.DetectResult
import com.speedroid.macroid.ui.activity.ModeActivity.Companion.preservedContext
import kotlin.math.abs

open class BaseImageController {
    private val deviceController: DeviceController = DeviceController(preservedContext)
    val screenHeight = deviceController.getHeightMax()

    private val winDrawablePixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_SMALL)
    val backDrawablePixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_SMALL)
    val convDrawablePixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_SMALL)

    val gateDrawablePixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_SMALL)

    private val smallRetryDrawablePixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_SMALL)
    private val largeRetryDrawablePixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_LARGE)

    init {
        // initialize drawable pixels
        val winBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_button_win) as BitmapDrawable).bitmap
        winBitmap.getPixels(winDrawablePixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        winBitmap.recycle()

        val backBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_button_back) as BitmapDrawable).bitmap
        backBitmap.getPixels(backDrawablePixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        backBitmap.recycle()

        val convBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_background_conv) as BitmapDrawable).bitmap
        convBitmap.getPixels(convDrawablePixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        convBitmap.recycle()

        val gateBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_button_gate) as BitmapDrawable).bitmap
        gateBitmap.getPixels(gateDrawablePixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        gateBitmap.recycle()

        val smallRetryBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_button_retry_s) as BitmapDrawable).bitmap
        smallRetryBitmap.getPixels(smallRetryDrawablePixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        smallRetryBitmap.recycle()

        val largeRetryBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_button_retry_l) as BitmapDrawable).bitmap
        largeRetryBitmap.getPixels(largeRetryDrawablePixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_LARGE)
        largeRetryBitmap.recycle()
    }

    fun computeDistanceAverage(drawablePixels: IntArray, screenPixels: IntArray): Long {
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

    fun detectWinImage(screenBitmap: Bitmap): DetectResult? {
        // initialize y
        val y = screenHeight - IMAGE_HEIGHT_SMALL

        // initialize cropped pixel
        val croppedBitmap = Bitmap.createBitmap(screenBitmap, 0, y, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        val croppedPixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_SMALL)
        croppedBitmap.getPixels(croppedPixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        croppedBitmap.recycle()

        // detect
        val distance = computeDistanceAverage(winDrawablePixels, croppedPixels)

        // check threshold
        if (distance > THRESHOLD_DISTANCE)
            return null

        // initialize click point
        val clickPoint = Point(1080 / 2, y + IMAGE_HEIGHT_SMALL * 3 / 5)

        return DetectResult(R.drawable.image_button_win, clickPoint, distance)
    }

    fun detectBackImage(screenBitmap: Bitmap): DetectResult? {
        // initialize y
        val y = screenHeight - IMAGE_HEIGHT_SMALL

        // initialize cropped pixel
        val croppedBitmap = Bitmap.createBitmap(screenBitmap, 0, y, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        val croppedPixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_SMALL)
        croppedBitmap.getPixels(croppedPixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        croppedBitmap.recycle()

        // detect
        val distance = computeDistanceAverage(backDrawablePixels, croppedPixels)

        // check threshold
        if (distance > THRESHOLD_DISTANCE)
            return null

        return DetectResult(R.drawable.image_button_back, null, distance)
    }

    fun detectConvImage(screenBitmap: Bitmap): DetectResult? {
        // initialize y
        val y = screenHeight - IMAGE_HEIGHT_SMALL

        // initialize cropped pixel
        val croppedBitmap = Bitmap.createBitmap(screenBitmap, 0, y, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        val croppedPixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_SMALL)
        croppedBitmap.getPixels(croppedPixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        croppedBitmap.recycle()

        // detect
        val distance = computeDistanceAverage(convDrawablePixels, croppedPixels)

        // check threshold
        if (distance > THRESHOLD_DISTANCE)
            return null

        return DetectResult(R.drawable.image_background_conv, null, distance)
    }

    fun detectRetryImage(screenBitmap: Bitmap): DetectResult? {
        // detect
        val smallRetryDetectResult = detectSmallRetryImage(screenBitmap)
        val largeRetryDetectResult = detectLargeRetryImage(screenBitmap)

        return smallRetryDetectResult ?: largeRetryDetectResult
    }

    private fun detectSmallRetryImage(screenBitmap: Bitmap): DetectResult? {
        // initialize y
        val y: Int = (screenHeight - IMAGE_HEIGHT_SMALL) / 2

        // initialize cropped pixel
        val croppedBitmap = Bitmap.createBitmap(screenBitmap, 0, y, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        val croppedPixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_SMALL)
        croppedBitmap.getPixels(croppedPixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        croppedBitmap.recycle()

        // detect
        val distance = computeDistanceAverage(smallRetryDrawablePixels, croppedPixels)

        // check threshold
        if (distance > THRESHOLD_DISTANCE)
            return null

        // initialize click point
        val clickPoint = Point(1080 * 3 / 4, IMAGE_HEIGHT_SMALL * 9 / 10 + y)

        return DetectResult(R.drawable.image_button_retry_s, clickPoint, distance)
    }

    private fun detectLargeRetryImage(screenBitmap: Bitmap): DetectResult? {
        // initialize y
        val y: Int = (screenHeight - IMAGE_HEIGHT_LARGE) / 2

        // initialize cropped pixel
        val croppedBitmap = Bitmap.createBitmap(screenBitmap, 0, y, IMAGE_WIDTH, IMAGE_HEIGHT_LARGE)
        val croppedPixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_LARGE)
        croppedBitmap.getPixels(croppedPixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_LARGE)
        croppedBitmap.recycle()

        // detect
        val distance = computeDistanceAverage(largeRetryDrawablePixels, croppedPixels)

        // check threshold
        if (distance > THRESHOLD_DISTANCE)
            return null

        // initialize click point
        val clickPoint = Point(1080 * 3 / 4, IMAGE_HEIGHT_LARGE * 7 / 8 + y)

        return DetectResult(R.drawable.image_button_retry_l, clickPoint, distance)
    }
}