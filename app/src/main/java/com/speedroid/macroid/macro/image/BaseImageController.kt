package com.speedroid.macroid.macro.image

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import com.speedroid.macroid.Configs.Companion.IMAGE_HEIGHT_HUGE
import com.speedroid.macroid.Configs.Companion.IMAGE_HEIGHT_LARGE
import com.speedroid.macroid.Configs.Companion.IMAGE_HEIGHT_SMALL
import com.speedroid.macroid.Configs.Companion.IMAGE_WIDTH
import com.speedroid.macroid.Configs.Companion.THRESHOLD_DISTANCE
import com.speedroid.macroid.DeviceController
import com.speedroid.macroid.R
import com.speedroid.macroid.ui.activity.ModeActivity.Companion.preservedContext
import kotlin.math.abs

open class BaseImageController {
    private val deviceController: DeviceController = DeviceController(preservedContext)
    val screenHeight = deviceController.getHeightMax()

    private val winDrawablePixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_SMALL)
    private val drawDrawablePixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_LARGE)
    private val largeRetryDrawablePixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_LARGE)
    private val smallRetryDrawablePixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_SMALL)

    val gateDrawablePixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_SMALL)
    val backDrawablePixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_SMALL)
    val convDrawablePixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_SMALL)

    val appearDrawablePixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_HUGE)

    init {
        // initialize private drawable pixels
        val winBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_button_win) as BitmapDrawable).bitmap
        winBitmap.getPixels(winDrawablePixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        winBitmap.recycle()

        val drawBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_button_draw) as BitmapDrawable).bitmap
        drawBitmap.getPixels(drawDrawablePixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_LARGE)
        drawBitmap.recycle()

        val largeRetryBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_button_retry_l) as BitmapDrawable).bitmap
        largeRetryBitmap.getPixels(largeRetryDrawablePixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_LARGE)
        largeRetryBitmap.recycle()

        val smallRetryBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_button_retry_s) as BitmapDrawable).bitmap
        smallRetryBitmap.getPixels(smallRetryDrawablePixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        smallRetryBitmap.recycle()

        // initialize public drawable pixels
        val backBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_button_back) as BitmapDrawable).bitmap
        backBitmap.getPixels(backDrawablePixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        backBitmap.recycle()

        val convBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_background_conv) as BitmapDrawable).bitmap
        convBitmap.getPixels(convDrawablePixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        convBitmap.recycle()

        val gateBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_button_gate) as BitmapDrawable).bitmap
        gateBitmap.getPixels(gateDrawablePixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        gateBitmap.recycle()

        val appearBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_button_appear) as BitmapDrawable).bitmap
        appearBitmap.getPixels(appearDrawablePixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_HUGE)
        appearBitmap.recycle()
    }

    fun detectImage(screenBitmap: Bitmap, drawableResId: Int): DetectResult? {
        val imageHeight = when (drawableResId) {
            R.drawable.image_button_appear -> IMAGE_HEIGHT_HUGE
            R.drawable.image_button_draw,
            R.drawable.image_button_retry_l -> IMAGE_HEIGHT_LARGE
            R.drawable.image_button_retry_s,
            R.drawable.image_button_win,
            R.drawable.image_button_gate,
            R.drawable.image_button_back,
            R.drawable.image_background_conv -> IMAGE_HEIGHT_SMALL
            else -> return null
        }

        val y = when (drawableResId) {
            R.drawable.image_button_appear,
            R.drawable.image_button_draw,
            R.drawable.image_button_retry_l,
            R.drawable.image_button_retry_s -> (screenHeight - imageHeight) / 2
            R.drawable.image_button_win,
            R.drawable.image_button_gate,
            R.drawable.image_button_back,
            R.drawable.image_background_conv -> screenHeight - imageHeight
            else -> return null
        }

        val drawablePixels = when (drawableResId) {
            R.drawable.image_button_appear -> appearDrawablePixels
            R.drawable.image_button_draw -> drawDrawablePixels
            R.drawable.image_button_retry_l -> largeRetryDrawablePixels
            R.drawable.image_button_retry_s -> smallRetryDrawablePixels
            R.drawable.image_button_win -> winDrawablePixels
            R.drawable.image_button_gate -> gateDrawablePixels
            R.drawable.image_button_back -> backDrawablePixels
            R.drawable.image_background_conv -> convDrawablePixels
            else -> return null
        }

        val croppedPixels = getCroppedPixels(screenBitmap, imageHeight, y)
        val distance = computeDistanceAverage(drawablePixels, croppedPixels)
        if (distance > THRESHOLD_DISTANCE)
            return null

        val clickPoint = when (drawableResId) {
            R.drawable.image_button_appear -> Point(1080 / 4, y + imageHeight * 9 / 10)
            R.drawable.image_button_draw -> Point(1080 / 2, screenHeight / 2)
            R.drawable.image_button_retry_l -> Point(1080 * 3 / 4, y + imageHeight * 7 / 8)
            R.drawable.image_button_retry_s -> Point(1080 * 3 / 4, y + imageHeight * 9 / 10)
            R.drawable.image_button_win -> Point(1080 / 2, y + imageHeight * 3 / 5)
            R.drawable.image_button_gate -> Point(1080 / 16 * 3, IMAGE_HEIGHT_SMALL * 3 / 4 + y)
            R.drawable.image_button_back,
            R.drawable.image_background_conv -> Point(1080 / 2, IMAGE_HEIGHT_SMALL / 8 + y) // duel button click point
            else -> return null
        }

        return DetectResult(drawableResId, clickPoint)
    }

    fun detectRetryImage(screenBitmap: Bitmap): DetectResult? {
        val smallRetryDetectResult = detectImage(screenBitmap, R.drawable.image_button_retry_s)
        val largeRetryDetectResult = detectImage(screenBitmap, R.drawable.image_button_retry_l)
        return smallRetryDetectResult ?: largeRetryDetectResult
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

    private fun getCroppedPixels(screenBitmap: Bitmap, imageHeight: Int, y: Int): IntArray {
        val croppedBitmap = Bitmap.createBitmap(screenBitmap, 0, y, IMAGE_WIDTH, imageHeight)
        val croppedPixels = IntArray(IMAGE_WIDTH * imageHeight)
        croppedBitmap.getPixels(croppedPixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, imageHeight)
        croppedBitmap.recycle()
        return croppedPixels
    }

    class DetectResult(val drawableResId: Int, val clickPoint: Point?)
}