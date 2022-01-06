package com.speedroid.macroid.macro.mode

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Point
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.speedroid.macroid.Configs
import com.speedroid.macroid.DeviceController
import com.speedroid.macroid.service.GestureService
import com.speedroid.macroid.ui.activity.ModeActivity.Companion.preservedContext

abstract class BaseMode {
    companion object {
        var macroHandler: Handler? = null
    }

    private val deviceController: DeviceController = DeviceController(preservedContext)
    val prefs: SharedPreferences = preservedContext.getSharedPreferences(Configs.PREFS, AppCompatActivity.MODE_PRIVATE)
    val screenWidth = deviceController.getWidthMax()
    val screenHeight = deviceController.getHeightMax()

    open lateinit var mainRunnable: Runnable

    init {
        // initialize handler
        if (macroHandler != null) macroHandler!!.removeMessages(0)
        macroHandler = Handler(preservedContext.mainLooper)
    }

    open fun click(point: Point?) {
        if (point == null)
            return

        val intent = Intent(preservedContext, GestureService::class.java)
        intent.putExtra("x1", point.x)
        intent.putExtra("y1", point.y)
        preservedContext.startService(intent)
    }

    open fun drag(startPoint: Point) {
        val intent = Intent(preservedContext, GestureService::class.java)
        intent.putExtra("x1", startPoint.x)
        intent.putExtra("y1", startPoint.y)
        intent.putExtra("isDrag", true)
        preservedContext.startService(intent)
    }

    open fun startMacro() {
        macroHandler!!.postDelayed(mainRunnable, Configs.DELAY_DEFAULT)
    }
}