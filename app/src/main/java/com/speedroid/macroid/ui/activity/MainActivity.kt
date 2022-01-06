package com.speedroid.macroid.ui.activity

import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.speedroid.macroid.Configs.Companion.DELAY_ENEMY
import com.speedroid.macroid.Configs.Companion.DELAY_ENEMY_DEFAULT
import com.speedroid.macroid.Configs.Companion.DIALOG_TYPE_ACCESS
import com.speedroid.macroid.Configs.Companion.DIALOG_TYPE_BATTERY
import com.speedroid.macroid.Configs.Companion.DIALOG_TYPE_OVERLAY
import com.speedroid.macroid.Configs.Companion.PREFS
import com.speedroid.macroid.Configs.Companion.SCREEN_WIDTH_STANDARD
import com.speedroid.macroid.Configs.Companion.SETTING_POSITION_TIME_OPPONENT
import com.speedroid.macroid.DeviceController
import com.speedroid.macroid.R
import com.speedroid.macroid.macro.mode.BaseMode
import com.speedroid.macroid.service.OverlayService
import com.speedroid.macroid.service.ProjectionService
import com.speedroid.macroid.ui.fragment.dialog.DefaultDialogFragment


class MainActivity : AppCompatActivity() {
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var overlayButton: Button
    private lateinit var warningTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // set navigation color
        window.navigationBarColor = Color.WHITE

        // initialize result launcher
        resultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // start projection service
                startService(ProjectionService.getStartIntent(this, RESULT_OK, result.data))

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
                if (BaseMode.macroHandler != null)
                    BaseMode.macroHandler!!.removeMessages(0)

                // set overlay button text
                overlayButton.setText(R.string.button_overlay_start)
            }
        }

        // initialize warning textview
        warningTextView = findViewById(R.id.text_warning)

        // initialize setting recycler view
        val settingRecyclerView: RecyclerView = findViewById(R.id.recycler_setting)
        settingRecyclerView.setHasFixedSize(true)
        settingRecyclerView.layoutManager = CustomLinearLayoutManager(this)
        settingRecyclerView.adapter = SettingRecyclerAdapter()
    }

    override fun onResume() {
        super.onResume()

        // check overlay permission
        if (!Settings.canDrawOverlays(this)) {
            val defaultDialogFragment = DefaultDialogFragment(DIALOG_TYPE_OVERLAY)
            defaultDialogFragment.show(supportFragmentManager, DIALOG_TYPE_OVERLAY.toString())
        }
        // check battery optimization
        else if (!(getSystemService(POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(packageName)) {
            val defaultDialogFragment = DefaultDialogFragment(DIALOG_TYPE_BATTERY)
            defaultDialogFragment.show(supportFragmentManager, DIALOG_TYPE_BATTERY.toString())
        }
        // check accessibility permission
        else {
            val accessibilityManager = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
            val list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

            var accessible = false
            for (i in list.indices) {
                if (list[i].resolveInfo.serviceInfo.packageName == packageName) {
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
        if (DeviceController(this).getWidthMax() != SCREEN_WIDTH_STANDARD) warningTextView.visibility = View.VISIBLE
        else warningTextView.visibility = View.GONE
    }

    inner class CustomLinearLayoutManager(var context: Context) : LinearLayoutManager(context) {
        override fun canScrollVertically(): Boolean {
            return false
        }
    }

    inner class SettingRecyclerAdapter : RecyclerView.Adapter<SettingRecyclerAdapter.SettingViewHolder>() {
        private val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        private val editor = prefs.edit()
        private val titleArray: Array<String> = resources.getStringArray(R.array.array_title_setting)

        inner class SettingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var titleTextView: TextView = itemView.findViewById(R.id.title_setting)
            var contentsTextView: TextView = itemView.findViewById(R.id.contents_setting)
            var seekBar: SeekBar = itemView.findViewById(R.id.seek_setting)
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): SettingViewHolder {
            val view: View = LayoutInflater.from(viewGroup.context).inflate(R.layout.layout_recycler_setting, viewGroup, false)
            return SettingViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
            // title
            holder.titleTextView.text = titleArray[position]

            when (position) {
                SETTING_POSITION_TIME_OPPONENT -> {
                    // contents
                    var currentDelay = prefs.getLong(DELAY_ENEMY, DELAY_ENEMY_DEFAULT)
                    holder.contentsTextView.text = "$currentDelay ms"

                    // seek bar
                    holder.seekBar.max = 4 // 0 1 2 3 4
                    holder.seekBar.progress = ((currentDelay - 6000L) / 1000L).toInt()
                    holder.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                        override fun onStopTrackingTouch(seekBar: SeekBar) {}
                        override fun onStartTrackingTouch(seekBar: SeekBar) {}
                        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                            editor.putLong(DELAY_ENEMY, 6000L + progress * 1000L)
                            editor.apply()

                            currentDelay = prefs.getLong(DELAY_ENEMY, DELAY_ENEMY_DEFAULT)
                            holder.contentsTextView.text = "$currentDelay ms"
                        }
                    })
                }
            }
        }

        override fun getItemCount(): Int {
            return titleArray.size
        }
    }

}