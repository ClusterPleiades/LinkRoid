package com.speedroid.macroid.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.PixelFormat
import android.os.IBinder
import android.os.SystemClock
import android.view.*
import com.speedroid.macroid.Configs.Companion.CLICK_TIME_THRESHOLD
import com.speedroid.macroid.Configs.Companion.NOTIFICATION_ID
import com.speedroid.macroid.DeviceController
import com.speedroid.macroid.NotificationController
import com.speedroid.macroid.R
import com.speedroid.macroid.macro.mode.BaseMode
import com.speedroid.macroid.ui.activity.ModeActivity

class OverlayService : Service() {
    companion object {
        var isOverlaid: Boolean = false
        var isClickable: Boolean = true
    }

    private lateinit var deviceController: DeviceController

    private lateinit var buttonView: View
    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var windowManager: WindowManager

    var touchDownTime: Long = 0

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        // initialize device controller
        deviceController = DeviceController(this)

        // start foreground with notification
        startForeground(NOTIFICATION_ID, NotificationController(this).initializeNotification())

        // initialize service
        initializeService()

        // set isOverlaid true
        isOverlaid = true
    }

    override fun onDestroy() {
        // remove view
        windowManager.removeView(buttonView)

        // set isOverlaid false
        isOverlaid = false
    }

    @SuppressLint("InflateParams")
    private fun initializeService() {
        // check already overlaid
        if (isOverlaid)
            return

        // initialize button view
        buttonView = (getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.layout_overlay, null)

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

        // initialize window manager
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(buttonView, layoutParams)

        // set button view click listener
        buttonView.setOnClickListener {
            if (isClickable) {
                // set clickable false
                isClickable = false

                // stop handler
                if (BaseMode.macroHandler != null)
                    BaseMode.macroHandler!!.removeMessages(0)

                // start mode dialog as activity
                val intent = Intent(this, ModeActivity::class.java)
                startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK))
            }
        }

        // set button view touch listener
        buttonView.setOnTouchListener { view: View, motionEvent: MotionEvent ->
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
    }
}