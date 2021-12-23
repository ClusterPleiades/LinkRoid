package com.speedroid.macroid.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.view.*
import androidx.core.widget.ImageViewCompat
import com.speedroid.macroid.Configs.Companion.CLICK_TIME_THRESHOLD
import com.speedroid.macroid.Configs.Companion.NOTIFICATION_ID_FOREGROUND
import com.speedroid.macroid.DeviceController
import com.speedroid.macroid.NotificationController
import com.speedroid.macroid.R

class MacroidService : Service() {
    companion object {
        var isOverlaid: Boolean = false
    }

    private lateinit var windowManager: WindowManager
    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var view: View
    private lateinit var popupView: View
    private lateinit var deviceController: DeviceController

    var touchDownTime: Long = 0

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        // initialize device controller
        deviceController = DeviceController(this)

        // start foreground with notification
        startForeground(NOTIFICATION_ID_FOREGROUND, NotificationController(this).initializeNotification())

        // initialize service
        initializeService()

        // set isOverlaid true
        isOverlaid = true
    }

    override fun onDestroy() {
        // remove view
        windowManager.removeView(view)

        // set isOverlaid false
        isOverlaid = false
    }

    @SuppressLint("InflateParams")
    private fun initializeService() {
        // check already overlaid
        if (isOverlaid)
            return

        // initialize layout params
        layoutParams = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )
        layoutParams.gravity = Gravity.END

        // initialize view
        view = (getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.layout_overlay, null)

        // set click listener
        view.setOnClickListener {
            var chicken: ImageViewCompat? = view.findViewById(R.id.chicken) as ImageViewCompat

        }

        // set touch listener
        view.setOnTouchListener { view: View, motionEvent: MotionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchDownTime = SystemClock.elapsedRealtime()
                }
                MotionEvent.ACTION_MOVE -> {
                    if (SystemClock.elapsedRealtime() - touchDownTime > CLICK_TIME_THRESHOLD) {
                        layoutParams.y = motionEvent.rawY.toInt() - (deviceController.getHeightMax() / 2)
                        windowManager.updateViewLayout(view, layoutParams)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (SystemClock.elapsedRealtime() - touchDownTime < CLICK_TIME_THRESHOLD)
                        view.performClick()
                }
            }
            true
        }

        // initialize window manager
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(view, layoutParams)
    }
}