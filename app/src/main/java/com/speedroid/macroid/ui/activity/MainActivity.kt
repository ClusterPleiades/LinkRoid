package com.speedroid.macroid.ui.activity

import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.speedroid.macroid.Configs.Companion.DIALOG_TYPE_OVERLAY
import com.speedroid.macroid.R
import com.speedroid.macroid.ui.fragment.dialog.DefaultDialogFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onResume() {
        super.onResume()

        // check overlay permission
        if (Settings.canDrawOverlays(this)) {
            // TODO
        } else {
            // show overlay dialog
            val defaultDialogFragment = DefaultDialogFragment(DIALOG_TYPE_OVERLAY)
            defaultDialogFragment.show(supportFragmentManager, DIALOG_TYPE_OVERLAY.toString())
        }
    }
}