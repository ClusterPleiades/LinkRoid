package com.speedroid.macroid.macro.image

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import com.speedroid.macroid.Configs.Companion.IMAGE_HEIGHT
import com.speedroid.macroid.Configs.Companion.IMAGE_WIDTH
import com.speedroid.macroid.Configs.Companion.THRESHOLD_DISTANCE
import com.speedroid.macroid.DeviceController
import com.speedroid.macroid.R
import com.speedroid.macroid.macro.DetectResult
import com.speedroid.macroid.ui.activity.ModeActivity.Companion.preservedContext
import kotlin.math.abs

open class BaseImageController {
    private val deviceController: DeviceController = DeviceController(preservedContext)
    open val screenHeight = deviceController.getHeightMax()

    private val winDrawablePixels: IntArray = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT)
    private val retryDrawablePixels: IntArray = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT)

    init {
        // initialize drawable pixels
        val winBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_button_confirm) as BitmapDrawable).bitmap
        winBitmap.getPixels(winDrawablePixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)
        winBitmap.recycle()

        val retryBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_button_retry) as BitmapDrawable).bitmap
        retryBitmap.getPixels(retryDrawablePixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)
        retryBitmap.recycle()
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
        val y = screenHeight - IMAGE_HEIGHT

        // initialize cropped pixel
        val croppedBitmap = Bitmap.createBitmap(screenBitmap, 0, y, IMAGE_WIDTH, IMAGE_HEIGHT)
        val croppedPixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT)
        croppedBitmap.getPixels(croppedPixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)
        croppedBitmap.recycle()

        // detect
        val distance = computeDistanceAverage(winDrawablePixels, croppedPixels)

        // check threshold
        if (distance > THRESHOLD_DISTANCE)
            return null

        // initialize click point
        val clickPoint = Point(1080 / 2, y + IMAGE_HEIGHT * 3 / 5)

        return DetectResult(R.drawable.image_button_confirm, clickPoint, distance)
    }

    fun detectRetryImage(screenBitmap:Bitmap): DetectResult? {
        // initialize y
        val y: Int = (screenHeight - IMAGE_HEIGHT) / 2

        // initialize cropped pixel
        val croppedBitmap = Bitmap.createBitmap(screenBitmap, 0, y, IMAGE_WIDTH, IMAGE_HEIGHT)
        val croppedPixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT)
        croppedBitmap.getPixels(croppedPixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)
        croppedBitmap.recycle()

        // detect
        val distance = computeDistanceAverage(retryDrawablePixels, croppedPixels)

        // check threshold
        if (distance > THRESHOLD_DISTANCE)
            return null

        // initialize click point
        val clickPoint = Point(1080 * 3 / 4, IMAGE_HEIGHT * 9 / 10 + y)

        return DetectResult(R.drawable.image_button_retry, clickPoint, distance)
    }
}