package com.speedroid.macroid.ui.activity

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.speedroid.macroid.Configs.Companion.DIALOG_TYPE_OVERLAY
import com.speedroid.macroid.R
import com.speedroid.macroid.service.MacroidService
import com.speedroid.macroid.service.ProjectionService
import com.speedroid.macroid.ui.fragment.dialog.DefaultDialogFragment


class MainActivity : AppCompatActivity() {
    private lateinit var overlayButton: Button
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // initialize result launcher
        resultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // start projection service
                startService(ProjectionService.getStartIntent(this, RESULT_OK, result.data));

                // start macroid service
                startService(Intent(this, MacroidService::class.java))

                // set overlay button text
                overlayButton.setText(R.string.button_overlay_stop)
            }
        }

        overlayButton = findViewById(R.id.button_overlay)
        overlayButton.setOnClickListener {
            // check overlay permission
            if (!Settings.canDrawOverlays(this))
                requestOverlayPermission()
            else {
                // case not overlaid
                if (!MacroidService.isOverlaid) {
                    // start projection
                    val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    resultLauncher.launch(projectionManager.createScreenCaptureIntent())
                } else {
                    // stop macroid service
                    stopService(Intent(this, MacroidService::class.java))

                    // stop projection service
                    startService(ProjectionService.getStopIntent(this));

                    // set overlay button text
                    overlayButton.setText(R.string.button_overlay_start)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // check overlay permission
        if (!Settings.canDrawOverlays(this)) requestOverlayPermission()

        // set overlay button text
        if (MacroidService.isOverlaid) overlayButton.setText(R.string.button_overlay_stop)
        else overlayButton.setText(R.string.button_overlay_start)
    }

    private fun requestOverlayPermission() {
        // show overlay dialog
        val defaultDialogFragment = DefaultDialogFragment(DIALOG_TYPE_OVERLAY)
        defaultDialogFragment.show(supportFragmentManager, DIALOG_TYPE_OVERLAY.toString())
    }
}