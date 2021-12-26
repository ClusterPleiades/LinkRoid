package com.speedroid.macroid

class Configs {
    companion object {
        // warning
        const val WIDTH_THRESHOLD = 1080

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
        const val PHASE_NON_DUEL = 0
        const val PHASE_DUEL = 1

        const val imageWidth = 100
        const val imageHeight = 40

        const val DELAY_START = 500L
        const val SIMILARITY_THRESHOLD = 0.98
    }
}