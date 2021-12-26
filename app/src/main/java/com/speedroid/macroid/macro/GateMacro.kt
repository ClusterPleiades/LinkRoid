package com.speedroid.macroid.macro

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import androidx.core.content.ContextCompat
import com.speedroid.macroid.Configs.Companion.DELAY_START
import com.speedroid.macroid.Configs.Companion.PHASE_DUEL
import com.speedroid.macroid.Configs.Companion.PHASE_NON_DUEL
import com.speedroid.macroid.Configs.Companion.SIMILARITY_THRESHOLD
import com.speedroid.macroid.Configs.Companion.imageHeight
import com.speedroid.macroid.Configs.Companion.imageWidth
import com.speedroid.macroid.DeviceController
import com.speedroid.macroid.R
import com.speedroid.macroid.service.ClickService
import com.speedroid.macroid.service.ProjectionService
import kotlin.math.sqrt

class GateMacro(private val context: Context) {
    companion object {
        lateinit var macroHandler: Handler
    }

    private val deviceController: DeviceController = DeviceController(context)

    private val width = deviceController.getWidthMax()
    private val height = deviceController.getHeightMax()
    private var phase = PHASE_NON_DUEL

    private var nonDuelNormalizedPixelsArray: Array<DoubleArray?>

    init {
        // TODO update drawable ids
        val nonDuelDrawableResIdArrayList = ArrayList<Int>()
        nonDuelDrawableResIdArrayList.add(R.drawable.image_gate)

        // initialize non duel bitmap array
        val nonDuelBitmapArray: Array<Bitmap?> = arrayOfNulls(nonDuelDrawableResIdArrayList.size)
        for (i in nonDuelBitmapArray.indices) {
            nonDuelBitmapArray[i] = (ContextCompat.getDrawable(context, nonDuelDrawableResIdArrayList[i]) as BitmapDrawable).bitmap
        }

        // initialize non duel pixels array
        val nonDuelPixelsArray: Array<IntArray?> = arrayOfNulls(nonDuelBitmapArray.size)
        var pixels: IntArray
        for (i in nonDuelPixelsArray.indices) {
            pixels = IntArray(imageWidth * imageHeight)
            nonDuelBitmapArray[i]!!.getPixels(pixels, 0, imageWidth, 0, 0, imageWidth, imageHeight)
            nonDuelPixelsArray[i] = pixels
        }

        // initialize non duel normalized pixels array
        nonDuelNormalizedPixelsArray = arrayOfNulls(nonDuelPixelsArray.size)
        for (i in nonDuelNormalizedPixelsArray.indices) {
            nonDuelNormalizedPixelsArray[i] = normalize(nonDuelPixelsArray[i])
        }
    }

    fun startMacro() {
        macroHandler = Handler(context.mainLooper!!)
        macroHandler.postDelayed({
            while (true) {
                if (phase == PHASE_DUEL) {

                } else {
                    val coordinate = findCoordinate()
                    val intent = Intent(context, ClickService::class.java)
                    intent.putExtra("x", coordinate.x)
                    intent.putExtra("y", coordinate.y)
                    context.startService(intent)
                }
            }
        }, DELAY_START)
    }

    private fun findCoordinate(): Point {
        // initialize screen bitmap
        var screenBitmap = ProjectionService.getScreenProjection()
        if (screenBitmap.width != width) screenBitmap = Bitmap.createScaledBitmap(screenBitmap, width, height, true)

        // move y
        var y = height - imageHeight // start at bottom
        do {
            // move x
            var x = 0 // start at left
            do {
                // crop screen bitmap
                val croppedScreenBitmap = Bitmap.createBitmap(screenBitmap, x, y, imageWidth, imageHeight)
                val croppedScreenPixels = IntArray(imageWidth * imageHeight)
                croppedScreenBitmap.getPixels(croppedScreenPixels, 0, imageWidth, 0, 0, imageWidth, imageHeight)
                val normalizedCroppedScreenPixels = normalize(croppedScreenPixels)

                // compute similarity
                for (i in nonDuelNormalizedPixelsArray.indices) {
                    val similarity = computeSimilarity(nonDuelNormalizedPixelsArray[i], normalizedCroppedScreenPixels)
                    if (similarity > SIMILARITY_THRESHOLD) {
                        // recycle bitmaps
                        screenBitmap.recycle()
                        croppedScreenBitmap.recycle()

                        // return center coordinate
                        return Point(x + imageWidth / 2, y + imageHeight / 2)
                    }
                }

                // recycle cropped screen bitmap
                croppedScreenBitmap.recycle()

                x += 1
            } while (x <= width - imageWidth)
            y -= 1
        } while (y >= 0)

        // case not found
        return findCoordinate() // recursive call
    }

    private fun normalize(pixelArray: IntArray?): DoubleArray {
        val min = pixelArray!!.minOrNull()!!.toDouble()
        val max = pixelArray.maxOrNull()!!.toDouble()
        val normalizedArray = DoubleArray(pixelArray.size)
        for (i in normalizedArray.indices) normalizedArray[i] = 1 - ((pixelArray[i] - min) / (max - min))
        return normalizedArray
    }

    private fun computeSimilarity(normalizedIntArrayA: DoubleArray?, normalizedIntArrayB: DoubleArray): Double {
        var dotProduct = 0.0
        var sumA = 0.0
        var sumB = 0.0

        for (i in normalizedIntArrayA!!.indices) {
            dotProduct += normalizedIntArrayA[i] * normalizedIntArrayB[i]
            sumA += normalizedIntArrayA[i] * normalizedIntArrayA[i]
            sumB += normalizedIntArrayB[i] * normalizedIntArrayB[i]
        }

        return dotProduct / (sqrt(sumA) * sqrt(sumB))
    }

    private fun saveImage(bitmap: Bitmap) {
        val fileOutputStream = context.openFileOutput("temp.png", Context.MODE_PRIVATE)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        return
    }
}