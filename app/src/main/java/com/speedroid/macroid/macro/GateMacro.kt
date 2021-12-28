package com.speedroid.macroid.macro

import android.content.Intent
import android.graphics.Point
import android.os.Handler
import com.speedroid.macroid.Configs.Companion.DELAY_INTERVAL
import com.speedroid.macroid.Configs.Companion.DELAY_START
import com.speedroid.macroid.Configs.Companion.STATE_DUEL_READY
import com.speedroid.macroid.Configs.Companion.STATE_DUEL_STANDBY
import com.speedroid.macroid.Configs.Companion.STATE_DUEL_START
import com.speedroid.macroid.Configs.Companion.STATE_NON_DUEL
import com.speedroid.macroid.macro.controller.DuelImageController
import com.speedroid.macroid.macro.controller.UsualImageController
import com.speedroid.macroid.service.ClickService
import com.speedroid.macroid.ui.activity.SplashActivity.Companion.preservedContext

class GateMacro {
    companion object {
        var macroHandler: Handler? = null
    }

    private val usualImageController: UsualImageController = UsualImageController()
    private val duelImageController: DuelImageController = DuelImageController()
    private val runnable: Runnable
    private var state = STATE_NON_DUEL

    init {
        // initialize handler
        macroHandler = Handler(preservedContext.mainLooper!!)

        // initialize runnable
        object : Runnable {
            override fun run() {
                // initialize delay
                var delay = DELAY_INTERVAL

                when (state) {
                    STATE_NON_DUEL, STATE_DUEL_READY -> {
                        // detect image
                        val detectResult = usualImageController.detectImage()
                        if (detectResult != null) {
                            // change state
                            if (detectResult.isDuel) state++

                            // click
                            click(detectResult.clickPoint)
                        }

                        // repeat
                        macroHandler!!.postDelayed(this, delay)
                    }
                    STATE_DUEL_STANDBY -> {
                        // detect mat
                        val detectResult = duelImageController.detectMat()
                        if (detectResult == null) {
                            // click retry location
                            click(usualImageController.detectRetryImage().clickPoint)
                        }
                        // change state
                        else state++

                        // repeat
                        macroHandler!!.postDelayed(this, delay)
                    }
                    STATE_DUEL_START -> {
                        // detect draw
                        var detectResult = duelImageController.detectDraw()
                        if (detectResult == null) {
                            // detect confirm
                            detectResult = duelImageController.detectConfirm()
                            if (detectResult != null) {
                                // change state
                                state = STATE_NON_DUEL

                                // click
                                click(detectResult.clickPoint)
                            }

                            // repeat
                            macroHandler!!.postDelayed(this, delay)
                        } else {
                            // TODO duel ~ endphase -> state = DUEL_START
                        }
                    }
                }
            }
        }.also { runnable = it }
    }

    fun startMacro() {
        macroHandler!!.postDelayed(runnable, DELAY_START)
    }

    fun click(point: Point) {
        val intent = Intent(preservedContext, ClickService::class.java)
        intent.putExtra("x", point.x)
        intent.putExtra("y", point.y)
        preservedContext.startService(intent)
    }
}