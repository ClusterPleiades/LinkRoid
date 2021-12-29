package com.speedroid.macroid.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.speedroid.macroid.Configs.Companion.DIALOG_TYPE_MODE
import com.speedroid.macroid.ui.fragment.dialog.RecyclerDialogFragment

class ModeActivity : AppCompatActivity() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var preservedContext: Context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initialize application context
        preservedContext = this

        RecyclerDialogFragment(DIALOG_TYPE_MODE).show(supportFragmentManager, DIALOG_TYPE_MODE.toString())
    }
}