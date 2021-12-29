package com.speedroid.macroid.macro.controller

import com.speedroid.macroid.DeviceController
import com.speedroid.macroid.ui.activity.ModeActivity.Companion.preservedContext
import com.speedroid.macroid.ui.activity.SplashActivity
import kotlin.math.abs

open class BaseImageController {
    private val deviceController: DeviceController = DeviceController(preservedContext)
    open val screenWidth = deviceController.getWidthMax()
    open val screenHeight = deviceController.getHeightMax()

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
}