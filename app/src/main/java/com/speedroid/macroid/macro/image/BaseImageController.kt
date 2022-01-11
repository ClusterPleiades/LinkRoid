package com.speedroid.macroid.macro.image

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.core.content.ContextCompat
import com.speedroid.macroid.Configs.Companion.IMAGE_WIDTH
import com.speedroid.macroid.Configs.Companion.THRESHOLD_DISTANCE
import com.speedroid.macroid.Configs.Companion.X_PHASE
import com.speedroid.macroid.Configs.Companion.Y_FROM_BOTTOM_DECK
import com.speedroid.macroid.Configs.Companion.Y_FROM_BOTTOM_PHASE
import com.speedroid.macroid.DeviceController
import com.speedroid.macroid.R
import com.speedroid.macroid.ui.activity.ModeActivity.Companion.preservedContext
import kotlin.math.abs

open class BaseImageController {
    private val deviceController: DeviceController = DeviceController(preservedContext)
    val screenHeight = deviceController.getHeightMax()

    // hash map of drawable resource id
    val pixelsHashMap = HashMap<Int, IntArray>()
    private val heightHashMap = HashMap<Int, Int>()
    private val yHashMap = HashMap<Int, Int>()
    val clickPointHashMap = HashMap<Int, Point>()

    init {
        initializePhysicsHashMaps()
        initializeOptionHashMaps()
    }

    private fun initializePhysicsHashMaps() {
        val playerBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_background_player) as BitmapDrawable).bitmap
        val enemyBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_background_enemy) as BitmapDrawable).bitmap
        val draw1Bitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_background_draw_1) as BitmapDrawable).bitmap
        val draw2Bitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_background_draw_2) as BitmapDrawable).bitmap
        val winBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_button_win) as BitmapDrawable).bitmap
        val largeRetryBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_button_retry_l) as BitmapDrawable).bitmap
        val smallRetryBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_button_retry_s) as BitmapDrawable).bitmap
        val backBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_button_back) as BitmapDrawable).bitmap
        val convBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_background_conv) as BitmapDrawable).bitmap
        val gateBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_button_gate) as BitmapDrawable).bitmap
        val appearBitmap = (ContextCompat.getDrawable(preservedContext, R.drawable.image_button_appear) as BitmapDrawable).bitmap

        val playerPixelArray = IntArray(playerBitmap.width * playerBitmap.height)
        val enemyPixelArray = IntArray(enemyBitmap.width * enemyBitmap.height)
        val draw1PixelArray = IntArray(draw1Bitmap.width * draw1Bitmap.height)
        val draw2PixelArray = IntArray(draw2Bitmap.width * draw2Bitmap.height)
        val winPixelArray = IntArray(winBitmap.width * winBitmap.height)
        val largeRetryPixelArray = IntArray(largeRetryBitmap.width * largeRetryBitmap.height)
        val smallRetryPixelArray = IntArray(smallRetryBitmap.width * smallRetryBitmap.height)
        val backPixelArray = IntArray(backBitmap.width * backBitmap.height)
        val convPixelArray = IntArray(convBitmap.width * convBitmap.height)
        val gatePixelArray = IntArray(gateBitmap.width * gateBitmap.height)
        val appearPixelArray = IntArray(appearBitmap.width * appearBitmap.height)

        playerBitmap.getPixels(playerPixelArray, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, playerBitmap.height)
        enemyBitmap.getPixels(enemyPixelArray, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, enemyBitmap.height)
        draw1Bitmap.getPixels(draw1PixelArray, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, draw1Bitmap.height)
        draw2Bitmap.getPixels(draw2PixelArray, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, draw2Bitmap.height)
        winBitmap.getPixels(winPixelArray, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, winBitmap.height)
        largeRetryBitmap.getPixels(largeRetryPixelArray, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, largeRetryBitmap.height)
        smallRetryBitmap.getPixels(smallRetryPixelArray, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, smallRetryBitmap.height)
        backBitmap.getPixels(backPixelArray, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, backBitmap.height)
        convBitmap.getPixels(convPixelArray, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, convBitmap.height)
        gateBitmap.getPixels(gatePixelArray, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, gateBitmap.height)
        appearBitmap.getPixels(appearPixelArray, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, appearBitmap.height)

        pixelsHashMap[R.drawable.image_background_player] = playerPixelArray
        pixelsHashMap[R.drawable.image_background_enemy] = enemyPixelArray
        pixelsHashMap[R.drawable.image_background_draw_1] = draw1PixelArray
        pixelsHashMap[R.drawable.image_background_draw_2] = draw2PixelArray
        pixelsHashMap[R.drawable.image_button_win] = winPixelArray
        pixelsHashMap[R.drawable.image_button_retry_l] = largeRetryPixelArray
        pixelsHashMap[R.drawable.image_button_retry_s] = smallRetryPixelArray
        pixelsHashMap[R.drawable.image_button_back] = backPixelArray
        pixelsHashMap[R.drawable.image_background_conv] = convPixelArray
        pixelsHashMap[R.drawable.image_button_gate] = gatePixelArray
        pixelsHashMap[R.drawable.image_button_appear] = appearPixelArray

        heightHashMap[R.drawable.image_background_player] = playerBitmap.height
        heightHashMap[R.drawable.image_background_enemy] = enemyBitmap.height
        heightHashMap[R.drawable.image_background_draw_1] = draw1Bitmap.height
        heightHashMap[R.drawable.image_background_draw_2] = draw2Bitmap.height
        heightHashMap[R.drawable.image_button_win] = winBitmap.height
        heightHashMap[R.drawable.image_button_retry_l] = largeRetryBitmap.height
        heightHashMap[R.drawable.image_button_retry_s] = smallRetryBitmap.height
        heightHashMap[R.drawable.image_button_back] = backBitmap.height
        heightHashMap[R.drawable.image_background_conv] = convBitmap.height
        heightHashMap[R.drawable.image_button_gate] = gateBitmap.height
        heightHashMap[R.drawable.image_button_appear] = appearBitmap.height

        playerBitmap.recycle()
        enemyBitmap.recycle()
        draw1Bitmap.recycle()
        draw2Bitmap.recycle()
        winBitmap.recycle()
        largeRetryBitmap.recycle()
        smallRetryBitmap.recycle()
        backBitmap.recycle()
        convBitmap.recycle()
        gateBitmap.recycle()
        appearBitmap.recycle()
    }

    private fun initializeOptionHashMaps() {
        // case center
        yHashMap[R.drawable.image_button_appear] = (screenHeight - heightHashMap[R.drawable.image_button_appear]!!) / 2
        yHashMap[R.drawable.image_button_retry_l] = (screenHeight - heightHashMap[R.drawable.image_button_retry_l]!!) / 2
        yHashMap[R.drawable.image_button_retry_s] = (screenHeight - heightHashMap[R.drawable.image_button_retry_s]!!) / 2

        // case bottom
        yHashMap[R.drawable.image_button_win] = screenHeight - heightHashMap[R.drawable.image_button_win]!!
        yHashMap[R.drawable.image_button_gate] = screenHeight - heightHashMap[R.drawable.image_button_gate]!!
        yHashMap[R.drawable.image_button_back] = screenHeight - heightHashMap[R.drawable.image_button_back]!!
        yHashMap[R.drawable.image_background_conv] = screenHeight - heightHashMap[R.drawable.image_background_conv]!!

        // else
        yHashMap[R.drawable.image_background_player] = screenHeight - Y_FROM_BOTTOM_DECK
        yHashMap[R.drawable.image_background_enemy] = screenHeight - Y_FROM_BOTTOM_DECK
        yHashMap[R.drawable.image_background_draw_1] = screenHeight - Y_FROM_BOTTOM_DECK
        yHashMap[R.drawable.image_background_draw_2] = screenHeight - Y_FROM_BOTTOM_DECK

        clickPointHashMap[R.drawable.image_button_appear] =
            Point(1080 / 4, yHashMap[R.drawable.image_button_appear]!! + heightHashMap[R.drawable.image_button_appear]!! / 10 * 9)
        clickPointHashMap[R.drawable.image_button_retry_l] =
            Point(1080 / 4 * 3, yHashMap[R.drawable.image_button_retry_l]!! + heightHashMap[R.drawable.image_button_retry_l]!! / 8 * 7)
        clickPointHashMap[R.drawable.image_button_retry_s] =
            Point(1080 / 4 * 3, yHashMap[R.drawable.image_button_retry_s]!! + heightHashMap[R.drawable.image_button_retry_s]!! / 10 * 9)
        clickPointHashMap[R.drawable.image_button_win] =
            Point(1080 / 2, yHashMap[R.drawable.image_button_win]!! + heightHashMap[R.drawable.image_button_win]!! / 5 * 3)
        clickPointHashMap[R.drawable.image_button_gate] =
            Point(1080 / 16 * 3, yHashMap[R.drawable.image_button_gate]!! + heightHashMap[R.drawable.image_button_gate]!! / 4 * 3)
        clickPointHashMap[R.drawable.image_button_back] =
            Point(1080 / 2, yHashMap[R.drawable.image_button_back]!! + heightHashMap[R.drawable.image_button_back]!! / 8)
        clickPointHashMap[R.drawable.image_background_conv] =
            Point(1080 / 2, yHashMap[R.drawable.image_background_conv]!! + heightHashMap[R.drawable.image_background_conv]!! / 8)
        clickPointHashMap[R.drawable.image_background_player] =
            Point(X_PHASE, screenHeight - Y_FROM_BOTTOM_PHASE)
        clickPointHashMap[R.drawable.image_background_enemy] =
            Point(X_PHASE, screenHeight - Y_FROM_BOTTOM_PHASE)
        clickPointHashMap[R.drawable.image_background_draw_1] =
            Point(X_PHASE, screenHeight - Y_FROM_BOTTOM_PHASE)
        clickPointHashMap[R.drawable.image_background_draw_2] =
            Point(X_PHASE, screenHeight - Y_FROM_BOTTOM_PHASE)
    }

    private fun getCroppedPixels(screenBitmap: Bitmap, drawableResId: Int): IntArray? {
        val imageHeight = heightHashMap[drawableResId] ?: return null
        val imageY = yHashMap[drawableResId] ?: return null

        val croppedBitmap = Bitmap.createBitmap(screenBitmap, 0, imageY, IMAGE_WIDTH, imageHeight)
        val croppedPixels = IntArray(IMAGE_WIDTH * imageHeight)
        croppedBitmap.getPixels(croppedPixels, 0, IMAGE_WIDTH, 0, 0, IMAGE_WIDTH, imageHeight)
        croppedBitmap.recycle()

        return croppedPixels
    }

    private fun getDistanceAverage(drawablePixels: IntArray, screenPixels: IntArray): Long {
        var distance = 0L
        var compareCount = 0

        for (i in drawablePixels.indices)
            if (drawablePixels[i] != 0) {
                distance += abs(drawablePixels[i] - screenPixels[i])
                compareCount++
            }

        return distance / compareCount
    }

    fun getDistanceAverageResult(drawablePixels: IntArray, screenPixels: IntArray): Long? {
        val distanceAverage = getDistanceAverage(drawablePixels, screenPixels)
        return if (distanceAverage > THRESHOLD_DISTANCE) null
        else distanceAverage
    }

    fun detectImage(screenBitmap: Bitmap, drawableResId: Int): DetectResult? {
        val imagePixels = pixelsHashMap[drawableResId] ?: return null
        val croppedPixels = getCroppedPixels(screenBitmap, drawableResId) ?: return null
        val distance = getDistanceAverageResult(imagePixels, croppedPixels) ?: return null

        if(drawableResId == R.drawable.image_button_win)
            Log.d("test", "win distance $distance")

        return DetectResult(drawableResId)
    }

    fun detectRetryImage(screenBitmap: Bitmap): DetectResult? {
        val smallRetryDetectResult = detectImage(screenBitmap, R.drawable.image_button_retry_s)
        val largeRetryDetectResult = detectImage(screenBitmap, R.drawable.image_button_retry_l)
        return smallRetryDetectResult ?: largeRetryDetectResult
    }

    fun detectDeckImage(screenBitmap: Bitmap): DetectResult {
        val croppedPixels = getCroppedPixels(screenBitmap, R.drawable.image_background_player)!!
        val playerDistanceAverage = getDistanceAverage(pixelsHashMap[R.drawable.image_background_player]!!, croppedPixels)
        val enemyDistanceAverage = getDistanceAverage(pixelsHashMap[R.drawable.image_background_enemy]!!, croppedPixels)
        val draw1DistanceAverage = getDistanceAverage(pixelsHashMap[R.drawable.image_background_draw_1]!!, croppedPixels)
        val draw2DistanceAverage = getDistanceAverage(pixelsHashMap[R.drawable.image_background_draw_2]!!, croppedPixels)

        var minDistanceAverage = Long.MAX_VALUE
        var drawableResId = R.drawable.image_background_enemy

        if (playerDistanceAverage < minDistanceAverage) {
            minDistanceAverage = playerDistanceAverage
            drawableResId = R.drawable.image_background_player
        }
        if (enemyDistanceAverage < minDistanceAverage) {
            minDistanceAverage = enemyDistanceAverage
            drawableResId = R.drawable.image_background_enemy
        }
        if (draw1DistanceAverage < minDistanceAverage) {
            minDistanceAverage = draw1DistanceAverage
            drawableResId = R.drawable.image_background_draw_1
        }
        if (draw2DistanceAverage < minDistanceAverage) {
            drawableResId = R.drawable.image_background_draw_2
        }

        return DetectResult(drawableResId)
    }

    inner class DetectResult {
        val drawableResId: Int
        val clickPoint: Point?

        constructor(drawableResId: Int) {
            this.drawableResId = drawableResId
            clickPoint = clickPointHashMap[drawableResId]
        }

        constructor(drawableResId: Int, clickPoint: Point?) {
            this.drawableResId = drawableResId
            this.clickPoint = clickPoint
        }
    }
}