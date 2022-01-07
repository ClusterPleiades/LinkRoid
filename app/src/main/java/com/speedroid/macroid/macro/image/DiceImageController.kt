package com.speedroid.macroid.macro.image

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import com.speedroid.macroid.Configs.Companion.IMAGE_HEIGHT_LARGE
import com.speedroid.macroid.Configs.Companion.IMAGE_HEIGHT_SMALL
import com.speedroid.macroid.Configs.Companion.IMAGE_WIDTH
import com.speedroid.macroid.R
import com.speedroid.macroid.ui.activity.ModeActivity.Companion.preservedContext

class DiceImageController : BaseImageController() {
    private val difficultyDrawablePixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_SMALL)
    private val moveDrawablePixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_LARGE)

    init {
        // initialize drawable pixels
        val difficultyBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_button_difficulty) as BitmapDrawable).bitmap
        difficultyBitmap.getPixels(difficultyDrawablePixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_SMALL)
        difficultyBitmap.recycle()

        val moveBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_button_move) as BitmapDrawable).bitmap
        moveBitmap.getPixels(moveDrawablePixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_LARGE)
        moveBitmap.recycle()
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
        getDistanceAverageResult(difficultyDrawablePixels, croppedPixels) ?: return null

        // initialize click point
        val clickPoint = Point(1080 * 3 / 4, IMAGE_HEIGHT_SMALL / 2 + y)

        return DetectResult(R.drawable.image_button_retry_s, clickPoint)
    }

    fun detectMoveImage(screenBitmap: Bitmap): DetectResult? {
        // initialize y
        val y: Int = (screenHeight - IMAGE_HEIGHT_LARGE) / 2

        // initialize cropped pixel
        val croppedBitmap = Bitmap.createBitmap(screenBitmap, 0, y, IMAGE_WIDTH, IMAGE_HEIGHT_LARGE)
        val croppedPixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT_LARGE)
        croppedBitmap.getPixels(croppedPixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT_LARGE)
        croppedBitmap.recycle()

        // detect
        getDistanceAverageResult(moveDrawablePixels, croppedPixels) ?: return null

        // initialize click point
        val clickPoint = Point(1080 * 3 / 4, IMAGE_HEIGHT_LARGE + y)

        return DetectResult(R.drawable.image_button_move, clickPoint)
    }
}