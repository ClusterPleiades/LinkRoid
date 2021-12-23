package com.speedroid.macroid.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import androidx.appcompat.widget.LinearLayoutCompat
import com.speedroid.macroid.Configs.Companion.NOTIFICATION_ID_FOREGROUND
import com.speedroid.macroid.NotificationController
import com.speedroid.macroid.R

class MacroidService : Service() {
    companion object {
        var isOverlaid: Boolean = false
    }

    private lateinit var windowManager: WindowManager
    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var layout: LinearLayoutCompat
    private lateinit var view: View

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

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

        // initialize view
        view = (getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.layout_overlay, null)

        // initialize layout
        layout = view.findViewById(R.id.layout_overlay)
        layout.setOnClickListener {
            // TODO
        }

        // initialize window manager
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(view, layoutParams)
    }
}