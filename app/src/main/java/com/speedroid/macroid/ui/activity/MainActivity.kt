package com.speedroid.macroid.ui.activity

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.speedroid.macroid.Configs.Companion.DIALOG_TYPE_OVERLAY
import com.speedroid.macroid.Configs.Companion.WIDTH_THRESHOLD
import com.speedroid.macroid.DeviceController
import com.speedroid.macroid.R
import com.speedroid.macroid.service.OverlayService
import com.speedroid.macroid.service.ProjectionService
import com.speedroid.macroid.ui.fragment.dialog.DefaultDialogFragment
import android.accessibilityservice.AccessibilityServiceInfo

import android.view.accessibility.AccessibilityManager
import com.speedroid.macroid.Configs.Companion.DIALOG_TYPE_ACCESS
import com.speedroid.macroid.macro.GateMacro


class MainActivity : AppCompatActivity() {
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var overlayButton: Button
    private lateinit var warningTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // initialize result launcher
        resultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // start projection service
                startService(ProjectionService.getStartIntent(this, RESULT_OK, result.data));

                // start overlay service
                startService(Intent(this, OverlayService::class.java))

                // set overlay button text
                overlayButton.setText(R.string.button_overlay_stop)

                // finish application to prevent back to application
                finish()
            }
        }

        // initialize overlay button
        overlayButton = findViewById(R.id.button_overlay)
        overlayButton.setOnClickListener {
            // case not overlaid
            if (!OverlayService.isOverlaid) {
                // start projection
                val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                resultLauncher.launch(projectionManager.createScreenCaptureIntent())
            } else {
                // stop overlay service
                stopService(Intent(this, OverlayService::class.java))

                // stop projection service
                startService(ProjectionService.getStopIntent(this))

                // stop handler
                if (GateMacro.macroHandler != null)
                    GateMacro.macroHandler!!.removeMessages(0)

                // set overlay button text
                overlayButton.setText(R.string.button_overlay_start)
            }
        }

        // initialize warning textview
        warningTextView = findViewById(R.id.text_warning)
    }

    override fun onResume() {
        super.onResume()

        // check overlay permission
        if (!Settings.canDrawOverlays(this)) {
            val defaultDialogFragment = DefaultDialogFragment(DIALOG_TYPE_OVERLAY)
            defaultDialogFragment.show(supportFragmentManager, DIALOG_TYPE_OVERLAY.toString())
        }
        // check accessibility permission
        else {
            val accessibilityManager = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
            val list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

            var accessible = false
            for (i in list.indices) {
                if (list[i].resolveInfo.serviceInfo.packageName == application.packageName) {
                    accessible = true
                    break
                }
            }

            if (!accessible) {
                val defaultDialogFragment = DefaultDialogFragment(DIALOG_TYPE_ACCESS)
                defaultDialogFragment.show(supportFragmentManager, DIALOG_TYPE_ACCESS.toString())
            }
        }

        // set overlay button text
        if (OverlayService.isOverlaid) overlayButton.setText(R.string.button_overlay_stop)
        else overlayButton.setText(R.string.button_overlay_start)

        // set warning visibility
        if (DeviceController(this).getWidthMax() >= WIDTH_THRESHOLD) warningTextView.visibility = View.VISIBLE
        else warningTextView.visibility = View.GONE
    }
}