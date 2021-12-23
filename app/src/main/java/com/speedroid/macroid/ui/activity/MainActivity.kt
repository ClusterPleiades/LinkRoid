package com.speedroid.macroid.ui.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.speedroid.macroid.Configs.Companion.DIALOG_TYPE_OVERLAY
import com.speedroid.macroid.R
import com.speedroid.macroid.service.MacroidService
import com.speedroid.macroid.ui.fragment.dialog.DefaultDialogFragment

class MainActivity : AppCompatActivity() {
    lateinit var overlayButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        overlayButton = findViewById(R.id.button_overlay)
        overlayButton.setOnClickListener {
            // check overlay permission
            if (!Settings.canDrawOverlays(this))
                requestOverlayPermission()
            else {
                if (!MacroidService.isOverlaid) {
                    // start service
                    startService(Intent(this, MacroidService::class.java))
                    overlayButton.setText(R.string.button_overlay_stop)
                } else {
                    // stop service
                    stopService(Intent(this, MacroidService::class.java))
                    overlayButton.setText(R.string.button_overlay_start)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // check overlay permission
        if (!Settings.canDrawOverlays(this)) requestOverlayPermission()
    }

    private fun requestOverlayPermission() {
        // show overlay dialog
        val defaultDialogFragment = DefaultDialogFragment(DIALOG_TYPE_OVERLAY)
        defaultDialogFragment.show(supportFragmentManager, DIALOG_TYPE_OVERLAY.toString())
    }
}