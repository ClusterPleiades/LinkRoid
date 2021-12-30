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
        const val IMAGE_HEIGHT_LARGE = 800

        const val DELAY_DEFAULT = 500L
        const val DELAY_LONG = 2000L
        const val DELAY_VERY_LONG = 2500L
        const val DELAY_WIN = 3000L
        const val DELAY_ENEMY = 5000L

        const val THRESHOLD_DISTANCE = 4000000L
        const val THRESHOLD_TIME_DRAW = 2000L
        const val THRESHOLD_TIME_STANDBY = 18000L

        const val DURATION_DRAG = 300L
        const val DURATION_CLICK = 100L

        const val STATE_GATE_USUAL = 0
        const val STATE_GATE_READY = 1
        const val STATE_DUEL_STANDBY = 2 // count 2 from non duel
        const val STATE_DUEL_START = 3
        const val STATE_DUEL_END = 4

        const val LENGTH_DRAG = 800
        const val X_CENTER = 540
        const val X_SUMMON = 400
        const val X_PHASE = 1030
        const val X_MONSTER_RIGHT = 785
        const val Y_FROM_BOTTOM_HAND = 200
        const val Y_FROM_BOTTOM_SUMMON = 500
        const val Y_FROM_BOTTOM_PHASE = 700
        const val Y_FROM_BOTTOM_MONSTER = 1020

        // 1080 290
        // 1080 800
    }


}