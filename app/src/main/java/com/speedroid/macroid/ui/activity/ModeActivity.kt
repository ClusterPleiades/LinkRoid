package com.speedroid.macroid.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.speedroid.macroid.Configs.Companion.DIALOG_TYPE_MODE
import com.speedroid.macroid.ui.fragment.dialog.RecyclerDialogFragment

class ModeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RecyclerDialogFragment(DIALOG_TYPE_MODE).show(supportFragmentManager, DIALOG_TYPE_MODE.toString())
    }
}