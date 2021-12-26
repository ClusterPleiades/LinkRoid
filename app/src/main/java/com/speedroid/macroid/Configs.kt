package com.speedroid.macroid

class Configs {
    companion object {
        // dialog
        const val DIALOG_TYPE_OVERLAY = 0
        const val DIALOG_TYPE_ACCESS = 1
        const val DIALOG_TYPE_MODE = 10

        const val DIALOG_POSITION_GATE = 0
        const val DIALOG_POSITION_STOP = 1

        // notification
        const val NOTIFICATION_ID = 1000

        // overlay service
        const val CLICK_TIME_THRESHOLD = 100

        // projection service
        const val PROJECTION_NAME = "projection"

        // macro
        const val IMAGE_WIDTH = 720
        const val IMAGE_HEIGHT = 40
        const val IMAGE_STRIDE = 2

        const val DELAY_START = 500L
        const val DELAY_INTERVAL = 1000L
        const val SIMILARITY_THRESHOLD = 0.7

        const val DRAWABLE_POSITION_GATE = 0
    }
}