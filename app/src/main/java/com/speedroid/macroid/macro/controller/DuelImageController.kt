package com.speedroid.macroid.macro.controller

import com.speedroid.macroid.ui.activity.SplashActivity.Companion.preservedContext

class DuelImageController {
    val statusBarResId = preservedContext.resources.getIdentifier("status_bar_height", "dimen", "android")
    val statusBarHeight = preservedContext.resources.getDimensionPixelSize(statusBarResId)
}