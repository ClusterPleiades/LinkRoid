package com.speedroid.macroid.service


import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.content.Intent
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import com.speedroid.macroid.Configs.Companion.DURATION_CLICK
import com.speedroid.macroid.Configs.Companion.DURATION_DRAG
import com.speedroid.macroid.Configs.Companion.LENGTH_DRAG


class GestureService : AccessibilityService() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // get coordinate
        val x1 = intent.getIntExtra("x1", 0).toFloat()
        val y1 = intent.getIntExtra("y1", 0).toFloat()
        val isDrag = intent.getBooleanExtra("isDrag", false)
        val duration = if (isDrag) DURATION_DRAG else DURATION_CLICK

        // initialize path
        val path = Path()
        path.moveTo(x1, y1)
        if (isDrag)
            path.lineTo(x1, y1 - LENGTH_DRAG)
        else
            path.lineTo(x1, y1)

        // initialize builder
        val gestureDescriptionBuilder = GestureDescription.Builder()
        gestureDescriptionBuilder.addStroke(StrokeDescription(path, 0, duration))

        // dispatch gesture
        dispatchGesture(gestureDescriptionBuilder.build(), null, null)

        // stop self
        stopSelf()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}
}