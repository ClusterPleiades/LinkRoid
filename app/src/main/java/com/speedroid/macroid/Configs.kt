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

        const val DELAY_DEFAULT = 500L
        const val DELAY_DOUBLE = 1000L
        const val DELAY_STANDBY = 7000L

        const val THRESHOLD_DISTANCE_DEFAULT = 3000000L
        const val THRESHOLD_DISTANCE_STRICT = 1500000L

        const val DURATION_DRAG = 250L
        const val DURATION_CLICK = 100L

        const val STATE_GATE = 0
        const val STATE_GATE_READY = 1
        const val STATE_GATE_CONV = 2
        const val STATE_GATE_STANDBY = 3
        const val STATE_GATE_START = 4
        const val STATE_GATE_DUEL = 5
        const val STATE_GATE_END = 6
        const val STATE_GATE_FINISH = 7

        const val LENGTH_DRAG = 600
        const val X_CENTER = 540
        const val X_SET = 680
        const val X_PHASE = 1030
        const val Y_FROM_BOTTOM_HAND = 200
        const val Y_FROM_BOTTOM_SUMMON = 500
        const val Y_FROM_BOTTOM_PHASE = 700
        const val Y_FROM_BOTTOM_MONSTER = 1020
        const val Y_FROM_BOTTOM_DECK = 850 // 2400 - 1550
    }
}