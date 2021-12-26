package com.speedroid.macroid.service


import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.content.Intent
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent


class ClickService : AccessibilityService() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // get coordinate
        val x = intent.getIntExtra("x", 0)
        val y = intent.getIntExtra("y", 0)

        // initialize builder
        val builder = GestureDescription.Builder()

        // initialize path
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())

        // initialize stroke
        val strokeDescription = StrokeDescription(path, 0, 500)
        builder.addStroke(strokeDescription)

        // dispatch gesture
        dispatchGesture(builder.build(), null, null)

        // stop self
        stopSelf()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}
}