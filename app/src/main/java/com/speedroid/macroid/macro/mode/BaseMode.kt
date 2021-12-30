package com.speedroid.macroid.macro.mode

import android.content.Intent
import android.graphics.Point
import android.os.Handler
import com.speedroid.macroid.DeviceController
import com.speedroid.macroid.service.GestureService
import com.speedroid.macroid.ui.activity.ModeActivity.Companion.preservedContext

open class BaseMode {
    companion object {
        var macroHandler: Handler? = null
    }

    private val deviceController: DeviceController = DeviceController(preservedContext)
    open val screenWidth = deviceController.getWidthMax()
    open val screenHeight = deviceController.getHeightMax()

    init {
        // initialize handler
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
}