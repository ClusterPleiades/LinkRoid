package com.speedroid.macroid.macro.mode

import android.graphics.Bitmap
import android.graphics.Point
import com.speedroid.macroid.Configs.Companion.DELAY_DEFAULT
import com.speedroid.macroid.Configs.Companion.STATE_DICE_END
import com.speedroid.macroid.Configs.Companion.STATE_DICE_READY
import com.speedroid.macroid.Configs.Companion.STATE_DICE_USUAL
import com.speedroid.macroid.macro.image.DiceImageController
import com.speedroid.macroid.service.ProjectionService

class DiceMode : BaseMode() {
    private val diceImageController = DiceImageController()

    private val dicePoint = Point(1080 * 3 / 4, screenHeight - 500)
    private val autoPoint = Point(1080 * 3 / 4, screenHeight - 250)

    private var state = STATE_DICE_USUAL
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
                        var detectResult = diceImageController.detectRetryImage(scaledBitmap)
                        if (detectResult == null) {
                            when (state) {
                                STATE_DICE_USUAL -> {
                                    // detect difficulty
                                    detectResult = diceImageController.detectDifficultyImage(scaledBitmap)
                                    if (detectResult == null)
                                        click(dicePoint)
                                    else {
                                        click(detectResult.clickPoint)
                                        state = STATE_DICE_READY
                                    }
                                }
                                STATE_DICE_READY -> {
                                    // detect win
                                    detectResult = diceImageController.detectWinImage(scaledBitmap)
                                    if (detectResult == null)
                                        click(autoPoint)
                                    else {
                                        click(detectResult.clickPoint)
                                        backupClickPoint = detectResult.clickPoint
                                        state = STATE_DICE_END
                                    }
                                }
                                STATE_DICE_END -> {
                                    // detect conversation
                                    detectResult = diceImageController.detectConvImage(scaledBitmap)
                                    if (detectResult == null)
                                        click(backupClickPoint)
                                    else
                                        state = STATE_DICE_USUAL
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