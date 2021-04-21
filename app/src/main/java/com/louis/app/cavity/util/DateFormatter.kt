package com.louis.app.cavity.util

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToLong

object DateFormatter {
    private const val DAY_IN_MILLIS = 1000 * 60 * 60 * 24
    private const val YEAR_IN_MILLIS = 1000 * 60 * 60 * 24 * 365.25f

    fun formatDate(timestamp: Long?): String {
        return if (timestamp != null && timestamp > 0L) {
            // TODO: i18n
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH)
            val calendar = Calendar.getInstance()

            calendar.timeInMillis = timestamp

            formatter.format(calendar.time)
        } else {
            ""
        }
    }

    fun roundToDay(timestamp: Long): Long {
        val exceed = timestamp % DAY_IN_MILLIS
        return timestamp - exceed
    }

    fun roundToYear(timestamp: Long): Long {
        val exceed = timestamp % YEAR_IN_MILLIS
        return timestamp - exceed.roundToLong()
    }

    fun isToday(timestamp: Long?): Boolean {
        val currentTime = System.currentTimeMillis()
        val startOfDay = roundToDay(currentTime)
        val endOfDay = startOfDay + DAY_IN_MILLIS - 1 // -1 to not overlap next day

        return timestamp in startOfDay..endOfDay
    }
}
