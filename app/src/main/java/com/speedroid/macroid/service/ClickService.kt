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
        val x = intent.getIntExtra("x", 0).toFloat()
        val y = intent.getIntExtra("y", 0).toFloat()

        // initialize path
        val path = Path()
        path.moveTo(x, y)
        path.lineTo(x, y)

        // initialize stroke

        // initialize builder
        val gestureDescriptionBuilder = GestureDescription.Builder()
        gestureDescriptionBuilder.addStroke(StrokeDescription(path, 0, 100))

        // dispatch gesture
        dispatchGesture(gestureDescriptionBuilder.build(), null, null)

        // stop self
        stopSelf()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}
}