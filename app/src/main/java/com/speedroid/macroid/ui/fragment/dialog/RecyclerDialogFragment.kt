package com.speedroid.macroid.ui.fragment.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.speedroid.macroid.Configs.Companion.DIALOG_POSITION_GATE
import com.speedroid.macroid.Configs.Companion.DIALOG_POSITION_STOP
import com.speedroid.macroid.Configs.Companion.DIALOG_TYPE_MODE
import com.speedroid.macroid.DeviceController
import com.speedroid.macroid.R
import com.speedroid.macroid.macro.mode.BaseMode
import com.speedroid.macroid.macro.mode.GateMode
import com.speedroid.macroid.service.OverlayService
import com.speedroid.macroid.service.ProjectionService
import com.speedroid.macroid.ui.activity.ModeActivity.Companion.preservedContext
import java.util.*

class RecyclerDialogFragment(private val type: Int) : androidx.fragment.app.DialogFragment() {

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // initialize builder
        val builder = context?.let { AlertDialog.Builder(it) }

        // initialize activity
        val activity: FragmentActivity? = activity
        if (activity != null) {
            // initialize dialog view
            val dialogView: View = activity.layoutInflater.inflate(R.layout.fragment_dialog_recycler, null)

            // initialize recycler view
            val dialogRecyclerView: RecyclerView = dialogView.findViewById(R.id.recycler_dialog)
            dialogRecyclerView.setHasFixedSize(true)
            dialogRecyclerView.layoutManager = LinearLayoutManager(context)
            dialogRecyclerView.adapter = DialogRecyclerAdapter()

            // set dialog view
            builder!!.setView(dialogView)
        }

        // create dialog
        val dialog: AlertDialog = builder!!.create()

        // set transparent background
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // set canceled on touch outside
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }

    override fun onStart() {
        super.onStart()

        val width = (DeviceController(context).getWidthMax() * 0.65).toInt()
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog!!.window!!.setLayout(width, height)

        dialog!!.setOnDismissListener {
            destroy()
        }
        dialog!!.setOnCancelListener {
            destroy()
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        dismiss()

        // set clickable true
        OverlayService.isClickable = true
    }

    private fun destroy() {
        if (type == DIALOG_TYPE_MODE) {
            val parentActivity: Activity? = activity
            parentActivity?.finish()
        }

        // set clickable true
        OverlayService.isClickable = true
    }

    inner class DialogRecyclerAdapter : RecyclerView.Adapter<DialogRecyclerAdapter.DialogViewHolder>() {
        private lateinit var textArray: Array<String>

        inner class DialogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var textView: TextView = itemView.findViewById(R.id.message_dialog_recycler)

            init {
                itemView.setOnClickListener {
                    // case error
                    if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener

                    when (type) {
                        DIALOG_TYPE_MODE -> {
                            when (adapterPosition) {
                                DIALOG_POSITION_GATE -> {
                                    // start macro
                                    GateMode().startMacro()
                                }
                                DIALOG_POSITION_STOP -> {
                                    // stop overlay service
                                    preservedContext.stopService(Intent(preservedContext, OverlayService::class.java))

                                    // stop projection service
                                    preservedContext.startService(ProjectionService.getStopIntent(preservedContext))

                                    // stop handler
                                    if (BaseMode.macroHandler != null)
                                        BaseMode.macroHandler!!.removeMessages(0)
                                }
                            }
                        }
                    }

                    // dismiss dialog
                    dismiss()

                    // destroy activity
                    destroy()
                }
            }
        }

        // constructor
        init {
            if (type == DIALOG_TYPE_MODE) {
                textArray = context!!.resources.getStringArray(R.array.array_mode)
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DialogViewHolder {
            val view: View = LayoutInflater.from(viewGroup.context).inflate(R.layout.layout_recycler_dialog, viewGroup, false)
            return DialogViewHolder(view)
        }

        override fun onBindViewHolder(holder: DialogViewHolder, position: Int) {
            // set text view
            holder.textView.text = textArray[position]

            // set text color
            val colorResId = if (position == DIALOG_POSITION_STOP) R.color.color_red else R.color.color_dark_gray
            holder.textView.setTextColor(ContextCompat.getColor(context!!, colorResId))
        }

        override fun getItemCount(): Int {
            return textArray.size
        }
    }
}