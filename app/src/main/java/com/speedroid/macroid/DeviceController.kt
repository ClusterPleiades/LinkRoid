package com.speedroid.macroid

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.TypedValue
import android.view.WindowManager

class DeviceController(private val context: Context?) {
    private val windowManager = context!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    fun getWidthMax(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            windowMetrics.bounds.width()
        } else {
            val size = Point()
            windowManager.defaultDisplay.getSize(size)
            size.x
        }
    }

    fun getHeightMax(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            windowMetrics.bounds.height()
        } else {
            val size = Point()
            windowManager.defaultDisplay.getSize(size)
            size.y
        }
    }

    fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), this.context!!.resources.displayMetrics).toInt()
    }
}