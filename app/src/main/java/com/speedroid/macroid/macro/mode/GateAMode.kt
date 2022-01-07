package com.speedroid.macroid.macro.mode

import android.graphics.Bitmap
import android.graphics.Point
import android.os.SystemClock
import com.speedroid.macroid.Configs.Companion.DELAY_DEFAULT
import com.speedroid.macroid.Configs.Companion.DELAY_ENEMY
import com.speedroid.macroid.Configs.Companion.DELAY_ENEMY_DEFAULT
import com.speedroid.macroid.Configs.Companion.DELAY_LONG
import com.speedroid.macroid.Configs.Companion.DELAY_VERY_LONG
import com.speedroid.macroid.Configs.Companion.DURATION_DRAG
import com.speedroid.macroid.Configs.Companion.STATE_GATE_A_END
import com.speedroid.macroid.Configs.Companion.STATE_GATE_A_STANDBY
import com.speedroid.macroid.Configs.Companion.STATE_GATE_A_DUEL
import com.speedroid.macroid.Configs.Companion.STATE_GATE_A_READY
import com.speedroid.macroid.Configs.Companion.STATE_GATE_A_USUAL
import com.speedroid.macroid.Configs.Companion.DELAY_DRAW
import com.speedroid.macroid.Configs.Companion.THRESHOLD_STANDBY
import com.speedroid.macroid.Configs.Companion.X_CENTER
import com.speedroid.macroid.Configs.Companion.X_MONSTER_RIGHT
import com.speedroid.macroid.Configs.Companion.X_PHASE
import com.speedroid.macroid.Configs.Companion.X_SUMMON
import com.speedroid.macroid.Configs.Companion.Y_FROM_BOTTOM_HAND
import com.speedroid.macroid.Configs.Companion.Y_FROM_BOTTOM_MONSTER
import com.speedroid.macroid.Configs.Companion.Y_FROM_BOTTOM_PHASE
import com.speedroid.macroid.Configs.Companion.Y_FROM_BOTTOM_SUMMON
import com.speedroid.macroid.R
import com.speedroid.macroid.macro.image.GateAImageController
import com.speedroid.macroid.service.ProjectionService

class GateAMode : BaseMode() {
    private val enemyDelay = prefs.getLong(DELAY_ENEMY, DELAY_ENEMY_DEFAULT)

    private val gateAImageController: GateAImageController = GateAImageController()
    private val duelRunnableArrayList: ArrayList<Runnable> = ArrayList()

    private var state = STATE_GATE_A_USUAL
    private var time = 0L
    private var turn = 0
    private var backupClickPoint: Point? = null

    init {
        // initialize main runnable
        object : Runnable {
            override fun run() {
                // initialize screen bitmap
                val screenBitmap = ProjectionService.getScreenProjection()
                if (screenBitmap == null)
                    macroHandler!!.postDelayed(this, DELAY_DEFAULT)
                else {
                    // initialize scaled bitmap
                    val scaledBitmap = if (screenBitmap.width != screenWidth) Bitmap.createScaledBitmap(screenBitmap, screenWidth, screenHeight, true) else screenBitmap

                    // recycle origin screen bitmap
                    screenBitmap.recycle()

                    if (scaledBitmap == null)
                        macroHandler!!.postDelayed(this, DELAY_DEFAULT)
                    else {
                        // detect retry
                        var detectResult = gateAImageController.detectRetryImage(scaledBitmap)
                        if (detectResult == null) {
                            when (state) {
                                STATE_GATE_A_USUAL, STATE_GATE_A_READY -> {
                                    // detect image
                                    detectResult = gateAImageController.detectImage(scaledBitmap)
                                    if (detectResult != null) {
                                        // click
                                        click(detectResult.clickPoint)
                                        backupClickPoint = detectResult.clickPoint

                                        // change state
                                        if (detectResult.drawableResId == R.drawable.image_button_back) {
                                            state++
                                            if (state == STATE_GATE_A_STANDBY) time = SystemClock.elapsedRealtime()
                                        }
                                    }

                                    // repeat
                                    macroHandler!!.postDelayed(this, DELAY_DEFAULT)
                                }
                                STATE_GATE_A_STANDBY -> {
                                    // change state
                                    if (SystemClock.elapsedRealtime() - time > THRESHOLD_STANDBY + enemyDelay) {
                                        state = STATE_GATE_A_DUEL
                                        turn = 0
                                    } else {
                                        click(backupClickPoint)
                                    }

                                    // repeat
                                    macroHandler!!.postDelayed(this, DELAY_DEFAULT)
                                }
                                STATE_GATE_A_DUEL -> {
                                    // detect win
                                    detectResult = gateAImageController.detectImage(scaledBitmap, R.drawable.image_button_win)
                                    if (detectResult == null) {
                                        if (turn == 3) {
                                            // repeat
                                            macroHandler!!.postDelayed(this, DELAY_DEFAULT)
                                        } else {
                                            // count turn
                                            turn++

                                            // run duel
                                            macroHandler!!.post(duelRunnableArrayList[0])
                                        }
                                    } else {
                                        // click
                                        click(detectResult.clickPoint)
                                        backupClickPoint = detectResult.clickPoint

                                        // change state
                                        state = STATE_GATE_A_END

                                        // repeat
                                        macroHandler!!.postDelayed(this, DELAY_DEFAULT)
                                    }
                                }
                                STATE_GATE_A_END -> {
                                    // detect image
                                    detectResult = gateAImageController.detectBottomImage(scaledBitmap)
                                    if (detectResult == null) {
                                        // click
                                        click(backupClickPoint)
                                    } else {
                                        // click
                                        click(detectResult.clickPoint)

                                        // change state
                                        if (detectResult.drawableResId == R.drawable.image_background_conv)
                                            state = STATE_GATE_A_USUAL
                                    }

                                    // repeat
                                    macroHandler!!.postDelayed(this, DELAY_DEFAULT)
                                }
                            }
                        } else {
                            // retry
                            click(detectResult.clickPoint)
                            time = SystemClock.elapsedRealtime()
                            macroHandler!!.postDelayed(this, DELAY_DEFAULT)
                        }

                        // recycle screen bitmap
                        scaledBitmap.recycle()
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
            if (SystemClock.elapsedRealtime() - time > DELAY_DRAW)
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
                macroHandler!!.postDelayed(mainRunnable, DELAY_DEFAULT)
            else
                macroHandler!!.postDelayed(mainRunnable, enemyDelay)
        }.also { duelRunnableArrayList.add(it) }
    }
}