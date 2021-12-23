package com.speedroid.macroid.ui.fragment.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.speedroid.macroid.Configs.Companion.DIALOG_TYPE_ENABLE_OVERLAY
import com.speedroid.macroid.DeviceController
import com.speedroid.macroid.R

class DefaultDialogFragment(private val type: Int) : androidx.fragment.app.DialogFragment() {

    @SuppressLint("InflateParams", "UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // initialize builder
        val builder = context?.let { AlertDialog.Builder(it) }

        // initialize activity
        val activity: FragmentActivity? = activity
        if (activity != null) {
            // initialize dialog view
            val dialogView: View = activity.layoutInflater.inflate(R.layout.fragment_dialog_default, null)

            // initialize and set message
            val messageTextView = dialogView.findViewById<TextView>(R.id.message_dialog_default)
            when (type) {
                DIALOG_TYPE_ENABLE_OVERLAY -> messageTextView.setText(R.string.dialog_message_overlay)
            }

            // set positive listener
            dialogView.findViewById<View>(R.id.positive_dialog_default).setOnClickListener {
                when (type) {
                    DIALOG_TYPE_ENABLE_OVERLAY -> {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context!!.packageName))
                        startActivity(intent)
                    }
                }
                dismiss()
            }

            // set negative listener
            dialogView.findViewById<View>(R.id.negative_dialog_default).setOnClickListener {
                dismiss()
            }

            // set negative visibility
            val negativeTextView = dialogView.findViewById<TextView>(R.id.negative_dialog_default)
            when (type) {
                DIALOG_TYPE_ENABLE_OVERLAY -> negativeTextView.visibility = View.INVISIBLE
                else -> negativeTextView.visibility = View.VISIBLE
            }

            // set dialog view
            builder!!.setView(dialogView)
        }

        // create dialog
        val dialog: AlertDialog = builder!!.create()

        // set transparent background
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // set canceled on touch outside
        when (type) {
            DIALOG_TYPE_ENABLE_OVERLAY -> {
                dialog.setCanceledOnTouchOutside(false)
            }
            else -> {
                dialog.setCanceledOnTouchOutside(true)
            }
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()

        val width = (DeviceController(context).getWidthMax() * 0.85).toInt()
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog!!.window!!.setLayout(width, height)

        // set cancelable
        when (type) {
            DIALOG_TYPE_ENABLE_OVERLAY -> {
                dialog!!.setCancelable(false)
            }
            else -> {
                dialog!!.setCancelable(true)
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        dismiss()
    }
}