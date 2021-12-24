package com.speedroid.macroid

class Configs {
    companion object {
        // dialog
        const val DIALOG_TYPE_OVERLAY = 0
        const val DIALOG_TYPE_MODE = 10

        // notification
        const val NOTIFICATION_ID_OVERLAY = 1004
        const val NOTIFICATION_ID_PROJECTION = 1005
        const val NOTIFICATION_CHANNEL_ID = "channel_id_notification"
        const val NOTIFICATION_CHANNEL_NAME = "channel_name_notification"

        // macroid service
        const val CLICK_TIME_THRESHOLD = 100

        // projection service
        const val PROJECTION_NAME = "projection"
    }
}