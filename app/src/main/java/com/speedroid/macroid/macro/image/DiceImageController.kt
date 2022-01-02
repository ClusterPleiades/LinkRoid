package com.speedroid.macroid.macro.image

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import com.speedroid.macroid.Configs.Companion.IMAGE_HEIGHT_SMALL
import com.speedroid.macroid.Configs.Companion.IMAGE_WIDTH
import com.speedroid.macroid.Configs.Companion.THRESHOLD_DISTANCE
import com.speedroid.macroid.R
import com.speedroid.macroid.macro.DetectResult
import com.speedroid.macroid.ui.activity.ModeActivity

class DiceImageController : BaseImageController() {
    private val difficultyDrawablePixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_SMALL)
    private val convDrawablePixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_SMALL)

    init {
        // initialize drawable pixels
        val difficultyBitmap = (ContextCompat.getDrawable(ModeActivity.preservedContext, R.drawable.image_button_difficulty) as BitmapDrawable).bitmap
        difficultyBitmap.getPixels(difficultyDrawablePixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        difficultyBitmap.recycle()

        val convBitmap = (ContextCompat.getDrawable(ModeActivity.preservedContext, R.drawable.image_background_conv) as BitmapDrawable).bitmap
        convBitmap.getPixels(convDrawablePixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        convBitmap.recycle()
    }

    fun detectDifficultyImage(screenBitmap: Bitmap): DetectResult? {
        // initialize y
        val y: Int = (screenHeight - IMAGE_HEIGHT_SMALL) / 2

        // initialize cropped pixel
        val croppedBitmap = Bitmap.createBitmap(screenBitmap, 0, y, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        val croppedPixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_SMALL)
        croppedBitmap.getPixels(croppedPixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        croppedBitmap.recycle()

        // detect
        val distance = computeDistanceAverage(difficultyDrawablePixels, croppedPixels)

        // check threshold
        if (distance > THRESHOLD_DISTANCE)
            return null

        // initialize click point
        val clickPoint = Point(1080 * 3 / 4, IMAGE_HEIGHT_SMALL / 2 + y)

        return DetectResult(R.drawable.image_button_retry_s, clickPoint, distance)
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
}