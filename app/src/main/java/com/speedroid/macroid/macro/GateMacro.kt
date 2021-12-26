package com.speedroid.macroid.macro

import android.content.Intent
import android.os.Handler
import com.speedroid.macroid.Configs.Companion.DELAY_INTERVAL
import com.speedroid.macroid.Configs.Companion.DELAY_START
import com.speedroid.macroid.ImageController
import com.speedroid.macroid.service.ClickService
import com.speedroid.macroid.ui.activity.SplashActivity.Companion.preservedContext

class GateMacro {
    companion object {
        var macroHandler: Handler? = null
    }

    private val imageController: ImageController = ImageController()
    private val runnable: Runnable

    init {
        // initialize handler
        macroHandler = Handler(preservedContext.mainLooper!!)

        // initialize runnable
        object : Runnable {
            override fun run() {
                val coordinate = imageController.findCoordinate()
                if (coordinate != null) {
                    val intent = Intent(preservedContext, ClickService::class.java)
                    intent.putExtra("x", coordinate.x)
                    intent.putExtra("y", coordinate.y)
                    preservedContext.startService(intent)
                }

                // run again
                macroHandler!!.postDelayed(this, DELAY_INTERVAL)
            }
        }.also { runnable = it }
    }

    fun startMacro() {
        macroHandler!!.postDelayed(runnable, DELAY_START)
    }
}