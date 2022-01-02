package com.speedroid.macroid.macro.mode

import android.graphics.Bitmap
import android.graphics.Point
import android.os.SystemClock
import android.util.Log
import com.speedroid.macroid.Configs
import com.speedroid.macroid.Configs.Companion.DELAY_DEFAULT
import com.speedroid.macroid.Configs.Companion.DELAY_LONG
import com.speedroid.macroid.Configs.Companion.DELAY_SPONGEBOB
import com.speedroid.macroid.Configs.Companion.STATE_DICE_DUEL
import com.speedroid.macroid.Configs.Companion.STATE_DICE_END
import com.speedroid.macroid.Configs.Companion.STATE_DICE_READY
import com.speedroid.macroid.Configs.Companion.STATE_DICE_STANDBY
import com.speedroid.macroid.Configs.Companion.STATE_DICE_USUAL
import com.speedroid.macroid.Configs.Companion.THRESHOLD_TIME_STANDBY
import com.speedroid.macroid.macro.image.DiceImageController
import com.speedroid.macroid.service.ProjectionService

class DiceMode : BaseMode() {
    private val diceImageController = DiceImageController()

    private val dicePoint = Point(1080 * 3 / 4, screenHeight - 500)
    private val autoPoint = Point(1080 * 3 / 4, screenHeight - 250)

    private var state = STATE_DICE_USUAL
    private var time = 0L
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

                    if (scaledBitmap != null) {
                        // detect retry
                        var detectResult = diceImageController.detectRetryImage(scaledBitmap)
                        if (detectResult == null) {
                            when (state) {
                                STATE_DICE_USUAL -> {
                                    Log.d("test", "usual")
                                    // detect conversation
                                    detectResult = diceImageController.detectConvImage(scaledBitmap)
                                    if (detectResult == null) {
                                        // detect move
                                        detectResult = diceImageController.detectMoveImage(scaledBitmap)
                                        if (detectResult == null) click(dicePoint)
                                        else click(detectResult.clickPoint)
                                    } else {
                                        if (SystemClock.elapsedRealtime() - time > DELAY_SPONGEBOB) {
                                            click(detectResult.clickPoint)
                                            state = STATE_DICE_READY
                                        } else
                                            click(dicePoint)
                                    }
                                }
                                STATE_DICE_READY -> {
                                    Log.d("test", "ready")
                                    // detect back
                                    detectResult = diceImageController.detectBackImage(scaledBitmap)
                                    if (detectResult == null) {
                                        // detect difficulty
                                        detectResult = diceImageController.detectDifficultyImage(scaledBitmap)
                                        if (detectResult == null) click(dicePoint)
                                        else click(detectResult.clickPoint)
                                    } else state = STATE_DICE_STANDBY
                                }
                                STATE_DICE_STANDBY -> {
                                    Log.d("test", "standby")
                                    // detect back
                                    detectResult = diceImageController.detectBackImage(scaledBitmap)
                                    if (detectResult != null) {
                                        click(autoPoint)
                                        state = STATE_DICE_DUEL
                                        time = SystemClock.elapsedRealtime()
                                    }
                                }
                                STATE_DICE_DUEL -> {
                                    Log.d("test", "duel")
                                    if (SystemClock.elapsedRealtime() - time > THRESHOLD_TIME_STANDBY) {
                                        // detect win
                                        detectResult = diceImageController.detectWinImage(scaledBitmap)
                                        if (detectResult == null) {
                                            click(autoPoint)
                                        } else {
                                            click(detectResult.clickPoint)
                                            backupClickPoint = detectResult.clickPoint
                                            state = STATE_DICE_END
                                        }
                                    } else click(autoPoint)
                                }
                                STATE_DICE_END -> {
                                    Log.d("test", "end")
                                    // detect conversation
                                    detectResult = diceImageController.detectConvImage(scaledBitmap)
                                    click(backupClickPoint)
                                    if (detectResult != null) {
                                        state = STATE_DICE_USUAL
                                        time = 0
                                    }
                                }
                            }
                        } else {
                            click(detectResult.clickPoint)
                        }

                        // repeat
                        macroHandler!!.postDelayed(this, DELAY_DEFAULT)

                        // recycle screen bitmap
                        scaledBitmap.recycle()
                    }
                }
            }
        }.also { mainRunnable = it }

    }
}