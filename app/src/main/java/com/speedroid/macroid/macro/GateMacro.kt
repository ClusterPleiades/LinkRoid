package com.speedroid.macroid.macro

import android.content.Intent
import android.graphics.Point
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import com.speedroid.macroid.Configs.Companion.DELAY_DEFAULT
import com.speedroid.macroid.Configs.Companion.DELAY_ENEMY
import com.speedroid.macroid.Configs.Companion.DELAY_LONG
import com.speedroid.macroid.Configs.Companion.DELAY_MID
import com.speedroid.macroid.Configs.Companion.DELAY_VERY_LONG
import com.speedroid.macroid.Configs.Companion.DELAY_WIN
import com.speedroid.macroid.Configs.Companion.DURATION_DRAG
import com.speedroid.macroid.Configs.Companion.STATE_DUEL_END
import com.speedroid.macroid.Configs.Companion.STATE_DUEL_STANDBY
import com.speedroid.macroid.Configs.Companion.STATE_DUEL_START
import com.speedroid.macroid.Configs.Companion.STATE_GATE_READY
import com.speedroid.macroid.Configs.Companion.STATE_GATE_USUAL
import com.speedroid.macroid.Configs.Companion.THRESHOLD_DISTANCE
import com.speedroid.macroid.Configs.Companion.THRESHOLD_TIME_DRAW
import com.speedroid.macroid.Configs.Companion.THRESHOLD_TIME_STANDBY
import com.speedroid.macroid.Configs.Companion.X_CENTER
import com.speedroid.macroid.Configs.Companion.X_MONSTER_RIGHT
import com.speedroid.macroid.Configs.Companion.X_PHASE
import com.speedroid.macroid.Configs.Companion.X_SUMMON
import com.speedroid.macroid.Configs.Companion.Y_FROM_BOTTOM_HAND
import com.speedroid.macroid.Configs.Companion.Y_FROM_BOTTOM_MONSTER
import com.speedroid.macroid.Configs.Companion.Y_FROM_BOTTOM_PHASE
import com.speedroid.macroid.Configs.Companion.Y_FROM_BOTTOM_SUMMON
import com.speedroid.macroid.DeviceController
import com.speedroid.macroid.R
import com.speedroid.macroid.macro.controller.GateImageController
import com.speedroid.macroid.service.GestureService
import com.speedroid.macroid.ui.activity.ModeActivity.Companion.preservedContext

class GateMacro {
    companion object {
        var macroHandler: Handler? = null
    }

    private val screenHeight = DeviceController(preservedContext).getHeightMax()
    private val gateImageController: GateImageController = GateImageController()
    private val duelRunnableArrayList: ArrayList<Runnable>
    private val mainRunnable: Runnable

    private var state = STATE_GATE_USUAL
    private var time = 0L
    private var turn = 0
    private var backupClickPoint: Point? = null

    init {
        // initialize handler
        macroHandler = Handler(Looper.myLooper()!!)

        // initialize duel runnable array
        duelRunnableArrayList = ArrayList()

        // initialize main runnable
        object : Runnable {
            override fun run() {
                when (state) {
                    STATE_GATE_USUAL, STATE_GATE_READY -> {
                        Log.d("test", "STATE USUAL/READY")
                        // detect image
                        val detectResult = gateImageController.detectImage()
                        if (detectResult != null) {
                            // change state
                            if (detectResult.drawableResId == R.drawable.image_button_duel_1 ||
                                detectResult.drawableResId == R.drawable.image_button_duel_2 ||
                                detectResult.drawableResId == R.drawable.image_button_back
                            ) state++

                            // click
                            click(detectResult.clickPoint)
                        }

                        // record time
                        if (state == STATE_DUEL_STANDBY)
                            time = SystemClock.elapsedRealtime()

                        // repeat
                        macroHandler!!.postDelayed(this, DELAY_MID)
                    }
                    STATE_DUEL_STANDBY -> {
                        Log.d("test", "STATE STANDBY")
                        // detect image
                        val detectResult = gateImageController.detectCenterImage()

                        // case retry
                        if (detectResult.distance < THRESHOLD_DISTANCE) {
                            // reset time
                            time = SystemClock.elapsedRealtime()
                        }

                        // click
                        click(detectResult.clickPoint)

                        // check time
                        if (SystemClock.elapsedRealtime() - time > THRESHOLD_TIME_STANDBY) {
                            // change state
                            state = STATE_DUEL_START
                        }

                        // initialize turn
                        turn = 0

                        // repeat
                        macroHandler!!.postDelayed(this, DELAY_DEFAULT)
                    }
                    STATE_DUEL_START -> {
                        Log.d("test", "STATE START")
                        // detect image
                        val detectResult = gateImageController.detectWinImage()
                        if (detectResult == null) {
                            // count turn
                            turn++

                            // run duel runnable
                            macroHandler!!.post(duelRunnableArrayList[0])
                        } else {
                            // click
                            click(detectResult.clickPoint)

                            // backup win click point
                            backupClickPoint = detectResult.clickPoint

                            // change state
                            state = STATE_DUEL_END

                            // repeat
                            macroHandler!!.postDelayed(this, DELAY_DEFAULT)
                        }
                    }
                    STATE_DUEL_END -> {
                        Log.d("test", "STATE END")
                        // detect image
                        val detectResult = gateImageController.detectImage()
                        if (detectResult != null && detectResult.drawableResId == R.drawable.image_background_dialog) {
                            // click
                            click(detectResult.clickPoint)

                            // change state
                            state = STATE_GATE_USUAL
                        } else {
                            // click
                            click(backupClickPoint)
                        }

                        // repeat
                        macroHandler!!.postDelayed(this, DELAY_DEFAULT)
                    }
                }
            }
        }.also { mainRunnable = it }

        // index 0: draw before
        Runnable {
            click(Point(X_SUMMON, screenHeight - Y_FROM_BOTTOM_SUMMON))
            time = SystemClock.elapsedRealtime()
            macroHandler!!.postDelayed(duelRunnableArrayList[1], DELAY_DEFAULT)
        }.also { duelRunnableArrayList.add(it) }

        // index 1: draw after
        Runnable {
            click(Point(X_SUMMON, screenHeight - Y_FROM_BOTTOM_SUMMON))
            if (SystemClock.elapsedRealtime() - time > THRESHOLD_TIME_DRAW)
                macroHandler!!.postDelayed(duelRunnableArrayList[2], DELAY_DEFAULT)
            else
                macroHandler!!.postDelayed(duelRunnableArrayList[1], DELAY_DEFAULT)
        }.also { duelRunnableArrayList.add(it) }

        // index 2: drag monster
        Runnable {
            drag(Point(X_SUMMON, screenHeight - Y_FROM_BOTTOM_HAND))
            macroHandler!!.postDelayed(duelRunnableArrayList[3], DELAY_DEFAULT + DURATION_DRAG)
        }.also { duelRunnableArrayList.add(it) }

        // index 3: summon monster
        Runnable {
            click(Point(X_SUMMON, screenHeight - Y_FROM_BOTTOM_SUMMON))
            macroHandler!!.postDelayed(duelRunnableArrayList[4], DELAY_LONG)
        }.also { duelRunnableArrayList.add(it) }

        // index 4: click phase
        Runnable {
            click(Point(X_PHASE, screenHeight - Y_FROM_BOTTOM_PHASE))
            macroHandler!!.postDelayed(duelRunnableArrayList[5], DELAY_DEFAULT)
        }.also { duelRunnableArrayList.add(it) }

        // index 5: to battle phase
        Runnable {
            click(Point(X_PHASE, screenHeight - Y_FROM_BOTTOM_PHASE))
            macroHandler!!.postDelayed(duelRunnableArrayList[6], DELAY_DEFAULT)
        }.also { duelRunnableArrayList.add(it) }

        // index 6: attack center
        Runnable {
            drag(Point(X_CENTER, screenHeight - Y_FROM_BOTTOM_MONSTER))
            if (turn == 1)
                macroHandler!!.postDelayed(duelRunnableArrayList[8], DELAY_VERY_LONG)
            else
                macroHandler!!.postDelayed(duelRunnableArrayList[7], DELAY_VERY_LONG)
        }.also { duelRunnableArrayList.add(it) }

        // index 7: attack right
        Runnable {
            drag(Point(X_MONSTER_RIGHT, screenHeight - Y_FROM_BOTTOM_MONSTER))
            macroHandler!!.postDelayed(duelRunnableArrayList[8], DELAY_VERY_LONG)
        }.also { duelRunnableArrayList.add(it) }

        // index 8: click phase
        Runnable {
            click(Point(X_PHASE, screenHeight - Y_FROM_BOTTOM_PHASE))
            macroHandler!!.postDelayed(duelRunnableArrayList[9], DELAY_DEFAULT)
        }.also { duelRunnableArrayList.add(it) }

        // index 9: to end phase
        Runnable {
            click(Point(X_PHASE, screenHeight - Y_FROM_BOTTOM_PHASE))

            if (turn >= 3)
                macroHandler!!.postDelayed(mainRunnable, DELAY_WIN)
            else
                macroHandler!!.postDelayed(mainRunnable, DELAY_ENEMY)
        }.also { duelRunnableArrayList.add(it) }
    }

    fun startMacro() {
        macroHandler!!.postDelayed(mainRunnable, DELAY_DEFAULT)
    }

    private fun click(point: Point?) {
        if (point == null)
            return

        val intent = Intent(preservedContext, GestureService::class.java)
        intent.putExtra("x1", point.x)
        intent.putExtra("y1", point.y)
        preservedContext.startService(intent)
    }

    private fun drag(startPoint: Point) {
        val intent = Intent(preservedContext, GestureService::class.java)
        intent.putExtra("x1", startPoint.x)
        intent.putExtra("y1", startPoint.y)
        intent.putExtra("isDrag", true)
        preservedContext.startService(intent)
    }
}