package com.speedroid.macroid.macro

import android.content.Intent
import android.os.Handler
import android.util.Log
import com.speedroid.macroid.Configs.Companion.DELAY_INTERVAL
import com.speedroid.macroid.Configs.Companion.DELAY_START
import com.speedroid.macroid.macro.controller.UsualImageController
import com.speedroid.macroid.service.ClickService
import com.speedroid.macroid.ui.activity.SplashActivity.Companion.preservedContext

class GateMacro {
    companion object {
        var macroHandler: Handler? = null
    }

    private val usualImageController: UsualImageController = UsualImageController()
    private val runnable: Runnable
    private var duelButtonCount = 0

    init {
        // initialize handler
        macroHandler = Handler(preservedContext.mainLooper!!)

        // initialize runnable
        object : Runnable {
            override fun run() {

                if (duelButtonCount == 2) {
                    // TODO duel logic
                    Log.d("test", "duel")
                    return
                } else {
                    // detect
                    val detectResult = usualImageController.detectImage()
                    if (detectResult != null) {
                        // count duel button
                        if (detectResult.isDuel) duelButtonCount++

                        // click
                        val intent = Intent(preservedContext, ClickService::class.java)
                        intent.putExtra("x", detectResult.clickPoint.x)
                        intent.putExtra("y", detectResult.clickPoint.y)
                        preservedContext.startService(intent)
                    }
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