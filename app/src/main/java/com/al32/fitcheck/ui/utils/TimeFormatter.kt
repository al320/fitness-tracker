package com.al32.fitcheck.ui.utils

fun formatRecoveryTime(hoursRemaining: Float): String {
    return when {
        hoursRemaining <= 0f       -> "Ready"
        hoursRemaining < 1f        -> "${(hoursRemaining * 60).toInt()}m left"
        hoursRemaining < 24f       -> "${hoursRemaining.toInt()}h left"
        else                       -> {
            val days = (hoursRemaining / 24).toInt()
            val hours = (hoursRemaining % 24).toInt()
            if (hours == 0) "${days}d left" else "${days}d ${hours}h left"
        }
    }
}

fun formatElapsedTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
