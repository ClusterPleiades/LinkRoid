package com.speedroid.macroid.macro.mode

import android.graphics.Bitmap
import android.graphics.Point
import com.speedroid.macroid.Configs.Companion.DELAY_DEFAULT
import com.speedroid.macroid.Configs.Companion.DELAY_DOUBLE
import com.speedroid.macroid.Configs.Companion.DURATION_DRAG
import com.speedroid.macroid.Configs.Companion.STATE_GATE_D
import com.speedroid.macroid.Configs.Companion.STATE_GATE_D_CONV
import com.speedroid.macroid.Configs.Companion.STATE_GATE_D_START
import com.speedroid.macroid.Configs.Companion.STATE_GATE_D_READY
import com.speedroid.macroid.Configs.Companion.STATE_GATE_D_STANDBY
import com.speedroid.macroid.Configs.Companion.DELAY_STANDBY
import com.speedroid.macroid.Configs.Companion.DURATION_CLICK
import com.speedroid.macroid.Configs.Companion.STATE_GATE_D_DUEL
import com.speedroid.macroid.Configs.Companion.STATE_GATE_D_END
import com.speedroid.macroid.Configs.Companion.STATE_GATE_D_FINISH
import com.speedroid.macroid.Configs.Companion.X_CENTER
import com.speedroid.macroid.Configs.Companion.X_PHASE
import com.speedroid.macroid.Configs.Companion.X_SET
import com.speedroid.macroid.Configs.Companion.Y_FROM_BOTTOM_HAND
import com.speedroid.macroid.Configs.Companion.Y_FROM_BOTTOM_MONSTER
import com.speedroid.macroid.Configs.Companion.Y_FROM_BOTTOM_PHASE
import com.speedroid.macroid.Configs.Companion.Y_FROM_BOTTOM_SUMMON
import com.speedroid.macroid.R
import com.speedroid.macroid.macro.image.BaseImageController
import com.speedroid.macroid.service.ProjectionService

class GateDMode : BaseMode() {
    private val imageController: BaseImageController = BaseImageController()
    private val duelRunnableArrayList: ArrayList<Runnable> = ArrayList()

    private var state = STATE_GATE_D
    private var backupClickPoint: Point? = null
    private var turnCount = 0

    init {
        initializeMainRunnable()
        initializeDuelRunnableArrayList()
    }

    private fun initializeMainRunnable() {
        object : Runnable {
            override fun run() {
                val screenBitmap = ProjectionService.getScreenProjection()
                if (screenBitmap == null)
                    macroHandler!!.postDelayed(this, DELAY_DEFAULT)
                else {
                    val scaledBitmap = if (screenBitmap.width == screenWidth) screenBitmap
                    else Bitmap.createScaledBitmap(screenBitmap, screenWidth, screenHeight, true)

                    if (scaledBitmap == null)
                        macroHandler!!.postDelayed(this, DELAY_DEFAULT)
                    else {
                        // detect retry image
                        val detectResult = imageController.detectRetryImage(scaledBitmap)
                        if (detectResult == null) runMainLogic(scaledBitmap)
                        else {
                            click(detectResult.clickPoint)
                            macroHandler!!.postDelayed(this, DELAY_DEFAULT)
                        }
                    }

                    screenBitmap.recycle()
                    scaledBitmap.recycle()
                }
            }
        }.also { mainRunnable = it }
    }

    private fun runMainLogic(bitmap: Bitmap) {
        when (state) {
            STATE_GATE_D -> {
                var detectResult = imageController.detectImage(bitmap, R.drawable.image_button_appear)
                if (detectResult == null) {
                    detectResult = imageController.detectImage(bitmap, R.drawable.image_button_gate)
                    if (detectResult == null) click(imageController.clickPointHashMap[R.drawable.image_button_gate])
                    else {
                        click(detectResult.clickPoint)
                        state = STATE_GATE_D_READY
                    }
                } else click(detectResult.clickPoint)
                macroHandler!!.postDelayed(mainRunnable, DELAY_DOUBLE)
            }
            STATE_GATE_D_READY -> {
                val detectResult = imageController.detectImage(bitmap, R.drawable.image_button_back)
                if (detectResult == null) macroHandler!!.postDelayed(mainRunnable, DELAY_DEFAULT)
                else {
                    click(detectResult.clickPoint)
                    state = STATE_GATE_D_CONV
                    macroHandler!!.postDelayed(mainRunnable, DELAY_DOUBLE)
                }
            }
            STATE_GATE_D_CONV -> {
                val detectResult = imageController.detectImage(bitmap, R.drawable.image_background_conv)
                if (detectResult == null) macroHandler!!.postDelayed(mainRunnable, DELAY_DEFAULT)
                else {
                    click(detectResult.clickPoint)
                    backupClickPoint = detectResult.clickPoint
                    state = STATE_GATE_D_STANDBY
                    macroHandler!!.postDelayed(mainRunnable, DELAY_DOUBLE)
                }
            }
            STATE_GATE_D_STANDBY -> {
                val detectResult = imageController.detectImage(bitmap, R.drawable.image_button_back)
                if (detectResult == null) {
                    click(backupClickPoint)
                    macroHandler!!.postDelayed(mainRunnable, DELAY_DOUBLE)
                } else {
                    click(detectResult.clickPoint)
                    state = STATE_GATE_D_START
                    macroHandler!!.postDelayed(mainRunnable, DELAY_DOUBLE)
                }
            }
            STATE_GATE_D_START -> {
                state = STATE_GATE_D_DUEL
                turnCount = 1
                macroHandler!!.postDelayed(mainRunnable, DELAY_STANDBY)
            }
            STATE_GATE_D_DUEL -> {
                macroHandler!!.postDelayed(duelRunnableArrayList[0], DELAY_DEFAULT)
            }
            STATE_GATE_D_END -> {
                val detectResult = imageController.detectImage(bitmap, R.drawable.image_background_conv)
                if (detectResult == null) click(backupClickPoint)
                else {
                    click(detectResult.clickPoint)
                    state = STATE_GATE_D_FINISH
                }
                macroHandler!!.postDelayed(mainRunnable, DELAY_DEFAULT)
            }
            STATE_GATE_D_FINISH -> {
                val detectResult = imageController.detectImage(bitmap, R.drawable.image_background_conv)
                if (detectResult == null) state = STATE_GATE_D
                else click(detectResult.clickPoint)
                macroHandler!!.postDelayed(mainRunnable, DELAY_DEFAULT)
            }
        }
    }

    private fun initializeDuelRunnableArrayList() {
        // index 0: draw
        Runnable {
            val screenBitmap = ProjectionService.getScreenProjection()
            if (screenBitmap == null) macroHandler!!.postDelayed(duelRunnableArrayList[0], DELAY_DEFAULT)
            else {
                val scaledBitmap = if (screenBitmap.width == screenWidth) screenBitmap
                else Bitmap.createScaledBitmap(screenBitmap, screenWidth, screenHeight, true)

                // detect win
                var detectResult = if (turnCount > 1) imageController.detectImage(scaledBitmap, R.drawable.image_button_win) else null
                if (detectResult == null) {
                    click(Point(X_CENTER, screenHeight / 2))
                    detectResult = imageController.detectDeckImage(scaledBitmap)
                    when (detectResult.drawableResId) {
                        R.drawable.image_background_player -> {
                            if (turnCount > 3 && turnCount % 2 == 0) macroHandler!!.postDelayed(duelRunnableArrayList[7], DELAY_DEFAULT)
                            else macroHandler!!.postDelayed(duelRunnableArrayList[1], DELAY_DEFAULT)
                        }
                        R.drawable.image_background_enemy,
                        R.drawable.image_background_draw_1,
                        R.drawable.image_background_draw_2 -> macroHandler!!.postDelayed(duelRunnableArrayList[0], DELAY_DEFAULT)
                    }
                } else {
                    click(detectResult.clickPoint)
                    backupClickPoint = detectResult.clickPoint
                    state = STATE_GATE_D_END
                    macroHandler!!.postDelayed(mainRunnable, DELAY_DOUBLE)
                }

                scaledBitmap.recycle()
                screenBitmap.recycle()
            }
        }.also { duelRunnableArrayList.add(it) }

        // index 1: drag monster
        Runnable {
            drag(Point(X_CENTER, screenHeight - Y_FROM_BOTTOM_HAND))
            macroHandler!!.postDelayed(duelRunnableArrayList[2], 2 * DURATION_DRAG)
        }.also { duelRunnableArrayList.add(it) }

        // index 2: set monster
        Runnable {
            click(Point(X_SET, screenHeight - Y_FROM_BOTTOM_SUMMON))
            macroHandler!!.postDelayed(duelRunnableArrayList[3], DELAY_DOUBLE)
        }.also { duelRunnableArrayList.add(it) }

        // index 3: click phase
        Runnable {
            click(Point(X_PHASE, screenHeight - Y_FROM_BOTTOM_PHASE))
            macroHandler!!.postDelayed(duelRunnableArrayList[4], DELAY_DEFAULT)
        }.also { duelRunnableArrayList.add(it) }

        // index 4: click phase (battle)
        Runnable {
            click(Point(X_PHASE, screenHeight - Y_FROM_BOTTOM_PHASE))
            macroHandler!!.postDelayed(duelRunnableArrayList[5], DELAY_DEFAULT)
        }.also { duelRunnableArrayList.add(it) }

        // index 5: click phase
        Runnable {
            click(Point(X_PHASE, screenHeight - Y_FROM_BOTTOM_PHASE))
            macroHandler!!.postDelayed(duelRunnableArrayList[6], DELAY_DEFAULT)
        }.also { duelRunnableArrayList.add(it) }

        // index 6: click phase (end)
        Runnable {
            click(Point(X_PHASE, screenHeight - Y_FROM_BOTTOM_PHASE))
            turnCount++
            macroHandler!!.postDelayed(mainRunnable, DELAY_DEFAULT)
        }.also { duelRunnableArrayList.add(it) }

        // case exception
        // index 7: click monster
        Runnable {
            click(Point(X_CENTER, screenHeight - Y_FROM_BOTTOM_MONSTER))
            macroHandler!!.postDelayed(duelRunnableArrayList[8], DELAY_DEFAULT + DURATION_CLICK)
        }.also { duelRunnableArrayList.add(it) }

        // index 8: def to atk
        Runnable {
            click(Point(X_CENTER, screenHeight - Y_FROM_BOTTOM_SUMMON))
            macroHandler!!.postDelayed(duelRunnableArrayList[3], DELAY_DOUBLE)
        }.also { duelRunnableArrayList.add(it) }
    }
}