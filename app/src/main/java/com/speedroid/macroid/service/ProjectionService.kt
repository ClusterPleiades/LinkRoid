package com.speedroid.macroid.service

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.speedroid.macroid.Configs.Companion.NOTIFICATION_ID_PROJECTION
import com.speedroid.macroid.Configs.Companion.PROJECTION_NAME
import com.speedroid.macroid.DeviceController
import com.speedroid.macroid.NotificationController
import java.nio.ByteBuffer
import java.util.*


class ProjectionService : Service() {
    companion object {
        fun getStartIntent(context: Context?, resultCode: Int, data: Intent?): Intent {
            val intent = Intent(context, ProjectionService::class.java)
            intent.putExtra("action", "start")
            intent.putExtra("result_code", resultCode)
            intent.putExtra("data", data)
            return intent
        }

        fun getStopIntent(context: Context?): Intent {
            val intent = Intent(context, ProjectionService::class.java)
            intent.putExtra("action", "stop")
            return intent
        }
    }

    private lateinit var deviceController: DeviceController

    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null
    private var handler: Handler? = null
    private var virtualDisplay: VirtualDisplay? = null

    private var width = 0
    private var height = 0
    private var density = Resources.getSystem().displayMetrics.densityDpi

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        // start handling thread
        object : Thread() {
            override fun run() {
                Looper.prepare()
                handler = Handler(Looper.myLooper()!!)
                Looper.loop()
            }
        }.start()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // case start command
        if ((intent.hasExtra("result_code") && intent.hasExtra("data") && intent.hasExtra("action") && Objects.equals(intent.getStringExtra("action"), "start"))) {
            // start foreground with notification
            startForeground(NOTIFICATION_ID_PROJECTION, NotificationController(this).initializeNotification())

            // start projection
            val resultCode = intent.getIntExtra("result_code", Activity.RESULT_CANCELED)
            val data = intent.getParcelableExtra<Intent>("data")
            startProjection(resultCode, data)
        }
        // case stop command
        else if (intent.hasExtra("action") && Objects.equals(intent.getStringExtra("action"), "stop")) {
            stopProjection()
            stopSelf()
        } else
            stopSelf()
        return START_NOT_STICKY
    }

    private fun startProjection(resultCode: Int, data: Intent?) {
        // initialize media projection manager
        val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        if (mediaProjection == null) {
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data!!)

            if (mediaProjection != null) {
                // initialize virtual display
                initializeVirtualDisplay()

                // register media projection stop callback
                mediaProjection!!.registerCallback(MediaProjectionStopCallback(), handler)
            }
        }
    }

    private fun stopProjection() {
        handler?.post {
            if (mediaProjection != null)
                mediaProjection!!.stop()
        }
    }

    @SuppressLint("WrongConstant")
    private fun initializeVirtualDisplay() {
        // initialize device controller
        deviceController = DeviceController(this)

        // initialize width, height
        width = deviceController.getWidthMax()
        height = deviceController.getHeightMax()

        // start capture reader
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        imageReader!!.setOnImageAvailableListener(ImageAvailableListener(), handler)

        // initialize virtual display
        val virtualDisplayFlag = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
        virtualDisplay = mediaProjection!!.createVirtualDisplay(PROJECTION_NAME, width, height, density, virtualDisplayFlag, imageReader!!.surface, null, handler)
    }

    private inner class ImageAvailableListener : ImageReader.OnImageAvailableListener {
        override fun onImageAvailable(reader: ImageReader?) {
            var bitmap: Bitmap? = null
            try {
                imageReader!!.acquireLatestImage().use { image ->
                    if (image != null) {
                        val planes: Array<Image.Plane> = image.planes
                        val buffer: ByteBuffer = planes[0].buffer
                        val pixelStride: Int = planes[0].pixelStride
                        val rowStride: Int = planes[0].rowStride
                        val rowPadding = rowStride - pixelStride * width

                        // create bitmap
                        bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
                        bitmap!!.copyPixelsFromBuffer(buffer)

                        // TODO?
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (bitmap != null) bitmap!!.recycle()
            }
        }
    }

    private inner class MediaProjectionStopCallback : MediaProjection.Callback() {
        override fun onStop() {
            handler!!.post {
                if (virtualDisplay != null) virtualDisplay!!.release()
                if (imageReader != null) imageReader!!.setOnImageAvailableListener(null, null)
                mediaProjection!!.unregisterCallback(this@MediaProjectionStopCallback)
            }
        }
    }
}