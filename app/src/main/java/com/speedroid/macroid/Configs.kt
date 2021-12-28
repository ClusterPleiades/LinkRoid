package com.speedroid.macroid

class Configs {
    companion object {
        // dialog
        const val DIALOG_TYPE_OVERLAY = 0
        const val DIALOG_TYPE_BATTERY = 1
        const val DIALOG_TYPE_ACCESS = 2
        const val DIALOG_TYPE_MODE = 10

        const val DIALOG_POSITION_GATE = 0
        const val DIALOG_POSITION_STOP = 1

        // notification
        const val NOTIFICATION_ID = 1000

        // warning
        const val SCREEN_WIDTH_STANDARD = 1080

        // overlay service
        const val CLICK_TIME_THRESHOLD = 100

        // projection service
        const val PROJECTION_NAME = "projection"

        // macro
        const val IMAGE_WIDTH = 1080
        const val IMAGE_HEIGHT = 290

        const val DELAY_START = 500L
        const val DELAY_INTERVAL = 500L

        const val DISTANCE_THRESHOLD = 3000000L

        const val STATE_NON_DUEL = 0
        const val STATE_DUEL_READY = 1
        const val STATE_DUEL_STANDBY = 2 // count 2 from non duel
        const val STATE_DUEL_START = 3

        // 1080 290
    }


}