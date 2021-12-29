package com.speedroid.macroid.macro.controller

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.core.content.ContextCompat
import com.speedroid.macroid.Configs
import com.speedroid.macroid.Configs.Companion.DISTANCE_THRESHOLD
import com.speedroid.macroid.Configs.Companion.IMAGE_HEIGHT
import com.speedroid.macroid.Configs.Companion.IMAGE_WIDTH
import com.speedroid.macroid.R
import com.speedroid.macroid.macro.result.UsualDetectResult
import com.speedroid.macroid.service.ProjectionService
import com.speedroid.macroid.ui.activity.SplashActivity

class DuelBaseImageController : BaseImageController() {
    private val matPixels: IntArray = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT)

    init {
        // initialize pixels
        val matBitmap = (ContextCompat.getDrawable(SplashActivity.preservedContext, R.drawable.image_mat) as BitmapDrawable).bitmap
        matBitmap.getPixels(matPixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)
    }

    fun detectMat(): Long? {
        // initialize screen bitmap
        var screenBitmap = ProjectionService.getScreenProjection()
        if (screenBitmap.width != screenWidth) screenBitmap = Bitmap.createScaledBitmap(screenBitmap, screenWidth, screenHeight, true)

        // initialize y
        val y: Int = screenHeight - IMAGE_HEIGHT - IMAGE_HEIGHT

        // initialize cropped pixel
        val croppedBitmap = Bitmap.createBitmap(screenBitmap, 0, y, IMAGE_WIDTH, IMAGE_HEIGHT)
        val croppedPixels = IntArray(IMAGE_WIDTH * IMAGE_HEIGHT)
        croppedBitmap.getPixels(croppedPixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT)

        // detect
        val distance = computeDistanceAverage(matPixels, croppedPixels)

        // recycle bitmap
        croppedBitmap.recycle()

        Log.d("test", "distance $distance")

        return if (distance > DISTANCE_THRESHOLD) null else distance
    }

}