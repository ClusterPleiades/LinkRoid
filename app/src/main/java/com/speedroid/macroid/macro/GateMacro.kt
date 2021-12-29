package com.speedroid.macroid.macro

import android.content.Intent
import android.graphics.Point
import android.os.Handler
import com.speedroid.macroid.Configs.Companion.DELAY_USUAL
import com.speedroid.macroid.Configs.Companion.DELAY_START
import com.speedroid.macroid.Configs.Companion.STATE_DUEL_READY
import com.speedroid.macroid.Configs.Companion.STATE_DUEL_STANDBY
import com.speedroid.macroid.Configs.Companion.STATE_DUEL_START
import com.speedroid.macroid.Configs.Companion.STATE_USUAL_GATE
import com.speedroid.macroid.macro.controller.DuelBaseImageController
import com.speedroid.macroid.macro.controller.UsualBaseImageController
import com.speedroid.macroid.service.ClickService
import com.speedroid.macroid.ui.activity.SplashActivity.Companion.preservedContext

class GateMacro {
    companion object {
        var macroHandler: Handler? = null
    }

    private val usualImageController: UsualBaseImageController = UsualBaseImageController()
    private val duelImageController: DuelBaseImageController = DuelBaseImageController()
    private val runnable: Runnable
    private var state = STATE_USUAL_GATE

    init {
        // initialize handler
        macroHandler = Handler(preservedContext.mainLooper!!)

        // initialize runnable
        object : Runnable {
            override fun run() {
                when (state) {
                    STATE_USUAL_GATE, STATE_DUEL_READY -> {
                        // detect image
                        val detectResult = usualImageController.detectImage()
                        if (detectResult != null) {
                            // change state
                            if (detectResult.isDuel) state++

                            // click
                            click(detectResult.clickPoint)
                        }

                        // repeat
                        macroHandler!!.postDelayed(this, DELAY_USUAL)
                    }
                    STATE_DUEL_STANDBY -> {
                        // detect mat
                        val distance = duelImageController.detectMat()
                        if (distance == null) {
                            // click retry location
                            val detectResult = usualImageController.detectRetryImage()
                            if (detectResult != null)
                                click(detectResult.clickPoint)
                        }
                        // change state
                        else state++

//                        // repeat
//                        macroHandler!!.postDelayed(this, delay)
                    }
                    STATE_DUEL_START -> {
//                        // detect draw
//                        var detectResult = duelImageController.detectDraw()
//                        if (detectResult == null) {
//                            // detect confirm
//                            detectResult = duelImageController.detectConfirm()
//                            if (detectResult != null) {
//                                // change state
//                                state = STATE_NON_DUEL
//
//                                // click
//                                click(detectResult.clickPoint)
//                            }
//
//                            // repeat
//                            macroHandler!!.postDelayed(this, delay)
//                        } else {
//                            // TODO duel ~ endphase -> state = DUEL_START
//                        }
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