package com.speedroid.macroid.macro

import android.graphics.Bitmap
import android.os.Handler
import com.speedroid.macroid.Configs.Companion.DELAY_START
import com.speedroid.macroid.DeviceController
import com.speedroid.macroid.ImageController
import com.speedroid.macroid.R
import com.speedroid.macroid.service.ProjectionService
import com.speedroid.macroid.ui.activity.SplashActivity.Companion.preservedContext

class GateMacro {
    companion object {
        var macroHandler: Handler? = null
    }

    private val deviceController: DeviceController = DeviceController(preservedContext)
    private val imageController: ImageController = ImageController()

    private val screenWidth = deviceController.getWidthMax()
    private val screenHeight = deviceController.getHeightMax()
    private val runnable: Runnable

    init {
        // initialize handler
        macroHandler = Handler(preservedContext.mainLooper!!)

        // initialize runnable
        object : Runnable {
            override fun run() {
                // initialize screen bitmap
                var screenBitmap = ProjectionService.getScreenProjection()
                if (screenBitmap.width != screenWidth) screenBitmap = Bitmap.createScaledBitmap(screenBitmap, screenWidth, screenHeight, true)

                // detect
                imageController.detect(screenBitmap, R.drawable.image_retry)




                // recycle bitmaps
                screenBitmap.recycle()

//                val detectResult = imageController.detect()
//                val intent = Intent(preservedContext, ClickService::class.java)
//                intent.putExtra("x", detectResult.fromX)
//                intent.putExtra("y", detectResult.fromY)
//
//                // click
//                preservedContext.startService(intent)

                // run again
//                macroHandler!!.postDelayed(this, DELAY_INTERVAL)
            }
        }.also { runnable = it }
    }

    fun startMacro() {
        macroHandler!!.postDelayed(runnable, DELAY_START)
    }
}