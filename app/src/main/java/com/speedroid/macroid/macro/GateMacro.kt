package com.speedroid.macroid.macro

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.speedroid.macroid.Configs.Companion.BOTTOM_LEFT
import com.speedroid.macroid.ImageController
import com.speedroid.macroid.R

class GateMacro(private val context: Context) {
    val imageController = ImageController(context)

    fun startMacro() {
        val handler = Handler(Looper.myLooper()!!)
        handler.postDelayed({
            imageController.findImage(R.drawable.image_gate, BOTTOM_LEFT)
        }, 1000)
    }

}